package islaterm.coronachan.spiders

import islaterm.coronachan.utils.LoggerKun
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

const val TABLE_ROW = "tr"
const val TABLE = "table"
const val ROW_CELL = "td"

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version v1.0.2-b.3
 * @since 1.0
 */
interface ICrownSpider {
  /**
   * Scrapes a site for information related to Corona-Virus and saves the relevant information into the class' fields.
   */
  fun scrape()
}

/**
 * Abstract class that contains the common functionalities of all the web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version v1.0.5-b.1
 * @since 1.0
 */
abstract class AbstractSpider(private val url: String) : ICrownSpider {
  protected val logger by LoggerKun()

  protected fun outputToFile(content: String, filename: String) {
    val output = File("../../corona-chan/public/$filename")
    output.writeText(content)
  }

  override fun scrape() {
    logger.info("Scrapping")
    val document = Jsoup.connect(url).get()
    concreteScrape(document)
    logger.info("Done with scrapping")
  }

  /**
   * Scrapes the html document to get the desired info.
   */
  protected abstract fun concreteScrape(document: Document)
}

fun main() {
  val spider = MinsalInfectionsSpider()
  spider.scrape()
  spider.generatePlots()
}