package islaterm.coronachan

import islaterm.coronachan.utils.LoggerKun
import org.jsoup.Jsoup
import tech.tablesaw.api.Table
import tech.tablesaw.plotly.components.Figure
import tech.tablesaw.plotly.components.Layout
import tech.tablesaw.plotly.components.Page
import tech.tablesaw.plotly.traces.BarTrace
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.regex.Pattern

const val TABLE_ROW = "tr"
const val TABLE = "table"
const val ROW_CELL = "td"

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-b.1
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
 */
class MinsalSpider : AbstractSpider() {
  lateinit var table: Table

  override fun scrape() {
    val url = "https://www.minsal.cl/nuevo-coronavirus-2019-ncov/casos-confirmados-en-chile-covid-19/"
    val document = Jsoup.connect(url).get()
    var csvString = ""
    for ((idx, row) in document.getElementsByTag(TABLE)[0].getElementsByTag(TABLE_ROW).withIndex()) {
      if (idx == 1) {
        csvString += "-,"
      }
      if (idx != 0) { // Skips the first column
        val cells = row.getElementsByTag(ROW_CELL)
        for ((col, cell) in cells.withIndex()) {

          val text = cell.text().replace(".", "").replace(',', '.')
          csvString += "${text}${if (col == cells.size - 1) System.lineSeparator() else ","}"
        }
      }
    }
    val date = LocalDate.now()
    val output = File(".\\src\\main\\resources\\minsal_$date")
    output.writeText(csvString)
    table = Table.read().csv(csvString, "Casos COVID-19 en Chile")
    table = table.first(table.rowCount() - 1)
  }

  fun plot() {
    val title = "Casos totales acumulados"
    val layout = Layout.builder()
      .title(title)
      .build()
    val trace = BarTrace.builder(table.categoricalColumn(0), table.numberColumn(1))
      .build()
    val page =
      Page.pageBuilder(Figure(layout, trace), title.replace(Pattern.compile("[^A-Za-z0-9]").toRegex(), "_")).build()
    syncOutput(
      "$page",
      "${URLEncoder.encode(title, Charset.forName("UTF-8"))}.html",
      "../../islaterm.github.io"
    )
  }

  private fun syncOutput(content: String, filename: String, root: String) {
    val indexText = File("src/main/resources/template.html").readText().replace("~today~", "${LocalDateTime.now()}")
    val output = File("$root/$filename")
    output.writeText(content)
    File("$root/index.html").writeText(indexText)
    val runtime = Runtime.getRuntime()
    val os = System.getProperty("os.name")
    val process = when {
      os.contains("win", true) -> runtime.exec("powershell.exe .\\git-sync.ps1")
      else -> runtime.exec("sh git-sync.sh")
    }
    BufferedReader(InputStreamReader(process.inputStream)).lines()
      .forEach { logger.info(it) }
    BufferedReader(InputStreamReader(process.errorStream)).lines()
      .forEach { logger.error(it) }
  }

}

fun main() {
  val spider = MinsalSpider()
  spider.scrape()
  spider.plot()
}