package com.amefure.minnanotanjyoubi.View.Fragment.Setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.View.Compose.BackUpperBar

/** 質問リスト */
private val faqList = listOf(
    FaqItem("通知が届かないのですがなぜですか？", "通知が届かない場合は、このアプリに対して通知が許可されていない可能性があります。\n端末の設定アプリから「アプリ」＞「みんなの誕生日」＞「通知」がONになっていることを確認してください。"),
    FaqItem("通知時間やメッセージを変更したのに反映されません。", "通知設定の変更内容は設定を変更後に通知登録した通知に反映されます。\n既にONになっている場合はお手数ですがON→OFF→ONと操作してください。"),
    FaqItem("登録できる人数は何人ですか？", "登録できる人数は制限されています。\nですが広告を視聴していただくことで追加することも可能です。"),
    FaqItem("○○な機能を追加してほしい", "設定画面の「不具合はこちら」をクリックしWebサイトのお問い合わせフォームからフィードバックをいただけるとできる限り対処いたします。\nしかしご要望に添えない可能性があることをご了承ください。"),
)


class FaqFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colorResource(id = R.color.thema_gray_light)
                    ) {
                        FaqScreen(
                            faqList,
                            onBack = {
                                parentFragmentManager.popBackStack()
                            }
                        )
                    }

                }
            }
        }
    }
}

/** 質問 & 回答保持用データクラス */
private data class FaqItem(val question: String, val answer: String)

@Composable
private fun FaqScreen(
    faqList: List<FaqItem>,
    onBack: () -> Unit
) {
    Column {

        BackUpperBar(onBack)

        // 各項目の展開状態を管理するためMapを作成
        val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            itemsIndexed(faqList) { index, faq ->
                Card(
                    // 背景色
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 12.dp else 0.dp,
                        topEnd = if (index == 0) 12.dp else 0.dp,
                        bottomEnd = if (index == faqList.size - 1) 12.dp else 0.dp,
                        bottomStart = if (index == faqList.size - 1) 12.dp else 0.dp,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable {
                            expandedStates[index] = !(expandedStates[index] ?: false)
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = faq.question,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = if (expandedStates[index] == true) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandedStates[index] == true) "閉じる" else "開く"
                            )
                        }

                        if (expandedStates[index] == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = faq.answer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = false)
@Composable
private fun FaqScreenPreView() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.thema_gray_light)
        ) {
            FaqScreen(
                faqList,
                onBack = {}
            )
        }
    }
}