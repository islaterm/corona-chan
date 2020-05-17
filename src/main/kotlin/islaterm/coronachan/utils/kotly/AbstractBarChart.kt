package islaterm.coronachan.utils.kotly

import java.io.File

/**
 * Abstract class to hold the common functionalities of all bar charts.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version v1.0.4-b.1
 * @since 1.0
 */
abstract class AbstractBarChart(private val title: String) :
  IKotlyComponent {
  protected val traces = mutableListOf<BarTrace>()
  var xData = listOf<Any>()

  override fun compile(): String {
    var compiledTraces = ""
    for (trace in traces) {
      compiledTraces += trace.compile()
    }
    return "const data = [\n" +
        "${compiledTraces}\n" +
        "];\n" +
        "${Layout(title).compile()}\n" +
        "const config = {responsive: true}\n" +
        "const id = document.getElementById('$title')\n" +
        "Plotly.newPlot(id, data, layout, config);\n"
  }

  fun toHtml(): String {
    return File("src/main/resources/charts/plotly.html").readText()
      .replace("{~ title ~}", title)
      .replace("{~ chart ~}", compile())
  }
}