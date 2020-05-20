package islaterm.coronachan.utils.kotly

import java.io.File

/**
 * Simple wrapper for Plot.ly's bar charts.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.3
 * @since 1.0
 */
class BarChart(title: String) : AbstractBarChart(title) {
  private val trace = traces[0]
  var yData
    get() = trace.yData
    set(value) {
      trace.yData = value
    }
}

/**
 * Class that represents a grouped bar chart.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-rc.2
 * @since 1.0
 */
class GroupedBarChart(title: String) : AbstractBarChart(title) {
  /**
   * Adds a new group of data to the y axis.
   */
  fun addData(yData: List<Number>, name: String): GroupedBarChart {
    val trace = BarTrace("${traces.size}", name)
    trace.xData = xData
    trace.yData = yData
    traces.add(trace)
    return this
  }
}

/**
 * This class represents a bar trace.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.3
 * @since 1.0
 */
class BarTrace(id: String, private val name: String) : IKotlyComponent {
  private val id = "trace$id"
  var xData = listOf<Any?>()
    set(value) {
      field = value.map { "'$it'" }
    }
  var yData = listOf<Number>()

  override fun compile(): String {
    if (xData.size == yData.size) {
      return "  {\n" +
          "    x: $xData,\n" +
          "    y: $yData,\n" +
          "    type: 'bar',\n" +
          "    name: '$name',\n" +
          "  },\n"
    }
    throw ArrayIndexOutOfBoundsException("X axis of size: ${xData.size} doesn't match with Y axis of size: ${yData.size}")
  }

  override fun toString() = id
}

fun main() {
  val chart = GroupedBarChart("test")
  chart.xData = listOf("giraffes", "orangutans", "monkeys")
  chart.addData(listOf(20, 14, 23), "SF Zoo")
  chart.addData(listOf(12, 18, 29), "LA Zoo")
  chart.compile()
  File("test.html").writeText(chart.toHtml())
}