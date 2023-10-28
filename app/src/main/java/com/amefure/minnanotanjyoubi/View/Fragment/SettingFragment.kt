package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.R
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingFragment : Fragment() {

    lateinit var dataStoreManager: DataStoreManager
    private var notifyTime: String? = null

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
            dialog.show(parentFragmentManager, "custom")
        }
    }


    // 日付ピッカーから選択された時に呼ばれるリスナー
    private var timeListener = TimePickerDialog.OnTimeSetListener { view, hour , minutes ->
        val time = hour.toString() + ":" + minutes.toString()
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
                    dataStoreManager.saveNotifyTime("7:00")
                }
            }

        }
        lifecycleScope.launch {
            dataStoreManager.observeNotifyDay().collect {
                if (it != null) {
                    notifyDayButton.text = it
                } else {
                    // 初期値格納
                    dataStoreManager.saveNotifyDay("当日")
                }
            }

        }
        lifecycleScope.launch {
            dataStoreManager.observeNotifyMsg().collect {
                if (it != null) {
                    notifyEditMsg.text = it
                }else {
                    // 初期値格納
                    dataStoreManager.saveNotifyMsg("明日は{userName}の誕生日！お祝いしてあげよう！")
                }
            }
        }
    }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}