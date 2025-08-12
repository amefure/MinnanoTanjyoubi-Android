package com.amefure.minnanotanjyoubi.View.Fragment.Domain

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Manager.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Manager.NotificationRequestManager
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.Model.Keys.*
import com.amefure.minnanotanjyoubi.Model.Relation
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.ViewModel.InputPersonViewModel
import com.amefure.minnanotanjyoubi.databinding.FragmentInputPersonBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class InputPersonFragment : Fragment() {
    private val viewModel: InputPersonViewModel by viewModels()
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var notificationRequestManager: NotificationRequestManager
    private val calcDateInfoManager = CalcDateInfoManager()

    private var _binding: FragmentInputPersonBinding? = null
    private val binding get() = _binding!!

    private lateinit var res: Resources

    private var notifyTime: String = ""
    private var notifyMsg: String = ""
    private var notifyDay: String = ""

    private var selectRelation: String = Relation.FRIEND.name

    // 更新時に値が格納される
    private var receiveId: Int? = null
    private var receiveName: String = ""
    private var receiveRuby: String = ""
    private var receiveDate: String = ""
    private var receiveRelation: String = ""
    private var receiveNotify: Boolean = false
    private var receiveMemo: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInputPersonBinding.inflate(inflater, container, false)

        dataStoreManager = DataStoreManager(this.requireContext())
        notificationRequestManager = NotificationRequestManager(this.requireContext())

        res = this.requireContext().resources

        notifyTime = res.getString(R.string.notify_default_time)
        notifyMsg = res.getString(R.string.notify_default_message)
        notifyDay = res.getString(R.string.notify_default_day)

        arguments?.let {
            receiveId = it.getInt(ARG_ID_KEY, 0)
            receiveName = it.getString(ARG_NAME_KEY, "")
            receiveRuby = it.getString(ARG_RUBY_KEY, "")
            receiveDate = it.getString(ARG_DATE_KEY, "2023/10/1")
            receiveRelation = it.getString(ARG_RELATION_KEY, "2023/10/1")
            receiveNotify = it.getBoolean(ARG_NOTIFY_KEY, true)
            receiveMemo = it.getString(ARG_MEMO_KEY, "")
        }
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        // Button
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val registerButton: ImageButton = view.findViewById(R.id.register_button)

        observeNotifyInfo()

        // 戻るボタン
        backButton.setOnClickListener {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        // 登録ボタン
        registerButton.setOnClickListener {
            val name = binding.nameEdit.text.toString()
            val ruby = binding.rubyEdit.text.toString()
            val date = binding.dateEditButton.text.toString()
            val relation = selectRelation
            val notify = binding.notifyEditButton.isChecked
            val memo = binding.memoEdit.text.toString()

            if (name.isNotEmpty()) {
                if (receiveId == null) {
                    viewModel.insertPerson(name, ruby, date, relation, memo, notify) {
                        if (notify) {
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

                            notificationRequestManager.setBroadcast(it.toInt(), month, day, time[0].toInt(), time[1].toInt(), msg)
                        }
                    }

                    showOffKeyboard()
                    parentFragmentManager.apply {
                        popBackStack()
                    }
                } else {
                    // 編集モード
                    viewModel.updatePerson(receiveId!!, name, ruby, date, relation, memo, notify)
                    showOffKeyboard()
                    parentFragmentManager.apply {
                        // トップまで戻す
                        popBackStack()
                        popBackStack()
                    }
                }
            } else {
                AlertDialog
                    .Builder(this.requireContext())
                    .setTitle("ERROR")
                    .setMessage("名前を入力してください。")
                    .setPositiveButton("OK", { _, _ -> })
                    .show()
            }
        }

        // 日付ダイアログ表示ボタン
        binding.dateEditButton.setOnClickListener {
            var year = 0
            var month = 0
            var day = 0
            if (viewModel.selectDate.value != null) {
                // 日付を選択済みの場合は初期値を変更
                val dateStr = viewModel.selectDate.value!!
                val parts = dateStr.split("[年月日]".toRegex())
                year = parts[0].toInt()
                month = parts[1].toInt() - 1
                day = parts[2].toInt()
            } else {
                // 現在の日付情報を取得
                val c = Calendar.getInstance()
                year = c.get(Calendar.YEAR)
                month = c.get(Calendar.MONTH)
                day = c.get(Calendar.DAY_OF_MONTH)
            }
            val dialog = DatePickerDialog(requireContext(), android.R.style.Theme_Holo_Dialog, dateListener, year, month, day)
            dialog.show()
        }

        // スピナーセット
        val spinnerAdapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        Relation.entries.forEach {
            spinnerAdapter.add(it.value())
        }
        binding.relationSpinner.adapter = spinnerAdapter
        binding.relationSpinner.onItemSelectedListener = spinnerAdapterListener

        // 選択される日付を観測
        viewModel.selectDate.observe(
            this.requireActivity(),
            Observer {
                binding.dateEditButton.text = it
            },
        )

        // 詳細画面からの遷移の場合は情報をセット
        // 全ての初期化の最後に実装
        if (receiveId != null) {
            setReceiveUIText()
        }
    }

    // DatePickerFragmentから選択された日付を反映させる
    private fun updateDate(
        year: Int,
        month: Int,
        day: Int,
    ) {
        val formattedDate = String.format("%04d年%02d月%02d日", year, month + 1, day)
        viewModel.setSelectDate(formattedDate)
    }

    // 日付ピッカーから選択された時に呼ばれるリスナー
    private var dateListener =
        DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            updateDate(year, month, dayOfMonth)
        }

    private val spinnerAdapterListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                // 選択された時に実行したい処理
                selectRelation = parent!!.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 選択されなかった時に実行したい処理
            }
        }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setReceiveUIText() {
        binding.nameEdit.setText(receiveName)
        binding.rubyEdit.setText(receiveRuby)
        viewModel.setSelectDate(receiveDate)
        binding.relationSpinner.setSelection(Relation.getIndex(receiveRelation))
        binding.notifyEditButton.isChecked = receiveNotify
        binding.memoEdit.setText(receiveMemo)
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

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun editInstance(
            id: Int,
            name: String,
            ruby: String,
            date: String,
            relation: String,
            notify: Boolean,
            memo: String,
        ) = InputPersonFragment().apply {
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
