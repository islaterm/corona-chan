package islaterm.coronachan.spiders.minsal

import islaterm.coronachan.spiders.AbstractSpider

/**
 * Web crawler for official information of the MINSAL on quarantine zones.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.2
 * @since 1.0
 */
class QuarantineSpider : AbstractSpider("https://www.minsal.cl/nuevo-coronavirus-2019-ncov/") {

  override fun concreteScrape() {
    iterateParagraphs() {}
  }
}