package islaterm.coronachan.utils

import java.io.File

const val resources = "./src/main/resources"
const val coronaWebSrc = "../../corona-chan/src"
const val componentsDir = "components"
const val minsalVue = "$componentsDir/Minsal.vue"
const val minsalDir = "$componentsDir/minsal"
const val infectionsVue = "$minsalDir/Infections.vue"
const val quarantinesVue = "$minsalDir/Quarantines.vue"

/**
 * Applies a template to a Vue component file.
 *
 * The function search for occurrences of `{~ pattern ~}` and replaces it with the given ``with`` string.
 */
fun applyTemplate(componentFile: String, pattern: String, with: String) {
  replaceInDocument(componentFile, "{~ $pattern ~}", with)
}

/**
 * Sets a Vue ``component`` file to the base template for that file.
 */
fun initializeComponent(filename: String) {
  replaceInDocument(filename, "<script>(?s).*</script>", "$resources/$filename")
}

/**
 * Replaces all the occurrences of ``pattern`` in the given file with the ``with`` string.
 */
private fun replaceInDocument(filename: String, pattern: String, with: String) {
  File("$coronaWebSrc/$filename").writeText(
    with.replace(pattern.toRegex(), with)
  )
}

/**
 * Simple interface representing a record of data for a specific day.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5
 * @since 1.0
 */
interface IDayRecord {
  val day: String
}