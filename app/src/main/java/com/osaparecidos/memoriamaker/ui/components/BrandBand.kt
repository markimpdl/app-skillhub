package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.R
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Lavender
import com.osaparecidos.memoriamaker.ui.theme.Purple
import com.osaparecidos.memoriamaker.ui.theme.White

/**
 * The navy branded header band (ACTVET logo + SKILLS CAMP) used on Home and the Solo/Duel
 * selection screens. Full-bleed with a rounded bottom; pass [subtitle] = null to omit the tagline.
 */
@Composable
fun BrandBand(modifier: Modifier = Modifier, subtitle: String? = null, compact: Boolean = false) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = if (compact) 24.dp else 36.dp, bottomEnd = if (compact) 24.dp else 36.dp))
            .background(InkNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 24.dp, vertical = if (compact) 8.dp else 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_actvet),
            contentDescription = "ACTVET",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(if (compact) 0.42f else 0.65f),
        )
        Spacer(Modifier.height(if (compact) 4.dp else 10.dp))
        Row {
            val size = if (compact) 22.sp else 40.sp
            Text("SKILLS ", color = White, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = size)
            Text("CAMP", color = Purple, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = size)
        }
        if (subtitle != null && !compact) {
            Text(subtitle, color = Lavender, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}
