package com.amefure.minnanotanjyoubi.View.Fragment.Setting

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.amefure.minnanotanjyoubi.BuildConfig
import com.amefure.minnanotanjyoubi.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.amefure.minnanotanjyoubi.View.Compose.BackUpperBar
import com.amefure.minnanotanjyoubi.View.Compose.components.BannerAdView
import com.amefure.minnanotanjyoubi.View.Compose.components.CustomText
import com.amefure.minnanotanjyoubi.View.Compose.components.TextSize
import com.amefure.minnanotanjyoubi.ViewModel.SettingViewModel
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

/** 遷移ロジック */
private sealed class Route {
    data class Web(val url: String) : Route()
    data class View(val fragment: Fragment) : Route()
}

@AndroidEntryPoint
class SettingFragment : Fragment() {

    private val viewModel: SettingViewModel by viewModels()
    private var rewardedAd: RewardedAd? = null

    private val handlers = object : SettingEventHandler {
        override fun onBack() {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        override fun onClickFaq() {
            route(Route.View(FaqFragment()))
        }
        override fun onClickNotifyTimeButton() {
            val (hour, minute) = viewModel.fetchNotifyTime()
            // 取得した日付情報を元にダイアログを生成
            val dialog = TimePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Dialog,
                timeListener,
                hour,
                minute,
                false
            )
            dialog.show()
        }
        override fun onClickNotifyDayButton() {
            viewModel.switchNotifyDay()
        }

        override fun onClickNotifyMessage() {
            route(Route.View(InputNotifyMsgFragment()))
        }

        override fun onClickAdsShow() {
            if (viewModel.isShowAd()) {
                rewardedAd?.let { ad ->
                    ad.show(this@SettingFragment.requireActivity(), { _ ->
                        // 広告を視聴し終えたら容量の追加と視聴日を保存
                        viewModel.updateLimitCapacity()
                    })
                } ?: run {
                    Log.e("AdMob", "リワード広告がまだセットされていません。")
                }
            } else {
                AlertDialog
                    .Builder(this@SettingFragment.requireContext())
                    .setTitle("お知らせ")
                    .setMessage("広告を視聴できるのは1日に1回までです。")
                    .setPositiveButton("OK", { _, _ -> })
                    .show()
            }
        }
        override fun onClickInAppPurchase() {
            route(Route.View(InAppBillingFragment()))
        }

        override fun onClickSendIssue() {
            route(Route.Web("https://appdev-room.com/contact"))
        }

        override fun onClickPrivacyPolicy() {
            route(Route.Web("https://appdev-room.com/app-terms-of-service"))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // リワード広告の読み込み
        loadingRewordAds()

        return ComposeView(requireContext()).apply {
            setContent {
                SettingScreenRoot(handlers)
            }
        }
    }

    private fun loadingRewordAds() {
        val rewardId = if (BuildConfig.DEBUG) BuildConfig.ADMOB_REWARD_ID_TEST else BuildConfig.ADMOB_REWARD_ID_PROD
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this.requireContext(),
            rewardId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdMob", "ロード失敗")
                    rewardedAd = null
                    viewModel.updateAdsButtonState(false)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdMob", "ロード完了")
                    rewardedAd = ad
                    viewModel.updateAdsButtonState(true)
                }
            },
        )

        // これがないと視聴できない？
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {}
    }

    private fun route(to: Route) {
        when (to) {
            is Route.Web -> {
                val intent = Intent(Intent.ACTION_VIEW, to.url.toUri())
                startActivity(intent)
            }
            is Route.View -> {
                parentFragmentManager.beginTransaction().apply {
                    add(R.id.main_frame, to.fragment)
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }

    /** 日付ピッカーから選択された時に呼ばれるリスナー */
    private var timeListener =
        TimePickerDialog.OnTimeSetListener { view, hour, minutes ->
            viewModel.saveNotifyTime(
                hour = hour,
                minutes = minutes
            )
        }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}

@Composable
private fun SettingScreenRoot(
    handlers: SettingEventHandler,
    viewModel: SettingViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.fetchSetUpLocalData()
    }

    val settingItems = listOf(

        /** 通知設定セクション */
        SettingSectionHeader(
            title = stringResource(R.string.notify_setting_title)
        ),
        SettingRowData(
            icon = painterResource(R.drawable.ic_notify_clock),
            title = stringResource(R.string.notify_setting_label),
            buttonText = viewModel.notifyTime,
            buttonOnClick = handlers::onClickNotifyTimeButton
        ),
        SettingRowData(
            icon =  painterResource(R.drawable.ic_calendar),
            title = stringResource(R.string.notify_setting_label2),
            buttonText = viewModel.notifyDay,
            buttonOnClick = handlers::onClickNotifyDayButton
        ),
        SettingRowData(
            icon =  painterResource(R.drawable.ic_message),
            title = stringResource(R.string.notify_setting_label_notify_msg),
            onClick = handlers::onClickNotifyMessage
        ),
        SettingSectionFooter(
            title = stringResource(R.string.notify_desc_label1)
        ),

        SettingSectionSpacer(),

        /** 広告セクション */
        SettingSectionHeader(
            title = stringResource(R.string.ads_setting_title)
        ),
        SettingRowData(
            icon = painterResource(R.drawable.ic_person_add),
            title = stringResource(R.string.ads_setting_show_label),
            buttonText = viewModel.adsButtonText,
            buttonOnClick = handlers::onClickAdsShow
        ),
        SettingRowData(
            icon =  painterResource(R.drawable.ic_person),
            title = stringResource(R.string.ads_setting_capacity_label, viewModel.currentCapacity),
        ),
        SettingRowData(
            icon =  painterResource(R.drawable.ic_giftcard),
            title = stringResource(R.string.in_app_billing_title),
            onClick = handlers::onClickInAppPurchase
        ),
        SettingSectionFooter(
            title = stringResource(R.string.ads_setting_desc_label1)
        ),
        SettingSectionFooter(
            title = stringResource(R.string.ads_setting_desc_label2)
        ),

        SettingSectionSpacer(),

        /** その他セクション */
        SettingSectionHeader(
            title = stringResource(R.string.other_setting_title)
        ),
        SettingRowData(
            icon = painterResource(R.drawable.ic_question_mark),
            title = stringResource(R.string.other_setting_faq_label),
            onClick = handlers::onClickFaq,
        ),
        SettingRowData(
            icon = painterResource(R.drawable.ic_send),
            title = stringResource(R.string.other_setting_app_issue_label),
            onClick = handlers::onClickSendIssue,
        ),
        SettingRowData(
            icon = painterResource(R.drawable.ic_sticky_note),
            title = stringResource(R.string.other_setting_privacy_policy_label),
            onClick = handlers::onClickPrivacyPolicy,
        ),
        SettingSectionSpacer(),
    )

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.thema_gray_light)
        ) {
            SettingScreen(
                settingItems = settingItems,
                removeAds = viewModel.removeAds,
                onBack = handlers::onBack
            )
        }
    }
}


