package islaterm.coronachan

import islaterm.coronachan.utils.LoggerKun
import org.jsoup.Jsoup

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-a.4
 * @since 1.0
 */
interface ICrownSpider {
  /**
   * Scrapes a site for information related to Corona-Virus and saves the relevant information into the class' fields.
   */
  fun scrape()
}

abstract class AbstractSpider : ICrownSpider {
  protected val logger by LoggerKun()
}

/**
 * Web crawler for official information of the MINSAL.
 */
class MinsalSpider : AbstractSpider() {
  override fun scrape() {
    val url = "https://www.minsal.cl/nuevo-coronavirus-2019-ncov/casos-confirmados-en-chile-covid-19/"
    val document = Jsoup.connect(url).get()
    logger.info(document)
  }
}

fun main() {
  MinsalSpider().scrape()
}