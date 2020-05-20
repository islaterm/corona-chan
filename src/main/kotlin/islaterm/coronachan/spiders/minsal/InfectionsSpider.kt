package islaterm.coronachan.spiders.minsal

import islaterm.coronachan.spiders.AbstractSpider
import islaterm.coronachan.spiders.ROW_CELL
import islaterm.coronachan.spiders.TABLE
import islaterm.coronachan.spiders.TABLE_ROW
import islaterm.coronachan.utils.IDayRecord
import islaterm.coronachan.utils.InfectionRecord
import islaterm.coronachan.utils.InfectionTables
import java.time.LocalDate

/**
 * Web crawler for official information of the MINSAL.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.10
 * @since 1.0
 */
class InfectionsSpider(queryDay: LocalDate) :
  AbstractSpider(
    "https://www.minsal.cl/nuevo-coronavirus-2019-ncov/casos-confirmados-en-chile-covid-19/",
    queryDay,
    "infections",
    "date,zone,cumulative_total_infections,total_new_infections,new_infections_with_symptoms,new_infections_without_symptoms,deceased,%_total"
  ) {

  private lateinit var footnote: String
  private val categories = mutableListOf<String>()
  private val todayData = mutableMapOf<String, MutableList<Number>>()
  private val yesterdayData = mutableMapOf<String, MutableList<Number>>()

  override fun scrape() {
    logger.info("Scrapping...")
    super.getRecords(InfectionRecord::class.java)
    logger.info("Done with scrapping")
  }

  override fun scrapeNewRecord() {
    parseTable()
    getFootnote()
  }

  private fun getFootnote() {
    iterateParagraphs {
      if (it.text().contains('*')) {
        footnote = "'$it'"
      }
    }
  }

  /**
   * Generates a .csv file from the MINSAL COVID-19 table.
   */
  private fun parseTable() {
    latestRecord = mutableListOf()
    document.getElementsByTag(TABLE)[0].getElementsByTag(
      TABLE_ROW
    ).withIndex().forEach { (idx, row) ->
      if (idx > 1) { // Skips the first 2 rows since those are headers
        val cells = row.getElementsByTag(ROW_CELL).toList()
        latestRecord.add(
          InfectionRecord(
            day = "$queryDay",
            place = cells[0].text().replace("Total", "Chile"),
            totalInfections = cells[1].text().replace(".", "").toInt(),
            newInfections = cells[2].text().replace(".", "").toInt(),
            newWithSymptoms = cells[3].text().replace(".", "").toInt(),
            newWithoutSymptoms = cells[4].text().replace(".", "").toInt(),
            deceased = cells[5].text().replace(".", "").toInt(),
            percentage = "\\d%".toRegex().replace(cells[6].text().replace(',', '.')) {
              it.value.dropLast(1)
            }.toDouble()
          )
        )
      }
    }
    saveRecords()
  }

  override fun generateDocuments() {
    logger.info("MINSAL spider is generating the plots")
    var graphicsLinks = ""
    val xData = categories.dropLast(1)
    val yesterdayTotals = mutableListOf<Number>()
    val todayTotals = mutableListOf<Number>()
    val places = mutableListOf<String>()
    val latest = getRecordsByPlace(latestRecord)
    val oldest = getRecordsByPlace(oldestRecord)
//    for (table in headers.split(",\\s*".toRegex())) {
//      val title = table.replace(Pattern.compile("[\\r\\n]").toRegex(), "")
//      val chart = GroupedBarChart(title)
//      chart.xData = xData
//      chart.addData(yesterdayData[table]!!.dropLast(1), "${LocalDate.now().minusDays(1)}")
//      chart.addData(todayData[table]!!.dropLast(1), "${LocalDate.now()}")
//      val filename = "$title.html".replace(Pattern.compile("[*:%]").toRegex(), "").replace(" ", "_")
//      graphicsLinks += "{ text: '$title', href: '$filename' },\n${" ".repeat(10)}"
//      outputToFile(chart.toHtml(), filename)
//      yesterdayTotals.add(yesterdayData[table]!!.last())
//      todayTotals.add(todayData[table]!!.last())
//    }
//    val chart = GroupedBarChart("Totales Chile")
//    chart.xData = tables
//    chart.addData(yesterdayTotals, "${LocalDate.now().minusDays(1)}")
//    chart.addData(todayTotals, "${LocalDate.now()}")
//    graphicsLinks += "{ href: 'Totales+Chile.html', text: 'Totales Chile' }\n"
//    outputToFile(chart.toHtml(), "Totales+Chile.html")
//    coronaChanVue.writeText(
//      coronaChanVue.readText()
//        .replace("'~graphics~'", graphicsLinks)
//        .replace("'~footnote~'", footnote)
//    )
    logger.info("Done with generating the plots")
  }

  private fun getRecordsByPlace(record: MutableList<IDayRecord?>): Map<String, Map<String, Number>> {
    val infections = mutableMapOf<String, Map<String, Number>>()
    val tables = InfectionTables()
    record.forEach {
      infections[(it as InfectionRecord).place] =
        mapOf<String, Number>(
          tables.totalInfections to it.totalInfections,
          tables.newInfections to it.newInfections,
          tables.newWithSymptoms to it.newWithSymptoms,
          tables.newWithoutSymptoms to it.newWithoutSymptoms,
          tables.deceased to it.deceased,
          tables.percentage to it.percentage
        )
    }
    return infections
  }
}