@Composable
private fun SettingScreen(
    settingItems: List<SettingSectionItem>,
    removeAds: Boolean,
    onBack: () -> Unit,
) {
    Column {

        BackUpperBar(onBack)

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .weight(1f)
        ) {
            itemsIndexed(settingItems) { index, item ->
                when (item) {
                    is SettingSectionHeader -> {
                        CustomText(
                            text = item.title,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    is SettingRowData -> {
                        SettingRowItem(
                            iconPainter = item.icon,
                            title = item.title,
                            onClick = item.onClick,
                            buttonText = item.buttonText,
                            buttonOnClick = item.buttonOnClick,
                            isFirst = settingItems.getOrNull(index - 1) !is SettingRowData,
                            isLast = settingItems.getOrNull(index + 1) !is SettingRowData
                        )
                    }
                    is SettingSectionFooter -> {
                        CustomText(
                            text = item.title,
                            color = Color.White,
                            maxLines = 2
                        )
                    }
                    is SettingSectionSpacer -> {
                        Spacer(
                            modifier = Modifier
                                .padding(vertical = item.padding)
                        )
                    }
                }
            }
        }

        if (!removeAds) {
            BannerAdView()
        }
    }
}

@Composable
private fun SettingRowItem(
    iconPainter: Painter,
    title: String,
    onClick: () -> Unit,
    buttonText: String,
    buttonOnClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    Card(
        // 背景色
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.thema_gray_dark),
        ),
        shape = RoundedCornerShape(
            topStart = if (isFirst) 12.dp else 0.dp,
            topEnd = if (isFirst) 12.dp else 0.dp,
            bottomEnd = if (isLast) 12.dp else 0.dp,
            bottomStart = if (isLast) 12.dp else 0.dp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
        ) {

            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            Icon(
                painter = iconPainter,
                tint = colorResource(id = R.color.thema_orange),
                contentDescription = title,
            )

            Spacer(
                modifier = Modifier
                    .width(20.dp)
            )

            CustomText(
                text = title,
                color = Color.White
            )

            Spacer(
                modifier = Modifier
                    .weight(1f)
            )

            if (buttonText.isNotEmpty()) {
                Button(
                    onClick = buttonOnClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.thema_gray_dark),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .width(90.dp)
                        .height(40.dp)
                ) {
                    CustomText(
                        text = buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textSize = TextSize.S
                    )
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .width(90.dp)
                        .height(40.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.thema_gray_light)
        ) {

            val handlers = object : SettingEventHandler {
                override fun onBack() {}
                override fun onClickFaq() {}
                override fun onClickNotifyTimeButton() {}
                override fun onClickNotifyDayButton() {}
                override fun onClickNotifyMessage() {}
                override fun onClickSendIssue() {}
                override fun onClickPrivacyPolicy() {}
                override fun onClickAdsShow() {}
                override fun onClickInAppPurchase() {}
            }

            SettingScreenRoot(
                handlers = handlers
            )
        }
    }
}

