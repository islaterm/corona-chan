package islaterm.coronachan.spiders.minsal

import islaterm.coronachan.spiders.AbstractSpider
import islaterm.coronachan.spiders.ROW_CELL
import islaterm.coronachan.spiders.TABLE
import islaterm.coronachan.spiders.TABLE_ROW
import islaterm.coronachan.utils.kotly.GroupedBarChart
import org.jsoup.select.Elements
import java.io.File
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * Web crawler for official information of the MINSAL.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.2
 * @since 1.0
 */
class InfectionsSpider :
  AbstractSpider("https://www.minsal.cl/nuevo-coronavirus-2019-ncov/casos-confirmados-en-chile-covid-19/") {

  private lateinit var footnote: String
  private val categories = mutableListOf<String>()
  private val tables = mutableListOf<String>()
  private val todayData = mutableMapOf<String, MutableList<Number>>()
  private val yesterdayData = mutableMapOf<String, MutableList<Number>>()


  override fun concreteScrape() {
    parseTable()
    getFootnote()
    parseYesterdayCSV()
  }

  private fun getFootnote() {
    iterateParagraphs() {
      if (it.text().contains('*')) {
        footnote = "$it"
      }
    }
  }

  /**
   * Generates a .csv file from the MINSAL COVID-19 table.
   */
  private fun parseTable() {
    var csvString = ""
    for ((idx, row) in document.getElementsByTag(TABLE)[0].getElementsByTag(
      TABLE_ROW
    ).withIndex()) {
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
    logger.info("MINSAL spider is generating the plots")
    var graphicsLinks = ""
    val xData = categories.dropLast(1)
    val yesterdayTotals = mutableListOf<Number>()
    val todayTotals = mutableListOf<Number>()
    for (table in tables) {
      val title = table.replace(Pattern.compile("[\\r\\n]").toRegex(), "")
      val chart = GroupedBarChart(title)
      chart.xData = xData
      chart.addData(yesterdayData[table]!!.dropLast(1), "${LocalDate.now().minusDays(1)}")
      chart.addData(todayData[table]!!.dropLast(1), "${LocalDate.now()}")
      val filename = "$title.html".replace(Pattern.compile("[*:%]").toRegex(), "").replace(" ", "_")
      graphicsLinks += "{ text: '$title', href: '$filename' },\n"
      outputToFile(chart.toHtml(), filename)
      yesterdayTotals.add(yesterdayData[table]!!.last())
      todayTotals.add(todayData[table]!!.last())
    }
    val chart = GroupedBarChart("Totales Chile")
    chart.xData = tables
    chart.addData(yesterdayTotals, "${LocalDate.now().minusDays(1)}")
    chart.addData(todayTotals, "${LocalDate.now()}")
    graphicsLinks += "{href: 'Totales+Chile.html', text: 'Totales Chile'}\n"
    outputToFile(chart.toHtml(), "Totales+Chile.html")
    val coronaChanVue = File("../../corona-chan/src/components/CoronaChan.vue")
    val template = File("src/main/resources/template.vue").readText()
      .replace("~graphics~", graphicsLinks)
      .replace("~footnote~", footnote)
    coronaChanVue.writeText(template)
    logger.info("MINSAL spider is done with generating the plots")
  }
}