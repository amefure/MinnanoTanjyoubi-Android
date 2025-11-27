package com.amefure.minnanotanjyoubi.View.Fragment.Setting

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.BuildConfig
import com.amefure.minnanotanjyoubi.Manager.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Model.Capacity
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.databinding.FragmentSettingBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import com.amefure.minnanotanjyoubi.View.Compose.BackUpperBar
import com.amefure.minnanotanjyoubi.View.Compose.components.CustomText
import com.amefure.minnanotanjyoubi.View.Compose.components.TextSize

class SettingFragment : Fragment() {

    private lateinit var dataStoreManager: DataStoreManager
    private val calcDateInfoManager = CalcDateInfoManager()

    private var rewardedAd: RewardedAd? = null

    // ローカルデータ格納用
    private var notifyTime: String? = null
    private var limitCapacity: Int = Capacity.initialCapacity
    private var lastDate: String = ""

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataStoreManager = DataStoreManager(this.requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                val handlers = object : SettingEventHandler {
                    override fun onBack() {
                        showOffKeyboard()
                        parentFragmentManager.popBackStack()
                    }

                    override fun onClickFaq() {
                        parentFragmentManager.beginTransaction()
                            .add(R.id.main_frame, FaqFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    override fun onClickNotifyTimeButton() {
                        val hour: Int
                        val minute: Int
                        if (notifyTime != null) {
                            // 日付を選択済みの場合は初期値を変更
                            val parts = notifyTime!!.split(":")
                            hour = parts[0].toInt()
                            minute = parts[1].toInt()
                        } else {
                            // 現在の日付情報を取得
                            // 初期値として7:00を格納しているため実行されない予定
                            val c = Calendar.getInstance()
                            hour = c.get(Calendar.HOUR_OF_DAY)
                            minute = c.get(Calendar.MINUTE)
                        }
                        // 取得した日付情報を元にダイアログを生成
                        val dialog = TimePickerDialog(requireContext(), android.R.style.Theme_Holo_Dialog, timeListener, hour, minute, false)
                        dialog.show()
                    }
                    override fun onClickNotifyDayButton() {
                        if (binding.notifySettingDayButton.text == "当日") {
                            setUpNotifyDayAfter()
                            lifecycleScope.launch {
                                dataStoreManager.saveNotifyDay("前日")
                            }
                        } else {
                            setUpNotifyDay()
                            lifecycleScope.launch {
                                dataStoreManager.saveNotifyDay("当日")
                            }
                        }
                    }

                    override fun onClickNotifyMessage() {
                        val nextFragment = InputNotifyMsgFragment()
                        parentFragmentManager.beginTransaction().apply {
                            add(R.id.main_frame, nextFragment)
                            addToBackStack(null)
                            commit()
                        }
                    }

                    override fun onClickAdsShow() {
                        if (!calcDateInfoManager.isToday(lastDate)) {
                            rewardedAd?.let { ad ->
                                ad.show(this@SettingFragment.requireActivity(), { _ ->
                                    lifecycleScope.launch {
                                        // 広告を視聴し終えたら容量の追加と視聴日を保存
                                        val newCapacity = limitCapacity + Capacity.addCapacity
                                        dataStoreManager.saveLimitCapacity(newCapacity)
                                        dataStoreManager.saveLastAcquisitionDate(calcDateInfoManager.getTodayString())
                                    }
                                })
                            } ?: run {
                                Log.d("TAG", "リワード広告がまだセットされていません。")
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
                        val nextFragment = InAppBillingFragment()
                        parentFragmentManager.beginTransaction().apply {
                            add(R.id.main_frame, nextFragment)
                            addToBackStack(null)
                            commit()
                        }
                    }

                    override fun onClickSendIssue() {
                        val uri = "https://appdev-room.com/contact".toUri()
                        val intent = Intent(Intent.ACTION_VIEW,uri)
                        startActivity(intent)
                    }
                    override fun onClickPrivacyPolicy() {
                        val uri = "https://appdev-room.com/app-terms-of-service".toUri()
                        val intent = Intent(Intent.ACTION_VIEW,uri)
                        startActivity(intent)
                    }
                }

                SettingScreenRoot(handlers)
            }
        }
    }

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View {
//        _binding = FragmentSettingBinding.inflate(inflater, container, false)
//
//        val rewardId = if (BuildConfig.DEBUG) BuildConfig.ADMOB_REWARD_ID_TEST else BuildConfig.ADMOB_REWARD_ID_PROD
//        val adRequest = AdRequest.Builder().build()
//        RewardedAd.load(
//            this.requireContext(),
//            rewardId,
//            adRequest,
//            object : RewardedAdLoadCallback() {
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    Log.d("Admob", "ロード失敗")
//                    rewardedAd = null
//                    updateAdsButtonState()
//                }
//
//                override fun onAdLoaded(ad: RewardedAd) {
//                    Log.d("Admob", "ロード完了")
//                    rewardedAd = ad
//                    updateAdsButtonState()
//                }
//            },
//        )
//
//        // これがないと視聴できない？
//        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {}
//
//        dataStoreManager = DataStoreManager(this.requireContext())
//        return binding.root
//    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // バナーの追加と読み込み
        addAdBannerView()

        // ローカルに保存している情報を取得
        observeLocalData()

//        // 戻るボタン
//        binding.componentBackUpperContainer.backButton.setOnClickListener {
//            showOffKeyboard()
//            parentFragmentManager.popBackStack()
//        }

//        binding.notifySettingTimeButton.setOnClickListener {
//            val hour: Int
//            val minute: Int
//            if (notifyTime != null) {
//                // 日付を選択済みの場合は初期値を変更
//                val parts = notifyTime!!.split(":")
//                hour = parts[0].toInt()
//                minute = parts[1].toInt()
//            } else {
//                // 現在の日付情報を取得
//                // 初期値として7:00を格納しているため実行されない予定
//                val c = Calendar.getInstance()
//                hour = c.get(Calendar.HOUR_OF_DAY)
//                minute = c.get(Calendar.MINUTE)
//            }
//            // 取得した日付情報を元にダイアログを生成
//            val dialog = TimePickerDialog(requireContext(), android.R.style.Theme_Holo_Dialog, timeListener, hour, minute, false)
//            dialog.show()
//        }

//        binding.notifySettingDayButton.setOnClickListener {
//            if (binding.notifySettingDayButton.text == "当日") {
//                setUpNotifyDayAfter()
//                lifecycleScope.launch {
//                    dataStoreManager.saveNotifyDay("前日")
//                }
//            } else {
//                setUpNotifyDay()
//                lifecycleScope.launch {
//                    dataStoreManager.saveNotifyDay("当日")
//                }
//            }
//        }
//        binding.notifySettingMsgButton.setOnClickListener {
//            val nextFragment = InputNotifyMsgFragment()
//            parentFragmentManager.beginTransaction().apply {
//                add(R.id.main_frame, nextFragment)
//                addToBackStack(null)
//                commit()
//            }
//        }

//        binding.adsRewardPlayButton.setOnClickListener {
//            if (!calcDateInfoManager.isToday(lastDate)) {
//                rewardedAd?.let { ad ->
//                    ad.show(this.requireActivity(), { _ ->
//                        lifecycleScope.launch {
//                            // 広告を視聴し終えたら容量の追加と視聴日を保存
//                            val newCapacity = limitCapacity + Capacity.addCapacity
//                            dataStoreManager.saveLimitCapacity(newCapacity)
//                            dataStoreManager.saveLastAcquisitionDate(calcDateInfoManager.getTodayString())
//                        }
//                    })
//                } ?: run {
//                    Log.d("TAG", "リワード広告がまだセットされていません。")
//                }
//            } else {
//                AlertDialog
//                    .Builder(this.requireContext())
//                    .setTitle("お知らせ")
//                    .setMessage("広告を視聴できるのは1日に1回までです。")
//                    .setPositiveButton("OK", { _, _ -> })
//                    .show()
//            }
//        }

//        binding.inAppBillingLayout.setOnClickListener {
//            val nextFragment = InAppBillingFragment()
//            parentFragmentManager.beginTransaction().apply {
//                add(R.id.main_frame, nextFragment)
//                addToBackStack(null)
//                commit()
//            }
//        }

//        binding.faqLayout.setOnClickListener {
//            val nextFragment = FaqFragment()
//            parentFragmentManager.beginTransaction().apply {
//                add(R.id.main_frame, nextFragment)
//                addToBackStack(null)
//                commit()
//            }
//        }

//        binding.appIssueLayout.setOnClickListener {
//            val uri = "https://appdev-room.com/contact".toUri()
//            val intent = Intent(Intent.ACTION_VIEW,uri)
//            startActivity(intent)
//        }
//
//        binding.privacyPolicyLayout.setOnClickListener {
//            val uri = "https://appdev-room.com/app-terms-of-service".toUri()
//            val intent = Intent(Intent.ACTION_VIEW,uri)
//            startActivity(intent)
//        }
    }

    private fun updateAdsButtonState() {
        if (rewardedAd != null) {
            binding.adsRewardPlayButton.text = "追加"
            val colorValue = ContextCompat.getColorStateList(this.requireContext(), R.color.thema_yelow)
            binding.adsRewardPlayButton.backgroundTintList = colorValue
        } else {
            binding.adsRewardPlayButton.text = getString(R.string.ads_setting_loading)
            val colorValue = ContextCompat.getColorStateList(this.requireContext(), R.color.thema_gray_dark)
            binding.adsRewardPlayButton.backgroundTintList = colorValue
        }
    }

    private fun setUpNotifyDay() {
        binding.notifySettingDayButton.text = "当日"
        val colorValue = ContextCompat.getColorStateList(this.requireContext(), R.color.thema_red)
        binding.notifySettingDayButton.backgroundTintList = colorValue
    }

    private fun setUpNotifyDayAfter() {
        binding.notifySettingDayButton.text = "前日"
        val colorValue = ContextCompat.getColorStateList(this.requireContext(), R.color.thema_blue)
        binding.notifySettingDayButton.backgroundTintList = colorValue
    }

    /** 日付ピッカーから選択された時に呼ばれるリスナー */
    private var timeListener =
        TimePickerDialog.OnTimeSetListener { view, hour, minutes ->
            var minutesStr = minutes.toString()
            if (minutesStr.length == 1) {
                minutesStr = "0$minutesStr"
            }
            val time = "$hour:$minutesStr"
            lifecycleScope.launch {
                dataStoreManager.saveNotifyTime(time)
            }
        }

    /** ローカルの情報を観測 */
    private fun observeLocalData() {
        // lifecycleScope.launchはまとめると動作しないので分割
        lifecycleScope.launch {
            dataStoreManager.observeNotifyTime().collect {
                if (it != null) {
                    binding.notifySettingTimeButton.text = it
                    notifyTime = it
                } else {
                    // 初期値格納
                    dataStoreManager.saveNotifyTime(getString(R.string.notify_default_time))
                }
            }
        }
        lifecycleScope.launch {
            dataStoreManager.observeNotifyDay().collect {
                if (it != null) {
                    binding.notifySettingDayButton.text = it
                    if (it == getString(R.string.notify_default_day)) {
                        setUpNotifyDay()
                    } else {
                        setUpNotifyDayAfter()
                    }
                } else {
                    // 初期値格納
                    dataStoreManager.saveNotifyDay(getString(R.string.notify_default_day))
                }
            }
        }

        lifecycleScope.launch {
            dataStoreManager.observeLimitCapacity().collect {
                if (it != null) {
                    limitCapacity = it
                    binding.adsSettingCapacityLabel.text = requireContext().getString(R.string.ads_setting_capacity_label, it)
                } else {
                    // 初期値格納
                    dataStoreManager.saveLimitCapacity(Capacity.initialCapacity)
                }
            }
        }

        lifecycleScope.launch {
            dataStoreManager.observeLastAcquisitionDate().collect {
                if (it != null) {
                    lastDate = it
                } else {
                    // 初期値格納
                    dataStoreManager.saveLastAcquisitionDate("")
                }
            }
        }

        lifecycleScope.launch {
            dataStoreManager.observeInAppUnlockStorage().collect {
                if (it) {
                    binding.adsSettingShowReward.visibility = View.GONE
                    binding.adsSettingCapacityLabel.text = "現在の容量：容量解放済み"
                }
            }
        }
    }

    /**
     *  AdMob バナー広告の追加と読み込み
     *  adUnitIdを動的に変更するためにはViewをコードで追加する必要がある
     */
    private fun addAdBannerView() {
        lifecycleScope.launch {
            // 広告削除購入済みなら追加しない
            dataStoreManager.observeInAppRemoveAds().collect {
                if (!it) {
                    // 全てのビューをリセット
                    // アプリ初回インストール時のみ、通知許可ダイアログがらみ?で2回呼ばれるため
                    binding.adViewLayout.removeAllViewsInLayout()
                    // AdViewを生成して設定
                    val adView =
                        AdView(this@SettingFragment.requireContext()).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId =
                                if (BuildConfig.DEBUG) {
                                    BuildConfig.ADMOB_BANNER_ID_TEST
                                } else {
                                    BuildConfig.ADMOB_BANNER_ID_PROD
                                }
                            // 広告の読み込み
                            loadAd(AdRequest.Builder().build())
                        }

                    // レイアウトパラメータを指定（横幅 match_parent、高さ wrap_content）
                    val layoutParams =
                        LinearLayout
                            .LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            ).apply {
                                gravity = Gravity.CENTER_HORIZONTAL
                            }

                    adView.layoutParams = layoutParams

                    // adViewLayout に AdView を追加
                    binding.adViewLayout.addView(adView)
                } else {
                    // 広告削除フラグがONなら既に表示している場合もあるので削除
                    binding.adViewLayout.removeAllViewsInLayout()
                }
            }
        }
    }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}

