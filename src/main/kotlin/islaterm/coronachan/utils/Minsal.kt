package islaterm.coronachan.utils

@Suppress("SpellCheckingInspection")
data class InfectionTables (
  val totalInfections: String = "Casos totales acumulados",
  val newInfections: String = "Casos nuevos totales",
  val newWithSymptoms: String = "Casos nuevos con síntomas",
  val newWithoutSymptoms: String = "Casos nuevos sin síntomas*",
  val deceased: String = "Fallecidos",
  val percentage: String = "% Total"
)

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

data class QuarantineRecord(override val day: String, val zone: String) :
  IDayRecord {
  override fun equals(other: Any?) = other is QuarantineRecord && zone == other.zone
  override fun hashCode() = zone.hashCode()
  override fun toString() = zone
}