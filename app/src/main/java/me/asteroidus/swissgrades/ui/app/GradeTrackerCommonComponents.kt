package me.asteroidus.swissgrades.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val strings = currentAppStrings()
    ConfirmationDialog(
        title = title,
        message = message,
        confirmLabel = strings.deleteLabel,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
internal fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val strings = currentAppStrings()
    val accentBlue = appAccentBlue()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = appCardSurface(),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                modifier = Modifier.verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelLabel, color = accentBlue)
            }
        }
    )
}

@Composable
internal fun HeaderBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = currentAppStrings()
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = strings.backLabel
        )
    }
}

@Composable
internal fun HeaderActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

internal fun Modifier.blockEndToStartSwipeMotion(state: SwipeToDismissBoxState): Modifier {
    return offset {
        val swipeOffset = runCatching { state.requireOffset() }.getOrDefault(0f)
        IntOffset(
            x = if (swipeOffset < 0f) (-swipeOffset).roundToInt() else 0,
            y = 0
        )
    }
}