@Composable
private fun SettingScreenRoot(
    handlers: SettingEventHandler,
) {

    val settingItems = listOf(

        /** 通知設定セクション */
        SettingSectionHeader(
            title = stringResource(R.string.notify_setting_title)
        ),
        SettingRowData(
            icon = painterResource(R.drawable.ic_notify_clock),
            title = stringResource(R.string.notify_setting_label),
            buttonText = "7:00",
            buttonOnClick = handlers::onClickNotifyTimeButton
        ),
        SettingRowData(
            icon =  painterResource(R.drawable.ic_calendar),
            title = stringResource(R.string.notify_setting_label2),
            buttonText = stringResource(R.string.notify_default_day),
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
            buttonText = stringResource(R.string.ads_setting_loading),
            buttonOnClick = handlers::onClickAdsShow
        ),
        SettingRowData(
            icon =  painterResource(R.drawable.ic_person),
            title = stringResource(R.string.ads_setting_capacity_label),
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
    )

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.thema_gray_light)
        ) {
            SettingScreen(
                settingItems = settingItems,
                onBack = handlers::onBack
            )
        }
    }
}



@Composable
private fun SettingScreen(
    settingItems: List<SettingSectionItem>,
    onBack: () -> Unit,
) {
    Column {

        BackUpperBar(onBack)

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 20.dp)
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
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
        ) {
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
                        textSize = TextSize.SS
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

/** 設定画面リスト表示用のインターフェース */
private interface SettingSectionItem

/** 設定画面リスト行データ */
private data class SettingRowData(
    val icon: Painter,
    val title: String,
    val onClick: () -> Unit = { },
    val buttonText: String = "",
    val buttonOnClick: () -> Unit = { }
): SettingSectionItem

/** 設定画面リストヘッダー */
private data class SettingSectionHeader(
    val title: String,
): SettingSectionItem

/** 設定画面リストフッター */
private data class SettingSectionFooter(
    val title: String,
): SettingSectionItem

/** 設定画面リストスペーサー */
private data class SettingSectionSpacer(
    val padding: Dp = 10.dp,
): SettingSectionItem

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
