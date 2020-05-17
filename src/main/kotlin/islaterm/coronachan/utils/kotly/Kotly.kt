package islaterm.coronachan.utils.kotly

import java.io.File

/**
 * Common interface that defines the common behaviour of all the elements of a chart.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.3
 * @since 1.0
 */
interface IKotlyComponent {
  /**
   * Returns the component as a PlotlyJS script.
   */
  fun compile(): String
}

/**
 * Class that represents the layout of a chart.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.2-b.3
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
