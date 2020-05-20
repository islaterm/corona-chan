package islaterm.coronachan.spiders.minsal

import islaterm.coronachan.coronaChanVue
import islaterm.coronachan.spiders.AbstractSpider
import islaterm.coronachan.utils.IDayRecord
import islaterm.coronachan.utils.QuarantineRecord
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * Web crawler for official information of the MINSAL on quarantine zones.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5
 * @since 1.0
 */
class QuarantineSpider(queryDay: LocalDate) :
  AbstractSpider("https://www.minsal.cl/nuevo-coronavirus-2019-ncov/", queryDay, "quarantines", "date,zone") {

  private lateinit var stay: List<IDayRecord?>
  private lateinit var quit: List<IDayRecord?>
  private lateinit var enter: List<IDayRecord?>

  override fun scrape() {
    logger.info("Scraping...")
    getRecords(QuarantineRecord::class.java)
    stay = oldestRecord.intersect(latestRecord).toList()
    quit = oldestRecord.minus(stay)
    enter = latestRecord.minus(stay)
    logger.info("Done with scrapping")
  }

  override fun scrapeNewRecord() {
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
    latestRecord = quarantineZonesTxt.split(Pattern.compile("\\s*–\\s*").toRegex())
      .drop(1) // The first element is always an empty string 🤦‍
      .map { QuarantineRecord("$queryDay", zone = it) }
      .toMutableList()
    saveRecords()
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