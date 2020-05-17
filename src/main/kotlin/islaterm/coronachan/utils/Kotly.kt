package islaterm.coronachan.utils

import java.io.File

/**
 * Simple wrapper for Plot.ly's bar charts.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.1
 * @since 1.0
 */
class BarChart(private val title: String) {
  var xData = listOf<Any?>()
    set(value) {
      field = value.map { "'$it'" }
    }
  var yData = listOf<Number>()
  private val type = "bar"

  fun compile(): String {
    if (xData.size == yData.size) {
      return "const data = [\n" +
          "  {\n" +
          "    x: $xData,\n" +
          "    y: $yData,\n" +
          "    type: '$type'\n" +
          "  }\n" +
          "];\n\n" +
          "const id = document.getElementById('$title')\n" +
          "Plotly.newPlot(id, data);\n"
    }
    throw ArrayIndexOutOfBoundsException("X axis of size: ${xData.size} doesn't match with Y axis of size: ${yData.size}")
  }

  fun toHtml(): String {
    return File("src/main/resources/charts/plotly.html").readText().replace("~title~", title)
      .replace("~chart~", compile())
  }
}

fun main() {
  val chart = BarChart("Example")
  chart.xData = listOf("giraffes", "orangutans", "monkeys")
  chart.yData = listOf(20, 14, 23)
  File("test.html").writeText(chart.toHtml())
}
