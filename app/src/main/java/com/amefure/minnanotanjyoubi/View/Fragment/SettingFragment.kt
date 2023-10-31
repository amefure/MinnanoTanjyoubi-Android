package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.TimePickerDialog
import android.content.Context
import android.media.tv.AdRequest
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
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingFragment : Fragment() {

    lateinit var dataStoreManager: DataStoreManager
    private var notifyTime: String? = null

    private var rewardedAd: RewardedAd? = null
    private final var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var adRequest = AdRequest.Builder().build()
        RewardedAd.load(this.requireContext(),"ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.toString())
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
            }
        })

        rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                // Called when ad fails to show.
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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


        // ローカルに保存している通知情報を取得
        observeNotifyInfo(view)

        // 戻るボタン
        backButton.setOnClickListener {


            rewardedAd?.let { ad ->
                ad.show(this.requireActivity(), OnUserEarnedRewardListener { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Log.d(TAG, "User earned the reward.")
                })
            } ?: run {
                Log.d(TAG, "The rewarded ad wasn't ready yet.")
            }

//            showOffKeyboard()
//            parentFragmentManager.popBackStack()
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
    }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}