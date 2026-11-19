package com.punhanaghayev.bimmerpro.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color

enum class AppThemeStyle(
    val id: String,
    val displayName: String,
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val card: Color,
    val cardElevated: Color,
    val outline: Color,
    val primary: Color,
    val primaryBright: Color,
    val accentRed: Color,
    val textPrimary: Color,
    val textSecondary: Color
) {
    M_CARBON(
        id = "m_carbon",
        displayName = "M Motorsport Carbon",
        isDark = true,
        bg = Color(0xFF0A0E15),
        surface = Color(0xFF111722),
        card = Color(0xFF17202F),
        cardElevated = Color(0xFF1E293B),
        outline = Color(0xFF2B394F),
        primary = Color(0xFF0066B1),
        primaryBright = Color(0xFF38BDF8),
        accentRed = Color(0xFFE21A22),
        textPrimary = Color(0xFFF1F5F9),
        textSecondary = Color(0xFF94A3B8)
    ),
    ALPINA_CLASSIC(
        id = "alpina",
        displayName = "Alpina Emerald & Gold",
        isDark = true,
        bg = Color(0xFF071311),
        surface = Color(0xFF0D1F1B),
        card = Color(0xFF132A25),
        cardElevated = Color(0xFF1A3831),
        outline = Color(0xFF264E45),
        primary = Color(0xFF059669),
        primaryBright = Color(0xFF34D399),
        accentRed = Color(0xFFFBBF24),
        textPrimary = Color(0xFFECFDF5),
        textSecondary = Color(0xFF9CA3AF)
    ),
    IPERFORMANCE_CYAN(
        id = "iperformance",
        displayName = "iPerformance Electric",
        isDark = true,
        bg = Color(0xFF090D1A),
        surface = Color(0xFF10172A),
        card = Color(0xFF162038),
        cardElevated = Color(0xFF1E2B4A),
        outline = Color(0xFF2D3F6B),
        primary = Color(0xFF0284C7),
        primaryBright = Color(0xFF22D3EE),
        accentRed = Color(0xFF818CF8),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFF94A3B8)
    ),
    M_AMBER_TRACK(
        id = "m_amber",
        displayName = "M Performance Amber",
        isDark = true,
        bg = Color(0xFF120C09),
        surface = Color(0xFF1B130E),
        card = Color(0xFF261B14),
        cardElevated = Color(0xFF33241B),
        outline = Color(0xFF4A3528),
        primary = Color(0xFFEA580C),
        primaryBright = Color(0xFFFB923C),
        accentRed = Color(0xFFEF4444),
        textPrimary = Color(0xFFFFF7ED),
        textSecondary = Color(0xFFA8A29E)
    ),
    DAYLIGHT_SILVER(
        id = "daylight",
        displayName = "BMW Daylight Silver",
        isDark = false,
        bg = Color(0xFFF1F5F9),
        surface = Color(0xFFFFFFFF),
        card = Color(0xFFFFFFFF),
        cardElevated = Color(0xFFE2E8F0),
        outline = Color(0xFFCBD5E1),
        primary = Color(0xFF0066B1),
        primaryBright = Color(0xFF0284C7),
        accentRed = Color(0xFFE21A22),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF475569)
    );

    companion object {
        private const val PREFS_NAME = "bmw_expert_pro_prefs"
        private const val KEY_THEME = "app_theme_style"

        fun fromId(id: String?): AppThemeStyle =
            entries.firstOrNull { it.id == id } ?: M_CARBON

        fun loadSaved(context: Context): AppThemeStyle {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return fromId(prefs.getString(KEY_THEME, M_CARBON.id))
        }

        fun save(context: Context, style: AppThemeStyle) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, style.id)
                .apply()
        }
    }
}
