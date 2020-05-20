package islaterm.coronachan.utils

class InfectionTables {
  val totalInfections = "Casos totales acumulados"
  val newInfections = "Casos nuevos totales"
  val newWithSymptoms = "Casos nuevos con síntomas"
  val newWithoutSymptoms = "Casos nuevos sin síntomas*"
  val deceased = "Fallecidos"
  val percentage = "% Total"
}

data class InfectionRecord(
  override val day: String,
  val place: String,
  val totalInfections: Int,
  val newInfections: Int,
  val newWithSymptoms: Int,
  val newWithoutSymptoms: Int,
  val deceased: Int,
  val percentage: Double
) : IDayRecord {
  override fun toString() =
    "$place, $totalInfections, $newInfections, $newWithSymptoms, $newWithoutSymptoms, $deceased, $percentage"
}