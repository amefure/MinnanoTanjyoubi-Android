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
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Model.Capacity
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.R
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.launch
import java.util.Calendar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class SettingFragment : Fragment() {

    lateinit var dataStoreManager: DataStoreManager
    private val calcDateInfoManager = CalcDateInfoManager()

    private var rewardedAd: RewardedAd? = null

    // ローカルデータ格納用
    private var notifyTime: String? = null
    private var limitCapacity: Int = Capacity.initialCapacity
    private var lastDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(this.requireContext(),this.getString(R.string.admob_reward_id), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("", "ロード失敗")
                rewardedAd = null
                updateAdsButtonState(view!!)
            }
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("", "ロード完了")
                rewardedAd = ad
                updateAdsButtonState(view!!)
            }
        })

        // これがないと視聴できない？
        rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {}

        dataStoreManager = DataStoreManager(this.requireContext())
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Button
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val notifyTimeButton: Button = view.findViewById(R.id.notify_setting_time_button)
        val notifyDayButton: Button = view.findViewById(R.id.notify_setting_day_button)
        val notifyMsgButton: Button = view.findViewById(R.id.notify_setting_msg_button)
        val adsPlayButton: Button = view.findViewById(R.id.ads_reward_play_button)

        var adView: AdView = view.findViewById(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())

        // ローカルに保存している通知情報を取得
        observeNotifyInfo(view)

        // 戻るボタン
        backButton.setOnClickListener {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        notifyTimeButton.setOnClickListener {
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

        notifyDayButton.setOnClickListener {
            if (notifyDayButton.text == "当日") {
                notifyDayButton.text = "前日"
                notifyDayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_blue))
                lifecycleScope.launch{
                    dataStoreManager.saveNotifyDay("前日")
                }
            } else {
                notifyDayButton.text = "当日"
                notifyDayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_red))
                lifecycleScope.launch{
                    dataStoreManager.saveNotifyDay("当日")
                }
            }
        }
        notifyMsgButton.setOnClickListener {
            val dialog = InputNotifyMsgDialogFragment()
            dialog.show(parentFragmentManager, "通知メッセージダイアログ")
        }

        adsPlayButton.setOnClickListener {

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

    private fun updateAdsButtonState(view: View) {
        val adsPlayButton: Button = view.findViewById(R.id.ads_reward_play_button)
        if (rewardedAd != null) {
            adsPlayButton.setText("追加")
            adsPlayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_yelow))
        } else {
            adsPlayButton.setText("読み込み中")
            adsPlayButton.setBackgroundColor(ContextCompat.getColor(this.requireContext(),R.color.thema_gray_dark))
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

    // ローカルの通知情報を観測
    private fun observeNotifyInfo(view: View) {
        val notifyTimeButton: Button = view.findViewById(R.id.notify_setting_time_button)
        val notifyDayButton: Button = view.findViewById(R.id.notify_setting_day_button)
        val notifyEditMsg: TextView = view.findViewById(R.id.notify_setting_edit_msg)
        val capacityLabel: TextView = view.findViewById(R.id.ads_setting_capacity_label)

        // lifecycleScope.launchはまとめると動作しないので分割
        lifecycleScope.launch {
            dataStoreManager.observeNotifyTime().collect {
                if (it != null) {
                    notifyTimeButton.text = it
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
                    notifyDayButton.text = it
                } else {
                    // 初期値格納
                    dataStoreManager.saveNotifyDay(getString(R.string.notify_default_day))
                }
            }

        }
        lifecycleScope.launch {
            dataStoreManager.observeNotifyMsg().collect {
                if (it != null) {
                    notifyEditMsg.text = it
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
                    capacityLabel.text = requireContext().getString(R.string.ads_setting_capacity_label,it)
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
}