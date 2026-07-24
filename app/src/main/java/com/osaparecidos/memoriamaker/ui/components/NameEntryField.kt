package com.osaparecidos.memoriamaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osaparecidos.memoriamaker.ui.theme.Faint
import com.osaparecidos.memoriamaker.ui.theme.Fredoka
import com.osaparecidos.memoriamaker.ui.theme.InkNavy
import com.osaparecidos.memoriamaker.ui.theme.Purple
import com.osaparecidos.memoriamaker.ui.theme.White

/** Uppercase name field, capped at [maxChars], with a purple caret. */
@Composable
fun NameEntryField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxChars: Int = 10,
    hint: String = "NAME",
) {
    val style = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = InkNavy,
        textAlign = TextAlign.Center,
    )
    BasicTextField(
        value = value,
        onValueChange = { raw -> onValueChange(raw.uppercase().take(maxChars)) },
        modifier = modifier,
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(Purple),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (value.isEmpty()) {
                    Text(hint, style = style.copy(color = Faint))
                }
                inner()
            }
        },
    )
}
