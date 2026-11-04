package me.asteroidus.swissgrades.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Base = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal)
val Typography = Typography(
    headlineLarge = Base.copy(fontSize = 28.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp),
    headlineMedium = Base.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium),
    headlineSmall = Base.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    titleLarge = Base.copy(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium),
    titleMedium = Base.copy(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
    titleSmall = Base.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = Base.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = Base.copy(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = Base.copy(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = Base.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = Base.copy(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
    labelSmall = Base.copy(fontSize = 11.sp, lineHeight = 16.sp)
)
