package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun GradeImpactCard(impact: GradeImpactUiState) {
    val strings = currentAppStrings()
    val delta = impact.officialAverageDelta
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grade-impact-card"),
        shape = RoundedCornerShape(20.dp),
        border = appCardBorder(),
        colors = CardDefaults.outlinedCardColors(containerColor = appCardSurface())
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = strings.gradeImpactTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            GradeImpactRow(
                label = strings.gradeImpactWithGrade,
                value = formatImpactAverage(impact.withGradeAverage),
                valueTag = "grade-impact-with"
            )
            if (impact.withoutGradeAverage == null) {
                Text(
                    text = strings.gradeImpactUnavailable,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("grade-impact-unavailable")
                )
            } else {
                GradeImpactRow(
                    label = strings.gradeImpactWithoutGrade,
                    value = formatImpactAverage(impact.withoutGradeAverage),
                    valueTag = "grade-impact-without"
                )
                GradeImpactRow(
                    label = strings.gradeImpactDelta,
                    value = formatSignedImpact(requireNotNull(delta)),
                    valueColor = when {
                        delta > 0.0 -> appAccentBlue()
                        delta < 0.0 -> appWarningColor()
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    valueTag = "grade-impact-delta"
                )
            }
        }
    }
}

@Composable
private fun GradeImpactRow(
    label: String,
    value: String,
    valueTag: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier
                .padding(start = 12.dp)
                .testTag(valueTag)
        )
    }
}

private fun formatImpactAverage(value: Double): String {
    return if (value % 1.0 == 0.0) {
        "%.1f".format(Locale.US, value)
    } else {
        "%.2f".format(Locale.US, value).trimEnd('0')
    }
}

private fun formatSignedImpact(value: Double): String {
    val normalized = if (abs(value) < 1e-9) 0.0 else value
    val prefix = if (normalized > 0.0) "+" else ""
    return prefix + formatImpactAverage(normalized)
}
