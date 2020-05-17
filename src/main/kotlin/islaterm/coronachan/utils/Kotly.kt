package islaterm.coronachan.utils

import java.io.File

/**
 * Common interface that defines the common behaviour of all the elements of a chart.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.2
 * @since 1.0
 */
interface IKotlyComponent {
  /**
   * Returns the component as a PlotlyJS script.
   */
  fun compile(): String
}

/**
 * Simple wrapper for Plot.ly's bar charts.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.2
 * @since 1.0
 */
class BarChart(private val title: String) : IKotlyComponent {
  private val trace = BarTrace("data")
  var yData
    get() = trace.yData
    set(value) {
      trace.yData = value
    }
  var xData
    get() = trace.xData
    set(value) {
      trace.xData = value
    }

  /**
   * Returns the chart as a PlotlyJS script.
   */
  override fun compile(): String {
    if (xData.size == yData.size) {
      return "${trace.compile()}\n" +
          "${Layout(title).compile()}\n" +
          "const id = document.getElementById('$title')\n" +
          "Plotly.newPlot(id, data, layout);\n"
    }
    throw ArrayIndexOutOfBoundsException(
      "X axis of size: ${xData.size} doesn't match with Y axis of size: ${yData.size}"
    )
  }

  fun toHtml(): String {
    return File("src/main/resources/charts/plotly.html").readText()
      .replace("{~ title ~}", title)
      .replace("{~ chart ~}", compile())
  }
}

/**
 * This class represents a bar trace.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.2
 * @since 1.0
 */
class BarTrace(private val id: String) : IKotlyComponent {
  var xData = listOf<Any?>()
    set(value) {
      field = value.map { "'$it'" }
    }
  var yData = listOf<Number>()

  override fun compile(): String {
    if (xData.size == yData.size) {
      return "const $id = [\n" +
          "  {\n" +
          "    x: $xData,\n" +
          "    y: $yData,\n" +
          "    type: 'bar'\n" +
          "  }\n" +
          "];\n"
    }
    throw ArrayIndexOutOfBoundsException("X axis of size: ${xData.size} doesn't match with Y axis of size: ${yData.size}")
  }
}

/**
 * Class that represents the layout of a chart.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.2
 * @since 1.0
 */
data class Layout(val title: String? = null) : IKotlyComponent {
  override fun compile() = "const layout = {\n" +
      "  ${if (title != null) {
        "title: '$title'"
      } else ""}\n" +
      "}\n"
}

fun main() {
  val chart = BarChart("Example")
  chart.xData = listOf("giraffes", "orangutans", "monkeys")
  chart.yData = listOf(20, 14, 23)
  File("test.html").writeText(chart.toHtml())
}
