package islaterm.coronachan.spiders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import islaterm.coronachan.resources
import islaterm.coronachan.utils.IDayRecord
import islaterm.coronachan.utils.LoggerKun
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileWriter
import java.time.LocalDate

const val TABLE_ROW = "tr"
const val TABLE = "table"
const val ROW_CELL = "td"

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-rc.2
 * @since 1.0
 */
interface ICrownSpider {
  /**
   * Scrapes a site for information related to Corona-Virus and saves the relevant information into the class' fields.
   */
  fun scrape()
  fun generateDocuments()
}

/**
 * Abstract class that contains the common functionalities of all the web crawlers.
 *
 * @constructor
 *    Creates the necessary storage files if they're not present in the FS.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-rc.2
 * @since 1.0
 */
abstract class AbstractSpider(
  private val url: String,
  protected val queryDay: LocalDate,
  filename: String,
  headers: String
) : ICrownSpider {
  protected val logger by LoggerKun()
  protected val document: Document by lazy { Jsoup.connect(url).get() }
  protected var latestRecord = mutableListOf<IDayRecord?>()
  protected var oldestRecord = mutableListOf<IDayRecord?>()
  protected lateinit var previousDate: String

  private val mapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
  private val storageYAMLFile = File("$resources/$filename.yml")
  private val storageCSVFile = File("$resources/tables/$filename.csv")

  init {
    if (!storageYAMLFile.exists()) {
      storageYAMLFile.createNewFile()
    }
    if (!storageCSVFile.exists()) {
      storageCSVFile.createNewFile()
      storageCSVFile.writeText("$headers\r\n")
    }
  }

  /**
   * Gets the last records stored by the spider.
   *
   * @param cls Class<T>
   *    the class of the record
   *
   * @return a pair of the records of the last 2 days
   */
  protected fun <T : IDayRecord> getRecords(cls: Class<T>) {
    val parser = YAMLFactory().createParser(storageYAMLFile)
    previousDate = "${queryDay.minusDays(1)}"
    var inLatestRecord = true
    var prevDate = ""
    mapper.readValues(parser, cls).readAll().forEach {
      if (prevDate.isBlank()) {
        prevDate = it.day
      }
      val latestDate = it.day
      inLatestRecord = inLatestRecord && latestDate == prevDate
      if (inLatestRecord) {
        latestRecord.add(it)
      } else {
        oldestRecord.add(it)
      }
      prevDate = it.day
    }
    if (latestRecord[0]?.day != "$queryDay" // All the records on the list should have the same date
    ) {
      oldestRecord = latestRecord
      scrapeNewRecord()
    }
  }

  /**
   * Saves the latest records to the appropriate files
   */
  protected fun saveRecords() {
    val writer = mapper.writer().writeValues(storageYAMLFile)
    latestRecord.forEach { writer.write(it) }
    oldestRecord.forEach { writer.write(it) }
    FileWriter(storageCSVFile, true).use { latestRecord.forEach { zone -> it.append("$queryDay, $zone\r\n") } }
  }

  /**
   * Iterates over the paragraphs of the html document and executes an action over them.
   */
  protected fun iterateParagraphs(action: (Element) -> Unit) {
    val paragraphs = document.getElementsByTag("p")
    paragraphs.forEach { action(it) }
  }

  protected fun outputToFile(content: String, filename: String) {
    val output = File("../../corona-chan/public/$filename")
    output.writeText(content)
  }

  /**
   * Parses the document to get the updated data.
   * The data is then stored in the class' fields and into files.
   */
  protected abstract fun scrapeNewRecord()
}