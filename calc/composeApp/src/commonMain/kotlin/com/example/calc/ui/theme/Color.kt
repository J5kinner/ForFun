package com.example.calc.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val BrandPrimary = Color(0xFF3B6EF6)
private val BrandOnPrimary = Color(0xFFFFFFFF)
private val BrandAccent = Color(0xFFFF8A3D)

val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    secondary = BrandAccent,
    background = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EAF1),
    onBackground = Color(0xFF12141A),
    onSurface = Color(0xFF12141A),
)

val DarkColors = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    secondary = BrandAccent,
    background = Color(0xFF0B0C10),
    surface = Color(0xFF15171E),
    surfaceVariant = Color(0xFF23262F),
    onBackground = Color(0xFFF2F3F7),
    onSurface = Color(0xFFF2F3F7),
)
