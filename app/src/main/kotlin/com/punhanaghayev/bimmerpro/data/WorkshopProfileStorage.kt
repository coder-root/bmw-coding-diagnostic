package com.punhanaghayev.bimmerpro.data

import android.content.Context

data class WorkshopProfile(
    val workshopName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val technicianName: String = ""
) {
    val isConfigured: Boolean
        get() = workshopName.isNotBlank() || technicianName.isNotBlank()

    fun headerLine(): String = buildList {
        if (workshopName.isNotBlank()) add(workshopName.trim())
        if (phoneNumber.isNotBlank()) add(phoneNumber.trim())
        if (address.isNotBlank()) add(address.trim())
    }.joinToString("  •  ")

    fun footerSignature(): String = buildList {
        if (workshopName.isNotBlank()) add(workshopName.trim())
        if (technicianName.isNotBlank()) add("Tech: ${technicianName.trim()}")
    }.joinToString("  |  ")
}

class WorkshopProfileStorage(context: Context) {
    private val prefs = context.getSharedPreferences("bimmerpro_workshop", Context.MODE_PRIVATE)

    fun load(): WorkshopProfile = WorkshopProfile(
        workshopName = prefs.getString(KEY_NAME, "").orEmpty(),
        phoneNumber = prefs.getString(KEY_PHONE, "").orEmpty(),
        address = prefs.getString(KEY_ADDRESS, "").orEmpty(),
        technicianName = prefs.getString(KEY_TECH, "").orEmpty()
    )

    fun save(profile: WorkshopProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.workshopName.trim())
            .putString(KEY_PHONE, profile.phoneNumber.trim())
            .putString(KEY_ADDRESS, profile.address.trim())
            .putString(KEY_TECH, profile.technicianName.trim())
            .apply()
    }

    companion object {
        private const val KEY_NAME = "workshop_name"
        private const val KEY_PHONE = "workshop_phone"
        private const val KEY_ADDRESS = "workshop_address"
        private const val KEY_TECH = "workshop_technician"
    }
}
