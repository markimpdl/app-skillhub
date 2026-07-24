package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Lavender
import com.osaparecidos.memoriamaker.ui.theme.White

/**
 * The stats HUD shown at the top of every game (display only — no reaching required).
 * The interactive RESTART / EXIT controls live in [GameControls] at the bottom of the screen,
 * within reach of small kids on the vertical totem.
 */
@Composable
fun GameHud(
    primaryLabel: String,
    primaryValue: String,
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatPill(
            label = primaryLabel,
            value = primaryValue,
            bg = InkNavy,
            labelColor = Lavender,
            valueColor = White,
        )
        stats.forEach { (label, value) ->
            StatPill(label = label, value = value)
        }
    }
}
