package islaterm.coronachan

import islaterm.coronachan.utils.LoggerKunConfigFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.ConfigurationFactory.setConfigurationFactory
import org.jsoup.Jsoup

/**
 * Common interface for the Corona-Virus updates web crawlers.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-a.3
 * @since 1.0
 */
interface ICrownSpider {
  /**
   * Scrapes a site for information related to Corona-Virus and saves the relevant information into the class' fields.
   */
  fun scrape()
}

abstract class AbstractSpider : ICrownSpider {
  val logger: Logger

  init {
    setConfigurationFactory(LoggerKunConfigFactory())
    logger = LogManager.getLogger(javaClass)
  }
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