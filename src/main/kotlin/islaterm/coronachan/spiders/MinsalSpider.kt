package islaterm.coronachan.spiders

import islaterm.coronachan.utils.kotly.BarChart
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import tech.tablesaw.api.Table
import java.io.File
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * Web crawler for official information of the MINSAL.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.3
 * @since 1.0
 */
class MinsalSpider : AbstractSpider() {
  private lateinit var footnote: String
  lateinit var todayTable: Table

  // TODO: Change table for lists
  override fun scrape() {
    logger.info("MinsalSpider is scrapping")
    val url = "https://www.minsal.cl/nuevo-coronavirus-2019-ncov/casos-confirmados-en-chile-covid-19/"
    val document = Jsoup.connect(url).get()
    val csvString = generateCSV(document)
    getFootnote(document)
    todayTable = Table.read().csv(csvString, "Casos COVID-19 en Chile")
    todayTable = todayTable.first(todayTable.rowCount() - 1)
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
   * Generates a .csv file from the MINSAL COVID-19 table and returns it's contents as a string.
   */
  private fun generateCSV(document: Document): String {
    var csvString = ""
    for ((idx, row) in document.getElementsByTag(TABLE)[0].getElementsByTag(
      TABLE_ROW
    ).withIndex()) {
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
    val date = LocalDate.now()
    val output = File(".\\src\\main\\resources\\minsal_$date.csv")
    output.writeText(csvString)
    return csvString
  }

  fun generatePlots() {
    logger.info("Minsal spider is generating the plots")
    var graphicsLinks = ""
    for (i in 1 until todayTable.columnCount()) {
      val title =
        todayTable.column(i).title().replace("Column: ", "").replace(
          Pattern.compile(
            "[\\r\\n]"
          ).toRegex(), "")
      val chart = BarChart(title)
      chart.xData = todayTable.stringColumn(0).asList()
      chart.yData = todayTable.numberColumn(i).asList()
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