package islaterm.coronachan.spiders.minsal

import islaterm.coronachan.coronaChanVue
import islaterm.coronachan.spiders.AbstractSpider
import islaterm.coronachan.spiders.ROW_CELL
import islaterm.coronachan.spiders.TABLE
import islaterm.coronachan.spiders.TABLE_ROW
import islaterm.coronachan.utils.IDayRecord
import islaterm.coronachan.utils.InfectionRecord
import islaterm.coronachan.utils.InfectionTables
import islaterm.coronachan.utils.kotly.GroupedBarChart
import java.time.LocalDate

/**
 * Web crawler for official information of the MINSAL.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5
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
  private val tables = InfectionTables()

  override fun scrape() {
    logger.info("Scrapping...")
    super.getRecords(InfectionRecord::class.java)
    getFootnote()
    logger.info("Done with scrapping")
  }

  private fun getFootnote() {
    iterateParagraphs {
      if (it.text().contains('*')) {
        footnote = "'$it'"
      }
    }
  }

  /**
   * Generates a record from the MINSAL COVID-19 table and saves the retrieved data.
   */
  override fun scrapeNewRecord() {
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
    val latest = getRecordsByPlace(latestRecord)
    val previous = getRecordsByPlace(oldestRecord)
    val tableList = listOf(
      tables.deceased,
      tables.percentage,
      tables.newWithoutSymptoms,
      tables.newWithSymptoms,
      tables.newInfections,
      tables.totalInfections
    )
    val latestTotals = mutableListOf<Number>()
    val prevTotals = mutableListOf<Number>()
    for (table in tableList) {
      val chart = GroupedBarChart(table)
      chart.xData = latest.keys.toList().filter { it != "Chile" }
      val latestData = mutableListOf<Number>()
      val oldestData = mutableListOf<Number>()
      for (place in chart.xData) {
        latest[place]?.get(table)?.let { latestData.add(it) }
        previous[place]?.get(table)?.let { oldestData.add(it) }
      }
      chart.addData(yData = oldestData, name = previousDate)
        .addData(yData = latestData, name = "$queryDay")
      val target = "$table.html".replace("[*:%]".toRegex(), "").replace(" ", "_")
      graphicsLinks += "{ text: '$table', href: '$target' },\r\n${" ".repeat(10)}"
      outputToFile(chart.toHtml(), target)
      latest["Chile"]?.get(table)?.let { latestTotals.add(it) }
      previous["Chile"]?.get(table)?.let { prevTotals.add(it) }
    }
    val chart = GroupedBarChart("Totales Chile")
    chart.xData = tableList
    chart.addData(yData = prevTotals, name = previousDate)
      .addData(yData = latestTotals, name = "$queryDay")
    graphicsLinks += "{ href: 'Totales+Chile.html', text: 'Totales Chile' }\r\n"
    outputToFile(chart.toHtml(), "Totales+Chile.html")
    coronaChanVue.writeText(
      coronaChanVue.readText()
        .replace("'~graphics~'", graphicsLinks)
        .replace("'~footnote~'", footnote)
    )
    logger.info("Done with generating the plots")
  }

  private fun getRecordsByPlace(record: MutableList<IDayRecord?>): Map<String, Map<String, Number>> {
    val infections = mutableMapOf<String, Map<String, Number>>()
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

