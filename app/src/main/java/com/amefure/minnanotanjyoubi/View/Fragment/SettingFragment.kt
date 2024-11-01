package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Model.Capacity
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.databinding.FragmentSettingBinding
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.launch
import java.util.Calendar
import com.google.android.gms.ads.AdRequest

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this.requireContext(),this.getString(R.string.admob_reward_id), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("", "ロード失敗")
                rewardedAd = null
                updateAdsButtonState()
            }
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("", "ロード完了")
                rewardedAd = ad
                updateAdsButtonState()
            }
        })

        // これがないと視聴できない？
        rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {}

        dataStoreManager = DataStoreManager(this.requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.adView.loadAd(AdRequest.Builder().build())

        // ローカルに保存している情報を取得
        observeLocalData()

        // 戻るボタン
        binding.backButton.setOnClickListener {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        binding.notifySettingTimeButton.setOnClickListener {
            var hour = 0
            var minute = 0
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
            val dialog = TimePickerDialog(requireContext(), android.R.style.Theme_Holo_Dialog, timeListener, hour, minute,false)
            dialog.show()
        }

        binding.notifySettingDayButton.setOnClickListener {
            if (binding.notifySettingDayButton.text == "当日") {
                binding.notifySettingDayButton.text = "前日"
                binding.notifySettingDayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_blue))
                lifecycleScope.launch{
                    dataStoreManager.saveNotifyDay("前日")
                }
            } else {
                binding.notifySettingDayButton.text = "当日"
                binding.notifySettingDayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_red))
                lifecycleScope.launch{
                    dataStoreManager.saveNotifyDay("当日")
                }
            }
        }
        binding.notifySettingMsgButton.setOnClickListener {
            val dialog = InputNotifyMsgDialogFragment()
            dialog.show(parentFragmentManager, "通知メッセージダイアログ")
        }

        binding.adsRewardPlayButton.setOnClickListener {
            if (!calcDateInfoManager.isToday(lastDate)) {
                rewardedAd?.let { ad ->
                    ad.show(this.requireActivity(), OnUserEarnedRewardListener { rewardItem ->
                        lifecycleScope.launch{
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
                AlertDialog.Builder(this.requireContext())
                    .setTitle("お知らせ")
                    .setMessage("広告を視聴できるのは1日に1回までです。")
                    .setPositiveButton("OK", { dialog, which ->
                        // ボタンクリック時の処理
                    })
                    .show()
            }
        }
    }

    private fun updateAdsButtonState() {
        if (rewardedAd != null) {
            binding.adsRewardPlayButton.text = "追加"
            binding.adsRewardPlayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_yelow))
        } else {
            binding.adsRewardPlayButton.text = "読み込み中"
            binding.adsRewardPlayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_gray_dark))
        }
    }


    // 日付ピッカーから選択された時に呼ばれるリスナー
    private var timeListener = TimePickerDialog.OnTimeSetListener { view, hour , minutes ->
        var minutesStr = minutes.toString()
        if (minutesStr.length == 1) {
            minutesStr = "0" + minutesStr
        }
        val time = hour.toString() + ":" + minutesStr
        lifecycleScope.launch{
            dataStoreManager.saveNotifyTime(time)
        }
    }

    // ローカルの情報を観測
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
                } else {
                    // 初期値格納
                    dataStoreManager.saveNotifyDay(getString(R.string.notify_default_day))
                }
            }

        }
        lifecycleScope.launch {
            dataStoreManager.observeNotifyMsg().collect {
                if (it != null) {
                    binding.notifySettingEditMsg.text = it
                } else {
                    // 初期値格納
                    dataStoreManager.saveNotifyMsg(getString(R.string.notify_default_message))
                }
            }
        }

        lifecycleScope.launch {
            dataStoreManager.observeLimitCapacity().collect {
                if (it != null) {
                    limitCapacity = it
                    binding.adsSettingCapacityLabel.text = requireContext().getString(R.string.ads_setting_capacity_label,it)
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