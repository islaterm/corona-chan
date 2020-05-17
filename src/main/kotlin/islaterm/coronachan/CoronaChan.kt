package islaterm.coronachan

import islaterm.coronachan.spiders.MinsalSpider
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
 * @version v1.0.2-rc.1
 * @since 1.0
 */
fun main() {
  CoronaChan().run()
}

/**
 * Concrete implementation of Corona-chan.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version v1.0.2-rc.1
 * @since 1.0
 */
class CoronaChan {
  private val logger by LoggerKun()
  private val spiders = listOf(MinsalSpider())

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