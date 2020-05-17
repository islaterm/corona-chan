package islaterm.coronachan

import islaterm.coronachan.spiders.minsal.InfectionsSpider
import islaterm.coronachan.utils.LoggerKun
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Corona-chan is a high-school girl who likes to play with spiders.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.2
 * @since 1.0
 */
fun main() {
  CoronaChan().run()
}

/**
 * Corona-chan missed her alarm and is late to school!
 * In the hurry to get ready she released all of her [spiders] >.<
 * 
 * While she was [run]ning to school with a toast in her mouth she met [LoggerKun] who was also late.
 * Are they going to be able to get to school on time or are they going to get grounded?
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.2
 * @since 1.0
 */
class CoronaChan {
  private val logger by LoggerKun()
  private val spiders = listOf(InfectionsSpider())

  /**
   * Runs all of corona's spiders in parallel to get information about COVID-19 and then syncs the retrieved data with
   * [corona-chan's website](https://islaterm-corona-chan.herokuapp.com).
   */
  fun run() {
    runBlocking {
      coroutineScope {
        spiders.forEach {
          launch { it.scrape() }
        }
      }
    }
    spiders.forEach { it.generatePlots() }
    syncOutput()
  }

  private fun syncOutput() {
    val runtime = Runtime.getRuntime()
    val os = System.getProperty("os.name")
    val process = when {
      os.contains("win", true) -> runtime.exec("powershell.exe .\\git-sync.ps1")
      else -> runtime.exec("sh git-sync.sh")
    }
    val stdThread = Thread {
      BufferedReader(InputStreamReader(process.inputStream)).lines()
        .forEach { logger.info(it) }
    }
    val errThread = Thread {
      BufferedReader(InputStreamReader(process.errorStream)).lines()
        .forEach { logger.info(it) }
    }
    stdThread.start()
    errThread.start()
  }
}