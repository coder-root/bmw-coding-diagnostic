package com.punhanaghayev.bimmerpro.core.bmw

/**
 * Maps BMW FA/VO option codes to the affected ECU modules that must be VO-coded (E-Sys logic).
 */
object VoDependencyMapper {

    private val optionToEcus: Map<String, List<Pair<Int, String>>> = mapOf(
        "2TB" to listOf(EcuAddress.EGS to "EGS", EcuAddress.DSC to "DSC/ICM", EcuAddress.KOMBI to "KOMBI"),
        "6NS" to listOf(EcuAddress.HU_MEDIA to "HU_NBT/MGU", EcuAddress.FEM_CAS to "CAS4/BDC"),
        "6WB" to listOf(EcuAddress.KOMBI to "KOMBI", EcuAddress.HU_MEDIA to "HU_NBT/MGU"),
        "5AS" to listOf(EcuAddress.KOMBI to "KOMBI", EcuAddress.DSC to "DSC", EcuAddress.HU_MEDIA to "HU_NBT/MGU"),
        "5AT" to listOf(EcuAddress.KOMBI to "KOMBI", EcuAddress.DSC to "DSC", EcuAddress.HU_MEDIA to "HU_MGU"),
        "552" to listOf(EcuAddress.FEM_CAS to "FEM/BDC", EcuAddress.ZGW_BDC to "ZGW"),
        "3AG" to listOf(EcuAddress.HU_MEDIA to "HU_NBT/MGU", EcuAddress.FEM_CAS to "FEM/BDC")
    )

    fun affectedEcusFor(voCode: String): List<Pair<Int, String>> {
        val clean = voCode.trim().uppercase().removePrefix("S")
        return optionToEcus[clean] ?: listOf(EcuAddress.FEM_CAS to "CAS4/BDC", EcuAddress.HU_MEDIA to "HU")
    }
}
