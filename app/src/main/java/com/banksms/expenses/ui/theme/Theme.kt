package com.banksms.expenses.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F4C81),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E6FA),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF0E7490),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),
    surface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFFE2E8F0),
    background = Color(0xFFF1F5F9),
    error = Color(0xFFDC2626)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF0F4C81),
    onPrimaryContainer = Color(0xFFD6E6FA),
    secondary = Color(0xFF67E8F9),
    onSecondary = Color(0xFF00363D),
    surface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFF1E293B),
    background = Color(0xFF020617),
    error = Color(0xFFF87171)
)

@Composable
fun BankSmsExpensesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
