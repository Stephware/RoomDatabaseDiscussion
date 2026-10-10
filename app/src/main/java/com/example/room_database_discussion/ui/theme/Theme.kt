
package com.example.room_database_discussion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudentColors = lightColorScheme(
    primary = Color(0xFF2554A4),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFEAF2FF),
    onPrimaryContainer = Color(0xFF173D78),

    secondary = Color(0xFF2554A4),
    onSecondary = Color.White,

    background = Color(0xFFF8FAFF),
    onBackground = Color(0xFF14253B),

    surface = Color.White,
    onSurface = Color(0xFF14253B),

    surfaceVariant = Color(0xFFF2F7FF),
    onSurfaceVariant = Color(0xFF425B77),

    outline = Color(0xFFD1DFF0),
    outlineVariant = Color(0xFFE6EFFA),

    error = Color(0xFFB42318),
    onError = Color.White
)

@Composable
fun Room_database_discussionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudentColors,
        typography = Typography,
        content = content
    )
}
