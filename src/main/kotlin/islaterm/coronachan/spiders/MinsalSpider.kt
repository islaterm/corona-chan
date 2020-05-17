package islaterm.coronachan.spiders

import islaterm.coronachan.utils.kotly.GroupedBarChart
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * Web crawler for official information of the MINSAL.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-rc.1
 * @since 1.0
 */
class MinsalSpider : AbstractSpider() {
  private lateinit var footnote: String
  private val categories = mutableListOf<String>()
  private val tables = mutableListOf<String>()
  private val todayData = mutableMapOf<String, MutableList<Number>>()
  private val yesterdayData = mutableMapOf<String, MutableList<Number>>()

  // TODO: Change table for lists
  override fun scrape() {
    logger.info("MinsalSpider is scrapping")
    val url = "https://www.minsal.cl/nuevo-coronavirus-2019-ncov/casos-confirmados-en-chile-covid-19/"
    val document = Jsoup.connect(url).get()
    parseTable(document)
    getFootnote(document)
    parseYesterdayCSV()
    logger.info("MinsalSpider is done with scrapping")
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
   * Generates a .csv file from the MINSAL COVID-19 table.
   */
  private fun parseTable(document: Document) {
    var csvString = ""
    for ((idx, row) in document.getElementsByTag(TABLE)[0].getElementsByTag(TABLE_ROW).withIndex()) {
      if (idx != 0) { // Skips the first row
        val cells = row.getElementsByTag(ROW_CELL)
        csvString += parseCells(cells, idx == 1)
      }
    }
    val date = LocalDate.now()
    val output = File(".\\src\\main\\resources\\minsal_$date.csv")
    output.writeText(csvString)
  }

  private fun parseYesterdayCSV() {
    val date = LocalDate.now().minusDays(1)
    File(".\\src\\main\\resources\\minsal_$date.csv").readLines().forEachIndexed { rowIdx, line ->
      line.split(",").forEachIndexed { cellIdx, cell ->
        if (cellIdx != 0) {
          if (rowIdx == 0) {
            yesterdayData[tables[cellIdx - 1]] = mutableListOf()
          } else {
            yesterdayData[tables[cellIdx - 1]]?.add(cell.toDouble())
          }
        }
      }

    }
  }

  /**
   * Parses the cells of a row and returns the result as a csv String.
   */
  private fun parseCells(cells: Elements, isFirstRow: Boolean): String {
    var csvString = ""
    for ((col, cell) in cells.withIndex()) {
      var text = cell.text().replace(".", "").replace(',', '.')
      text = "\\d%".toRegex().replace(text) {
        it.value.dropLast(1)
      }
      when {
        isFirstRow -> {
          tables.add(text)
          todayData[text] = mutableListOf()
          yesterdayData[text] = mutableListOf()
        }
        col == 0 -> categories.add(text)
        else -> todayData[tables[col - 1]]?.add(text.toDouble())
      }
      csvString += "${text}${if (col == cells.size - 1) System.lineSeparator() else ","}"
    }
    return csvString
  }

  fun generatePlots() {
    logger.info("Minsal spider is generating the plots")
    var graphicsLinks = ""
    val xData = categories.dropLast(1)
    for (table in tables) {
      val title = table.replace(Pattern.compile("[\\r\\n]").toRegex(), "")
      val chart = GroupedBarChart(title)
      chart.xData = xData
      chart.addData(yesterdayData[table]!!.dropLast(1), "${LocalDate.now().minusDays(1)}")
      chart.addData(todayData[table]!!.dropLast(1), "${LocalDate.now()}")
      val filename = "$title.html".replace(Pattern.compile("[*:%]").toRegex(), "").replace(" ", "_")
      graphicsLinks += "      <li>\n" +
          "        <a\n" +
          "          href=\"$filename\"\n" +
          "          target=\"_blank\"\n" +
          "          rel=\"noopener\"\n" +
          "        >$title</a>\n" +
          "      </li>\n"
      outputToFile(chart.toHtml(), filename)
    }
    val coronaChanVue = File("../../corona-chan/src/components/CoronaChan.vue")
    val template = File("src/main/resources/template.vue").readText()
      .replace("~graphics~", graphicsLinks)
      .replace("~footnote~", footnote)
    coronaChanVue.writeText(template)
    logger.info("Minsal spider is done with generating the plots")
  }
}