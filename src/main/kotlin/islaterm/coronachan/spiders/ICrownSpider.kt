package islaterm.coronachan.spiders

import islaterm.coronachan.utils.LoggerKun
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

abstract class AbstractSpider : ICrownSpider {
  protected val logger by LoggerKun()
  protected fun outputToFile(content: String, filename: String) {
    val output = File("../../corona-chan/public/$filename")
    output.writeText(content)
  }
}

fun main() {
  val spider = MinsalSpider()
  spider.scrape()
  spider.generatePlots()
}