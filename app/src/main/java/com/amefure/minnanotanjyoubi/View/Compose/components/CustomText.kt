package com.amefure.minnanotanjyoubi.View.Compose.components

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/** テキストサイズ */
enum class TextSize {
    SS, S, MS, M, ML, L
}

/**
 * 通常のTextを使いやすくラップしたカスタムテキスト
 */
@Composable
fun CustomText(
    text: String,
    textSize: TextSize = TextSize.M,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = 1,
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier,
) {
    val fontSize = when (textSize) {
        TextSize.SS -> 12.sp
        TextSize.S -> 14.sp
        TextSize.MS -> 15.sp
        TextSize.M -> 17.sp
        TextSize.ML -> 18.sp
        TextSize.L -> 20.sp
    }

    Text(
        text = text,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        softWrap = true,
        style = TextStyle(
            fontSize = fontSize,
            color = color,
            fontWeight = fontWeight,
        ),
        modifier = modifier,
    )
}
