package com.amefure.minnanotanjyoubi.View.Fragment

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Domain.NotificationRequestManager
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.Model.Keys.*
import com.amefure.minnanotanjyoubi.R
import kotlinx.coroutines.launch


class DetailPersonFragment : Fragment() {

    private var id: Int = 0
    private var name: String = ""
    private var ruby: String = ""
    private var date: String = ""
    private var relation: String = ""
    private var notify: Boolean = false
    private var memo:String = ""

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var notificationRequestManager: NotificationRequestManager
    private val calcDateInfoManager = CalcDateInfoManager()

    private lateinit var res: Resources

    private var notifyTime: String = ""
    private var notifyMsg: String = ""
    private var notifyDay: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataStoreManager = DataStoreManager(this.requireContext())
        notificationRequestManager = NotificationRequestManager(this.requireContext())

        res = this.requireContext().resources
        notifyTime = res.getString(R.string.notify_default_time)
        notifyMsg = res.getString(R.string.notify_default_message)
        notifyDay = res.getString(R.string.notify_default_day)

        arguments?.let {
            id = it.getInt(ARG_ID_KEY,0)
            name = it.getString(ARG_NAME_KEY,"")
            ruby = it.getString(ARG_RUBY_KEY,"")
            date = it.getString(ARG_DATE_KEY,"2023/10/1")
            relation = it.getString(ARG_RELATION_KEY,"2023/10/1")
            notify = it.getBoolean(ARG_NOTIFY_KEY,true)
            memo = it.getString(ARG_MEMO_KEY,"")
        }
        return inflater.inflate(R.layout.fragment_detail_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)

        val relationLabel: TextView = view.findViewById(R.id.relation_text)
        val daysLaterLabel: TextView = view.findViewById(R.id.days_later_text)
        val nameLabel: TextView = view.findViewById(R.id.name_text)
        val rubyLabel: TextView = view.findViewById(R.id.ruby_text)
        val dateLabel: TextView = view.findViewById(R.id.date_text)
        val ageLabel: TextView = view.findViewById(R.id.age_text)
        val signOgZodiacLabel: TextView = view.findViewById(R.id.sign_of_zodiac_text)
        val zodiacLabel: TextView = view.findViewById(R.id.zodiac_text)
        val memoLabel: TextView = view.findViewById(R.id.memo_text)
        val notifySwitch: Switch = view.findViewById(R.id.notify_edit_button)

        // 戻るボタン
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        editButton.setOnClickListener {
            // input画面にレコードの情報を渡して生成
            var nextFragment = InputPersonFragment.editInstance(id, name, ruby, date, relation, notify, memo)
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, nextFragment)
                addToBackStack(null)
                commit()
            }
        }

        relationLabel.text = relation
        daysLaterLabel.text = "あと" + calcDateInfoManager.daysLater(date).toString() + "日"
        nameLabel.text = name
        rubyLabel.text = ruby
        dateLabel.text = date + "（" + calcDateInfoManager.japaneseEraName(date) + "）"
        ageLabel.text = calcDateInfoManager.currentAge(date).toString() + "歳"
        signOgZodiacLabel.text = calcDateInfoManager.signOfZodiac(date)
        zodiacLabel.text = calcDateInfoManager.zodiac(date)
        memoLabel.text = memo
        notifySwitch.isChecked = notify

        notifySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val time = notifyTime.split(":")
                var thisDate = date
                if (notifyDay != res.getString(R.string.notify_default_day)) {
                    // "前日"が指定されている場合は一日前の日付取得
                    thisDate = calcDateInfoManager.getBeforeOneDayString(thisDate)
                }

                val parts = thisDate.split("[年月日]".toRegex())
                val month = parts[1].toInt()
                val day = parts[2].toInt()

                val msg = notifyMsg.replace("{userName}",name)

                notificationRequestManager.setBroadcast(id,month,day,time[0].toInt(),time[1].toInt(),msg)
            } else {
            }
        }

    }

    private fun observeNotifyInfo() {
        lifecycleScope.launch {
            dataStoreManager.observeNotifyTime().collect {
                if (it != null) {
                    notifyTime = it
                }
            }
        }
        lifecycleScope.launch {
            dataStoreManager.observeNotifyDay().collect {
                if (it != null) {
                    notifyDay = it
                }
            }

        }
        lifecycleScope.launch {
            dataStoreManager.observeNotifyMsg().collect {
                if (it != null) {
                    notifyMsg = it
                }
            }
        }
    }

    companion object{
        @JvmStatic
        fun newInstance(id: Int, name: String, ruby: String, date: String, relation: String, notify: Boolean, memo: String) =
            DetailPersonFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID_KEY, id)
                    putString(ARG_NAME_KEY, name)
                    putString(ARG_RUBY_KEY, ruby)
                    putString(ARG_DATE_KEY, date)
                    putString(ARG_RELATION_KEY, relation)
                    putBoolean(ARG_NOTIFY_KEY, notify)
                    putString(ARG_MEMO_KEY,memo)
                }
            }
    }
}