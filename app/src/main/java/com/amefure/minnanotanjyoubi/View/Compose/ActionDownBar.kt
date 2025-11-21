package com.amefure.minnanotanjyoubi.View.Compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amefure.minnanotanjyoubi.R


/**
 *ActionDownBar
 */
@Composable
fun ActionDownBar(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 35.dp)
            .padding(end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 左側の棒
        Box(
            modifier = Modifier
                .weight(1f) // 余白スペースに広がるように拡大
                .height(50.dp)
                .background(
                    color = colorResource(id = R.color.thema_gray_dark),
                    shape = RoundedCornerShape(
                        topEnd = 50.dp,
                        bottomEnd = 50.dp
                    )
                )
        )

        // 戻るボタン
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(50.dp)
                .background(
                    shape = CircleShape,
                    color = colorResource(id = R.color.thema_gray_dark),
                )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(id = R.string.icon_back),
                tint = Color.White,
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun ActionDownBarPreView() {
    MaterialTheme {
        ActionDownBar(
            {}
        )
    }
}
