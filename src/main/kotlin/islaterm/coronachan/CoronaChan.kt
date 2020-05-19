package islaterm.coronachan

import islaterm.coronachan.spiders.minsal.InfectionsSpider
import islaterm.coronachan.spiders.minsal.QuarantineSpider
import islaterm.coronachan.utils.LoggerKun
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.schedule


const val resources = ".\\src\\main\\resources"
val vueTemplate by lazy { File("$resources\\template.vue") }
val coronaChanVue by lazy { File("..\\..\\corona-chan\\src\\components\\CoronaChan.vue") }

/**
 * Corona-chan is a high-school girl who likes to play with spiders.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version v1.0.5-b.8
 * @since 1.0
 */
fun main() {
  val coronaChan = CoronaChan()
  coronaChan.run()
  val now = LocalDateTime.now()
  val updateTime = now.withHour(12).withMinute(15)
  Timer().schedule(
    Date.from(
      updateTime.plusDays(if (now > updateTime) 1 else 0)
        .withHour(12)
        .withMinute(15)
        .atZone(ZoneId.systemDefault())
        .toInstant()
    ), 86_400_000
  ) { coronaChan.run() }
}


/**
 * Corona-chan missed her alarm and is late to school!
 * In the hurry to get ready she released all of her [spiders] >.<
 *
 * While she was [run]ning to school with a toast in her mouth she met [LoggerKun] who was also late.
 * Are they going to be able to get to school on time or are they going to get grounded?
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version v1.0.5-b.8
 * @since 1.0
 */
class CoronaChan {
  private val logger by LoggerKun()
  private val queryDay: LocalDate
    get() {
      val now = LocalDateTime.now()
      return if (now < now.withHour(12).withMinute(15))
        LocalDate.now().minusDays(1)
      else
        LocalDate.now()
    }
  private val spiders = listOf(InfectionsSpider(queryDay), QuarantineSpider(queryDay))

  /**
   * Runs all of corona's spiders in parallel to get information about COVID-19 and then syncs the retrieved data with
   * [corona-chan's website](https://islaterm-corona-chan.herokuapp.com).
   */
  fun run() {
    coronaChanVue.writeText(
      coronaChanVue.readText().replace(Pattern.compile("<script>(?s).*</script>").toRegex(), vueTemplate.readText())
    )
    runBlocking {
      coroutineScope {
        spiders.forEach {
          launch { it.scrape() }
        }
      }
    }
    spiders.forEach { it.generateDocuments() }
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