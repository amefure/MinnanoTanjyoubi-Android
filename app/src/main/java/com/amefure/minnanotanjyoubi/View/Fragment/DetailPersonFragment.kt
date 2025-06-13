package com.amefure.minnanotanjyoubi.View.Fragment

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Domain.NotificationRequestManager
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.Model.Keys.*
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.ViewModel.DetailPersonViewModel
import com.amefure.minnanotanjyoubi.databinding.FragmentDetailPersonBinding
import kotlinx.coroutines.launch

class DetailPersonFragment : Fragment() {
    private var id: Int = 0
    private var name: String = ""
    private var ruby: String = ""
    private var date: String = ""
    private var relation: String = ""
    private var notify: Boolean = false
    private var memo: String = ""

    private val viewModel: DetailPersonViewModel by viewModels()
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var notificationRequestManager: NotificationRequestManager
    private val calcDateInfoManager = CalcDateInfoManager()

    private lateinit var res: Resources

    private var notifyTime: String = ""
    private var notifyMsg: String = ""
    private var notifyDay: String = ""

    private var _binding: FragmentDetailPersonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailPersonBinding.inflate(inflater, container, false)

        dataStoreManager = DataStoreManager(this.requireContext())
        notificationRequestManager = NotificationRequestManager(this.requireContext())

        res = this.requireContext().resources
        notifyTime = res.getString(R.string.notify_default_time)
        notifyMsg = res.getString(R.string.notify_default_message)
        notifyDay = res.getString(R.string.notify_default_day)

        arguments?.let {
            id = it.getInt(ARG_ID_KEY, 0)
            name = it.getString(ARG_NAME_KEY, "")
            ruby = it.getString(ARG_RUBY_KEY, "")
            date = it.getString(ARG_DATE_KEY, "2023/10/1")
            relation = it.getString(ARG_RELATION_KEY, "2023/10/1")
            notify = it.getBoolean(ARG_NOTIFY_KEY, true)
            memo = it.getString(ARG_MEMO_KEY, "")
        }
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        observeNotifyInfo()

        // 戻るボタン
        binding.componentBackUpperContainer.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.editButton.setOnClickListener {
            // input画面にレコードの情報を渡して生成
            val nextFragment = InputPersonFragment.editInstance(id, name, ruby, date, relation, notify, memo)
            parentFragmentManager.beginTransaction().apply {
                add(R.id.main_frame, nextFragment)
                addToBackStack(null)
                commit()
            }
        }

        binding.relationText.text = relation
        if (calcDateInfoManager.isBirthDay(date)) {
            binding.daysLaterText.text = "HAPPY BIRTHDAY"
        } else {
            binding.daysLaterText.text = "あと" + calcDateInfoManager.daysLater(date).toString() + "日"
        }

        binding.nameText.text = name
        binding.rubyText.text = ruby
        binding.dateText.text = date + "（" + calcDateInfoManager.japaneseEraName(date) + "）"
        binding.ageText.text = calcDateInfoManager.currentAge(date).toString() + "歳"
        binding.signOfZodiacText.text = calcDateInfoManager.signOfZodiac(date)
        binding.zodiacText.text = calcDateInfoManager.zodiac(date)
        binding.memoText.text = memo
        binding.notifyEditButton.isChecked = notify

        binding.notifyEditButton.setOnCheckedChangeListener { _, isChecked ->
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

                val msg = notifyMsg.replace("{userName}", name)

                // 通知をセット
                notificationRequestManager.setBroadcast(id, month, day, time[0].toInt(), time[1].toInt(), msg)

                // ローカルデータを更新
                viewModel.updatePerson(id, name, ruby, date, relation, memo, isChecked)
            } else {
                // 通知をリセット
                notificationRequestManager.deleteBroadcast(id)
                // ローカルデータを更新
                viewModel.updatePerson(id, name, ruby, date, relation, memo, isChecked)
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            id: Int,
            name: String,
            ruby: String,
            date: String,
            relation: String,
            notify: Boolean,
            memo: String,
        ) = DetailPersonFragment().apply {
            arguments =
                Bundle().apply {
                    putInt(ARG_ID_KEY, id)
                    putString(ARG_NAME_KEY, name)
                    putString(ARG_RUBY_KEY, ruby)
                    putString(ARG_DATE_KEY, date)
                    putString(ARG_RELATION_KEY, relation)
                    putBoolean(ARG_NOTIFY_KEY, notify)
                    putString(ARG_MEMO_KEY, memo)
                }
        }
    }
}
