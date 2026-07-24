package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.BorderCream
import com.osaparecidos.memoriamaker.ui.theme.BronzeBorder
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.GoldBg
import com.osaparecidos.memoriamaker.ui.theme.GoldBorder
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.MedalBronze
import com.osaparecidos.memoriamaker.ui.theme.MedalGold
import com.osaparecidos.memoriamaker.ui.theme.MedalSilver
import com.osaparecidos.memoriamaker.ui.theme.Muted
import com.osaparecidos.memoriamaker.ui.theme.NeutralMedal
import com.osaparecidos.memoriamaker.ui.theme.SilverBorder
import com.osaparecidos.memoriamaker.ui.theme.White

/** One leaderboard row, styled by rank (0=gold, 1=silver, 2=bronze, else neutral). */
@Composable
fun RankRow(
    rank: Int,
    name: String,
    detail: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val (rowBg, rowBorder, medalBg) = when (rank) {
        0 -> Triple(GoldBg, GoldBorder, MedalGold)
        1 -> Triple(White, SilverBorder, MedalSilver)
        2 -> Triple(White, BronzeBorder, MedalBronze)
        else -> Triple(White, BorderCream, NeutralMedal)
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(rowBg)
            .border(2.dp, rowBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(medalBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${rank + 1}",
                color = if (rank <= 2) InkNavy else Muted,
                fontFamily = Fredoka,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(name, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (detail.isNotEmpty()) {
                Text(detail, color = Muted, fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }
        }
        Text(value, color = InkNavy, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}
