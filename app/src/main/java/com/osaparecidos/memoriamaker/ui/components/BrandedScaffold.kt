package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Cream
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy

/**
 * Screen frame with the Skills Camp brand band on top (kept on every menu/selection screen so
 * the content sits lower, within kids' reach), a scrollable middle, and a ← BACK bar pinned at
 * the bottom.
 */
@Composable
fun BrandedScaffold(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    scroll: Boolean = true,
    centerVertically: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().background(Cream)) {
        BrandBand()

        val middle = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)
        Column(
            modifier = if (scroll && !centerVertically) middle.verticalScroll(rememberScrollState()) else middle,
            verticalArrangement = if (centerVertically) Arrangement.Center else Arrangement.Top,
        ) {
            if (title != null) {
                Spacer(Modifier.height(12.dp))
                Text(title, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Spacer(Modifier.height(4.dp))
            }
            content()
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).windowInsetsPadding(WindowInsets.navigationBars)) {
            GameControls(onExit = onBack, exitLabel = "← BACK")
            Spacer(Modifier.height(12.dp))
        }
    }
}
