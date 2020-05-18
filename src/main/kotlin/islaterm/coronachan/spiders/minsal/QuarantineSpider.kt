package islaterm.coronachan.spiders.minsal

import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import islaterm.coronachan.resources
import islaterm.coronachan.spiders.AbstractSpider
import islaterm.coronachan.spiders.minsal.QuarantineSpider.QuarantineStatus.*
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
 * @version 1.0.5-b.5
 * @since 1.0
 */
class QuarantineSpider : AbstractSpider("https://www.minsal.cl/nuevo-coronavirus-2019-ncov/") {
  private enum class QuarantineStatus {
    STAY, QUIT, ENTER
  }

  private val quarantineZones = mutableMapOf<QuarantineStatus, List<String>>()

  override fun concreteScrape() {
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
      writer.write(mapOf(today to quarantineZones))
      writer.write(yesterdayQuarantine)
      writer.close()
    } else {
      todayQuarantine = (lastRecord[today] as List<*>).filterIsInstance<String>()
      val yesterdayData = reader.read(Map::class.java)?.get("${LocalDate.now().minusDays(1)}")
      yesterdayQuarantine = (yesterdayData as? List<*>)?.filterIsInstance<String>()
    }
    val stay = yesterdayQuarantine?.intersect(todayQuarantine) ?: todayQuarantine
    val quit = yesterdayQuarantine?.minus(stay) ?: listOf()
    val enter = todayQuarantine.minus(stay)
    quarantineZones[ENTER] = enter
    quarantineZones[QUIT] = quit
    quarantineZones[STAY] = stay.toList()
  }

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
    // TODO: Add different values for quarantine status
//    val quarantineText = "'${quarantineZones.joinToString("', \n${" ".repeat(10)}'")}'"
//    coronaChanVue.writeText(
//      coronaChanVue.readText()
//        .replace("'~quarantine~'", quarantineText)
//    )
    logger.info("Finished generating documents")
  }
}

