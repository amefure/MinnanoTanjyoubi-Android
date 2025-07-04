package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
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
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        val rewardId = if (BuildConfig.DEBUG) BuildConfig.ADMOB_REWARD_ID_TEST else BuildConfig.ADMOB_REWARD_ID_PROD
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this.requireContext(),
            rewardId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("Admob", "ロード失敗")
                    rewardedAd = null
                    updateAdsButtonState()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("Admob", "ロード完了")
                    rewardedAd = ad
                    updateAdsButtonState()
                }
            },
        )

        // これがないと視聴できない？
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {}

        dataStoreManager = DataStoreManager(this.requireContext())
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // バナーの追加と読み込み
        addAdBannerView()

        // ローカルに保存している情報を取得
        observeLocalData()

        // 戻るボタン
        binding.componentBackUpperContainer.backButton.setOnClickListener {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        binding.notifySettingTimeButton.setOnClickListener {
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

        binding.notifySettingDayButton.setOnClickListener {
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
        binding.notifySettingMsgButton.setOnClickListener {
            val nextFragment = InputNotifyMsgFragment()
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, nextFragment)
                addToBackStack(null)
                commit()
            }
        }

        binding.adsRewardPlayButton.setOnClickListener {
            if (!calcDateInfoManager.isToday(lastDate)) {
                rewardedAd?.let { ad ->
                    ad.show(this.requireActivity(), { _ ->
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
                    .Builder(this.requireContext())
                    .setTitle("お知らせ")
                    .setMessage("広告を視聴できるのは1日に1回までです。")
                    .setPositiveButton("OK", { _, _ -> })
                    .show()
            }
        }

        binding.inAppBillingLayout.setOnClickListener {
            val nextFragment = InAppBillingFragment()
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, nextFragment)
                addToBackStack(null)
                commit()
            }
        }
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
