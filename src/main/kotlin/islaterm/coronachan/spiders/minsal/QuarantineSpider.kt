package islaterm.coronachan.spiders.minsal

import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import islaterm.coronachan.coronaChanVue
import islaterm.coronachan.resources
import islaterm.coronachan.spiders.AbstractSpider
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import java.util.regex.Pattern

/**
 * Web crawler for official information of the MINSAL on quarantine zones.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.4
 * @since 1.0
 */
class QuarantineSpider : AbstractSpider("https://www.minsal.cl/nuevo-coronavirus-2019-ncov/") {
  lateinit var quarantineZones: List<String>

  override fun concreteScrape() {
    val quarantinesFile = File("$resources\\quarantines.yml")
    val reader = YamlReader(FileReader(quarantinesFile))
    val lastRecord = reader.read(Map::class.java)
    val today = "${LocalDate.now()}"

    if (lastRecord == null || lastRecord.keys.none { it == today }) {
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
      quarantineZones = quarantineZonesTxt.split(Pattern.compile("\\s*–\\s*").toRegex()).drop(1)

      val writer = YamlWriter(FileWriter(quarantinesFile))
      writer.write(mapOf(today to quarantineZones))
      writer.close()
    } else {
      quarantineZones = (lastRecord[today] as List<*>).filterIsInstance<String>()
    }
  }

  override fun generateDocuments() {
    logger.info("Generating documents...")
    val quarantineText = "'${quarantineZones.joinToString("', \n${" ".repeat(10)}'")}'"
    coronaChanVue.writeText(
      coronaChanVue.readText()
        .replace("'~quarantine~'", quarantineText)
    )
    logger.info("Finished generating documents")
  }
}

