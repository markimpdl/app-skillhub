package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The chunky 3D surface shared by every screen: a solid fill sitting on a colored lip
 * ([shadow]) with no blur. Size comes from [content] (plus the lip height), so this composes
 * inside weighted rows, fillMaxWidth, or fixed sizes. On press the face sinks into the lip
 * (down by [shadowHeight]) and springs back on release.
 */
@Composable
fun Chunky3D(
    modifier: Modifier = Modifier,
    bg: Color = Color.White,
    borderColor: Color? = null,
    shadow: Color = Color(0x22000000),
    cornerRadius: Dp = 18.dp,
    shadowHeight: Dp = 5.dp,
    borderWidth: Dp = 3.dp,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    // how far the face has sunk into the lip: 0 at rest, shadowHeight while pressed
    val sink by animateDpAsState(
        targetValue = if (pressed && enabled) shadowHeight else 0.dp,
        animationSpec = spring(),
        label = "chunkySink",
    )

    Box(
        modifier
            .drawBehind {
                val sh = shadowHeight.toPx()
                val r = cornerRadius.toPx()
                val faceH = size.height - sh
                if (faceH > 0f) {
                    drawRoundRect(
                        color = shadow,
                        topLeft = Offset(0f, sh),
                        size = Size(size.width, faceH),
                        cornerRadius = CornerRadius(r, r),
                    )
                }
            }
            .padding(top = sink, bottom = shadowHeight - sink)
            .clip(shape)
            .background(bg)
            .then(if (borderColor != null) Modifier.border(borderWidth, borderColor, shape) else Modifier)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                ) else Modifier
            ),
        content = content,
    )
}
