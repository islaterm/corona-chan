package islaterm.coronachan.spiders.minsal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import islaterm.coronachan.coronaChanVue
import islaterm.coronachan.resources
import islaterm.coronachan.spiders.AbstractSpider
import islaterm.coronachan.utils.IDayRecord
import java.io.FileWriter
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * Web crawler for official information of the MINSAL on quarantine zones.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.9
 * @since 1.0
 */
class QuarantineSpider(queryDay: LocalDate) :
  AbstractSpider("https://www.minsal.cl/nuevo-coronavirus-2019-ncov/", queryDay, "quarantines.yml") {

  private lateinit var stay: List<QuarantineRecord?>
  private lateinit var quit: List<QuarantineRecord?>
  private lateinit var enter: List<QuarantineRecord?>

  override fun scrape() {
    logger.info("Scraping...")
    val lastRecord = getLastRecords(QuarantineRecord::class.java)

    var latestQuarantine = lastRecord.first
    var previousQuarantine = lastRecord.second

    if (latestQuarantine.isEmpty()
      || latestQuarantine[0]?.day != "$queryDay" // All the records on the list should have the same date
    ) {
      previousQuarantine = latestQuarantine

      latestQuarantine = scrapeQuarantine()
      val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
      val writer = mapper.writer().writeValues(storageFile)
      latestQuarantine.forEach { zone -> writer.write(zone) }
      previousQuarantine.forEach { zone -> writer.write(zone) }
      FileWriter("$resources\\tables\\quarantine_zones.csv", true).use { writer ->
        latestQuarantine.forEach { zone -> writer.append("$queryDay, $zone\r\n") }
      }
    }

    stay = previousQuarantine.intersect(latestQuarantine).toList()
    quit = previousQuarantine.minus(stay)
    enter = latestQuarantine.minus(stay)
    logger.info("Done with scrapping")
  }

  /**
   * Parses the document and returns a list with all the zones in quarantine for today.
   */
  private fun scrapeQuarantine(): List<QuarantineRecord?> {
    var onQuarantineParagraph = false
    var quarantineZonesTxt = ""
    iterateParagraphs {
      if (onQuarantineParagraph) {
        quarantineZonesTxt = it.text()
        onQuarantineParagraph = false
      } else if (it.text() == "Cuarentenas vigentes:") {
        onQuarantineParagraph = true
      }
    }
    return quarantineZonesTxt.split(Pattern.compile("\\s*–\\s*").toRegex()).drop(1)
      .map { QuarantineRecord(it, "${LocalDate.now()}") }
  }

  override fun generateDocuments() {
    logger.info("Generating documents...")
    val quarantineText = "{\n" +
        "${
        if (stay.isNotEmpty())
          "${" ".repeat(12)} stay :['${stay.joinToString("', '")}'],\n"
        else
          ""
        }${
        if (enter.isNotEmpty())
          "${" ".repeat(12)} enter :['${enter.joinToString("', '")}'],\n"
        else
          ""
        }${
        if (quit.isNotEmpty())
          "${" ".repeat(12)} quit :['${quit.joinToString("', '")}'],\n"
        else
          ""
        }" +
        "${" ".repeat(10)}}\n"
    coronaChanVue.writeText(coronaChanVue.readText().replace("'~quarantine~'", quarantineText))
    logger.info("Finished generating documents")
  }
}

data class QuarantineRecord(val zone: String, override val day: String) : IDayRecord {
  override fun equals(other: Any?) = other is QuarantineRecord && zone == other.zone
  override fun hashCode() = zone.hashCode()
  override fun toString() = zone
}