package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.asteroidus.swissgrades.ui.theme.AppBackgroundDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveContainerDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveContainerLight
import me.asteroidus.swissgrades.ui.theme.AppPositiveDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveLight
import me.asteroidus.swissgrades.ui.theme.AppPositiveOnBlueDark
import me.asteroidus.swissgrades.ui.theme.AppPositiveOnBlueLight
import me.asteroidus.swissgrades.ui.theme.AppWarningContainerDark
import me.asteroidus.swissgrades.ui.theme.AppWarningContainerLight
import me.asteroidus.swissgrades.ui.theme.AppWarningDark
import me.asteroidus.swissgrades.ui.theme.AppWarningLight
import me.asteroidus.swissgrades.ui.theme.SwissBlueDark

internal val DashboardCardShape = RoundedCornerShape(24.dp)
internal val AppScreenHorizontalPadding = 16.dp
internal val AppScreenTopPadding = 16.dp
internal val AppScreenBottomPadding = 16.dp
internal val AppScreenListBottomPadding = 28.dp
internal const val MaxGradeAttachments = 5

@Composable
internal fun appAccentBlue(): Color = MaterialTheme.colorScheme.primary

@Composable
internal fun isDarkPalette(): Boolean = MaterialTheme.colorScheme.background == AppBackgroundDark

@Composable
internal fun appCardSurface(): Color = MaterialTheme.colorScheme.surface

@Composable
internal fun appCardBorderColor(): Color = MaterialTheme.colorScheme.outlineVariant

@Composable
internal fun appCardBorder(): BorderStroke = BorderStroke(1.dp, appCardBorderColor())

@Composable
internal fun appSoftAccentContainer(): Color = MaterialTheme.colorScheme.secondaryContainer

@Composable
internal fun appProgressTrack(): Color = if (isDarkPalette()) Color(0xFF304053) else Color(0xFFDCE4F2)

@Composable
internal fun appSelectedOptionContainer(): Color =
    if (isDarkPalette()) Color(0xFF16263B) else Color(0xFFF7FAFF)

@Composable
internal fun appSelectedOptionBorder(): Color = MaterialTheme.colorScheme.primary

@Composable
internal fun appIdleOptionBorder(): Color = appCardBorderColor()

@Composable
internal fun appIdleBadgeBackground(): Color =
    if (isDarkPalette()) Color(0xFF22344B) else Color(0xFFE1ECFF)

@Composable
internal fun appSelectedBadgeBackground(): Color = MaterialTheme.colorScheme.primary

@Composable
internal fun appIdleBadgeTint(): Color =
    if (isDarkPalette()) SwissBlueDark else Color(0xFF1459B2)

@Composable
internal fun appPositiveColor(): Color = if (isDarkPalette()) AppPositiveDark else AppPositiveLight

@Composable
internal fun appPositiveOnBlue(): Color = if (isDarkPalette()) AppPositiveOnBlueDark else AppPositiveOnBlueLight

@Composable
internal fun appPositiveBackground(): Color =
    if (isDarkPalette()) AppPositiveContainerDark else AppPositiveContainerLight

@Composable
internal fun appWarningColor(): Color = if (isDarkPalette()) AppWarningDark else AppWarningLight

@Composable
internal fun appWarningBackground(): Color =
    if (isDarkPalette()) AppWarningContainerDark else AppWarningContainerLight

@Composable
internal fun appNeutralBackground(): Color =
    if (isDarkPalette()) Color(0xFF223046) else Color(0xFFF1F5FB)

@Composable
internal fun appSwipeDeleteBackground(): Color =
    if (isDarkPalette()) Color(0xFF8A3F55) else Color(0xFFE85A7A)
