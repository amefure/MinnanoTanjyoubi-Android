package com.amefure.minnanotanjyoubi.View.Fragment.Setting

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 設定画面リスト表示用のインターフェース */
interface SettingSectionItem

/** 設定画面リスト行データ */
data class SettingRowData(
    val icon: Painter,
    val title: String,
    val onClick: () -> Unit = { },
    val buttonText: String = "",
    val buttonOnClick: () -> Unit = { }
): SettingSectionItem

/** 設定画面リストヘッダー */
data class SettingSectionHeader(
    val title: String,
): SettingSectionItem

/** 設定画面リストフッター */
data class SettingSectionFooter(
    val title: String,
): SettingSectionItem

/** 設定画面リストスペーサー */
data class SettingSectionSpacer(
    val padding: Dp = 10.dp,
): SettingSectionItem

/** 設定画面イベントハンドラー */
interface SettingEventHandler {
    /** 戻る */
    fun onBack()

    /** 通知時間変更ボタン */
    fun onClickNotifyTimeButton()
    /** 通知日変更ボタン */
    fun onClickNotifyDayButton()
    /** 通知Msg変更 */
    fun onClickNotifyMessage()

    /** 広告視聴 */
    fun onClickAdsShow()
    /** アプリ内課金 */
    fun onClickInAppPurchase()

    /** よくある質問 */
    fun onClickFaq()
    /** お問い合わせ */
    fun onClickSendIssue()
    /** プライバシーポリシー */
    fun onClickPrivacyPolicy()

}
