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
import java.time.LocalDate
import kotlin.reflect.KClass

const val TABLE_ROW = "tr"
const val TABLE = "table"
const val ROW_CELL = "td"

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.9
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
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.9
 * @since 1.0
 */
abstract class AbstractSpider(private val url: String, protected val queryDay: LocalDate, filename: String) :
  ICrownSpider {
  protected val storageFile = File("$resources\\$filename")
  protected val logger by LoggerKun()
  protected val document: Document by lazy { Jsoup.connect(url).get() }
  protected lateinit var previousDate: String

  init {
    if (!storageFile.exists()) {
      storageFile.createNewFile()
    }
  }

  protected fun outputToFile(content: String, filename: String) {
    val output = File("../../corona-chan/public/$filename")
    output.writeText(content)
  }

  /**
   * Gets the last records stored by the spider.
   *
   * @param cls Class<T>
   *    the class of the record
   *
   * @return a pair of the records of the last 2 days
   */
  protected fun <T : IDayRecord> getLastRecords(cls: Class<T>): Pair<List<T?>, List<T?>> {
    val lastRecords = Pair(mutableListOf<T?>(), mutableListOf<T?>())
    val parser = YAMLFactory().createParser(storageFile)
    previousDate = "${queryDay.minusDays(1)}"
    ObjectMapper().registerModule(KotlinModule()).readValues(parser, cls).readAll().forEach {
      if (it.day == "$queryDay") {
        lastRecords.first.add(it)
      } else if (it.day == previousDate) {
        lastRecords.second.add(it)
      }
    }
    return lastRecords
  }

  /**
   * Iterates over the paragraphs of the html document and executes an action over them.
   */
  protected fun iterateParagraphs(action: (Element) -> Unit) {
    val paragraphs = document.getElementsByTag("p")
    paragraphs.forEach { action(it) }
  }
}