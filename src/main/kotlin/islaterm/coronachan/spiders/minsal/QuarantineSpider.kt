package islaterm.coronachan.spiders.minsal

import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import islaterm.coronachan.coronaChanVue
import islaterm.coronachan.resources
import islaterm.coronachan.spiders.AbstractSpider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * Web crawler for official information of the MINSAL on quarantine zones.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-rc.1
 * @since 1.0
 */
class QuarantineSpider : AbstractSpider("https://www.minsal.cl/nuevo-coronavirus-2019-ncov/") {

  private lateinit var stay: List<String>
  private lateinit var quit: List<String>
  private lateinit var enter: List<String>

  override fun concreteScrape() {
    logger.info("Scraping...")
    val quarantinesFile = File("$resources\\quarantines.yml")
    val reader = try {
      YamlReader(FileReader(quarantinesFile))
    } catch (e: FileNotFoundException) {
      null
    }
    val lastRecord = reader?.read(Map::class.java)
    val today = "${LocalDate.now()}"
    val yesterdayQuarantine: List<String>?
    val todayQuarantine: List<String>

    if (lastRecord == null || lastRecord.keys.none { it == today }) {
      yesterdayQuarantine = (lastRecord?.get("${LocalDate.now().minusDays(1)}") as? List<*>)?.filterIsInstance<String>()
      todayQuarantine = getTodayQuarantine()

      val writer = YamlWriter(FileWriter(quarantinesFile))
      writer.write(mapOf(today to todayQuarantine))
      writer.write(yesterdayQuarantine)
      writer.close()
    } else {
      todayQuarantine = (lastRecord[today] as List<*>).filterIsInstance<String>()
      val yesterdayData = reader.read(Map::class.java)?.get("${LocalDate.now().minusDays(1)}")
      yesterdayQuarantine = (yesterdayData as? List<*>)?.filterIsInstance<String>()
    }
    stay = (yesterdayQuarantine?.intersect(todayQuarantine) ?: todayQuarantine).toList()
    quit = yesterdayQuarantine?.minus(stay) ?: listOf()
    enter = todayQuarantine.minus(stay)
    logger.info("Done with scrapping")
  }

  /**
   * Parses the document and returns a list with all the zones in quarantine for today.
   */
  private fun getTodayQuarantine(): List<String> {
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

