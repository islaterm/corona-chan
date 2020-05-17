package islaterm.coronachan

import islaterm.coronachan.utils.LoggerKun
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Layout
import tech.tablesaw.plotly.components.Page
import tech.tablesaw.plotly.traces.BarTrace
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import java.util.regex.Pattern

const val TABLE_ROW = "tr"
const val TABLE = "table"
const val ROW_CELL = "td"

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-rc.1
 * @since 1.0
 */
interface ICrownSpider {
  /**
   * Scrapes a site for information related to Corona-Virus and saves the relevant information into the class' fields.
   */
  fun scrape()
}

abstract class AbstractSpider : ICrownSpider {
  protected val logger by LoggerKun()
}

/**
 * Web crawler for official information of the MINSAL.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-rc.1
 * @since 1.0
 */
class MinsalSpider : AbstractSpider() {
  private lateinit var footnote: String
  lateinit var table: Table

  override fun scrape() {
    val url = "https://www.minsal.cl/nuevo-coronavirus-2019-ncov/casos-confirmados-en-chile-covid-19/"
    val document = Jsoup.connect(url).get()
    val csvString = generateCSV(document)
    getFootnote(document)
    val date = LocalDate.now()
    val output = File(".\\src\\main\\resources\\minsal_$date.csv")
    output.writeText(csvString)
    table = Table.read().csv(csvString, "Casos COVID-19 en Chile")
    table = table.first(table.rowCount() - 1)
  }

  private fun getFootnote(document: Document) {
    val paragraphs = document.getElementsByTag("p")
    for (paragraph in paragraphs) {
      if (paragraph.text().contains('*')) {
        footnote = "$paragraph"
      }
    }
  }

  /**
   * Generates a .csv string from the MINSAL COVID-19 table.
   */
  private fun generateCSV(document: Document): String {
    var csvString = ""
    for ((idx, row) in document.getElementsByTag(TABLE)[0].getElementsByTag(TABLE_ROW).withIndex()) {
      if (idx == 1) {
        csvString += "-,"
      }
      if (idx != 0) { // Skips the first column
        val cells = row.getElementsByTag(ROW_CELL)
        for ((col, cell) in cells.withIndex()) {

          var text = cell.text().replace(".", "").replace(',', '.')
          text = "\\d%".toRegex().replace(text) {
            it.value.dropLast(1)
          }
          csvString += "${text}${if (col == cells.size - 1) System.lineSeparator() else ","}"
        }
      }
    }
    return csvString
  }

  fun generatePlots() {
    var graphicsLinks = ""
    for (i in 1 until table.columnCount()) {
      val title = table.column(i).title().replace("Column: ", "").replace(Pattern.compile("[\\r\\n]").toRegex(), "")
      val layout = Layout.builder()
        .title("$title ${LocalDate.now()}")
        .build()
      val trace = BarTrace.builder(table.categoricalColumn(0), table.numberColumn(i))
        .build()
      val filename = "$title.html".replace(Pattern.compile("[*:%]").toRegex(), "").replace(" ", "_")
      graphicsLinks += "      <li>\n" +
          "        <a\n" +
          "          href=\"$filename\"\n" +
          "          target=\"_blank\"\n" +
          "          rel=\"noopener\"\n" +
          "        >$title</a>\n" +
          "      </li>\n"
      val page =
        Page.pageBuilder(Figure(layout, trace), title.replace(Pattern.compile("[^A-Za-z0-9]").toRegex(), "_")).build()
      outputToFile("$page".replace(Pattern.compile("[\\r\\n]").toRegex(), "\r\n"), filename)
    }
    val coronaChanVue = File("../../corona-chan/src/components/CoronaChan.vue")
    val template = File("src/main/resources/template.vue").readText()
      .replace("~graphics~", graphicsLinks)
      .replace("~footnote~", footnote)
    coronaChanVue.writeText(template)
    syncOutput()
  }

  private fun outputToFile(content: String, filename: String) {
    val output = File("../../corona-chan/public/$filename")
    output.writeText(content)
  }

  private fun syncOutput() {
    val runtime = Runtime.getRuntime()
    val os = System.getProperty("os.name")
    val process = when {
      os.contains("win", true) -> runtime.exec("powershell.exe .\\git-sync.ps1")
      else -> runtime.exec("sh git-sync.sh")
    }
    val stdThread = Thread {
      BufferedReader(InputStreamReader(process.inputStream)).lines()
        .forEach { logger.info(it) }
    }
    val errThread = Thread {
      BufferedReader(InputStreamReader(process.errorStream)).lines()
        .forEach { logger.info(it) }
    }
    stdThread.start()
    errThread.start()
  }
}

fun main() {
  val spider = MinsalSpider()
  spider.scrape()
  spider.generatePlots()
}