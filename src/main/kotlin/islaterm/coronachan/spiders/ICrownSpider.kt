package islaterm.coronachan.spiders

import islaterm.coronachan.spiders.minsal.InfectionsSpider
import islaterm.coronachan.utils.LoggerKun
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File

const val TABLE_ROW = "tr"
const val TABLE = "table"
const val ROW_CELL = "td"

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.3
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
 * @version 1.0.5-b.3
 * @since 1.0
 */
abstract class AbstractSpider(private val url: String) : ICrownSpider {
  protected val logger by LoggerKun()
  protected val document: Document by lazy { Jsoup.connect(url).get() }

  protected fun outputToFile(content: String, filename: String) {
    val output = File("../../corona-chan/public/$filename")
    output.writeText(content)
  }

  override fun scrape() {
    logger.info("Scrapping")
    concreteScrape()
    logger.info("Done with scrapping")
  }

  /**
   * Scrapes the html document to get the desired info.
   */
  protected abstract fun concreteScrape()

  /**
   * Iterates over the paragraphs of the html document and executes an action over them.
   */
  protected fun iterateParagraphs(action: (Element) -> Unit) {
    val paragraphs = document.getElementsByTag("p")
    paragraphs.forEach { action(it) }
  }
}

fun main() {
  val spider = InfectionsSpider()
  spider.scrape()
  spider.generateDocuments()
}