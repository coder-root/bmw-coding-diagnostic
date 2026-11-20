package com.punhanaghayev.bimmerpro.core.i18n

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

/**
 * Modular multi-language registry.
 * Translations live in dedicated files ([StringsEn], [StringsTr], [StringsAz], [StringsRu])
 * so new languages can be added cleanly without editing a monolithic file.
 */
object AppLocalization {

    private const val PREFS_NAME = "bmw_expert_pro_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_LANG_CONFIRMED = "app_language_confirmed"

    fun hasConfirmedLanguage(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_LANG_CONFIRMED, false)

    fun loadSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        return if (saved != null) AppLanguage.fromCode(saved) else AppLanguage.detectSystemLanguage()
    }

    fun saveLanguage(context: Context, language: AppLanguage, markConfirmed: Boolean = true) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply {
                if (markConfirmed) putBoolean(KEY_LANG_CONFIRMED, true)
            }
            .apply()
    }

    fun stringsFor(language: AppLanguage): AppStrings = when (language) {
        AppLanguage.EN -> StringsEn.build()
        AppLanguage.TR -> StringsTr.build()
        AppLanguage.AZ -> StringsAz.build()
        AppLanguage.RU -> StringsRu.build()
    }
}

val LocalAppStrings = compositionLocalOf { AppLocalization.stringsFor(AppLanguage.EN) }
