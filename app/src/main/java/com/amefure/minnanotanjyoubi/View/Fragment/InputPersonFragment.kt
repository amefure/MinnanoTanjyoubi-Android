package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.amefure.minnanotanjyoubi.Domain.CalcDateInfoManager
import com.amefure.minnanotanjyoubi.Model.Relation
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.ViewModel.InputPersonViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import com.amefure.minnanotanjyoubi.Model.Keys.*
class InputPersonFragment : Fragment() {

    private val viewModel:InputPersonViewModel by viewModels()



    private var selectRelation: String = Relation.FRIEND.name

    // 更新時に値が格納される
    private var receiveId: Int? = null
    private var receiveName: String = ""
    private var receiveRuby: String = ""
    private var receiveDate: String = ""
    private var receiveRelation: String = ""
    private var receiveNotify: Boolean = false
    private var receiveMemo:String = ""

    // Input UI
    private lateinit var nameEdit: EditText
    private lateinit var rubyEdit: EditText
    private lateinit var dateEditButton:Button
    private lateinit var relationSpinner:Spinner
    private lateinit var notifySwitch: Switch
    private lateinit var memoEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            receiveId = it.getInt(ARG_ID_KEY,0)
            receiveName = it.getString(ARG_NAME_KEY,"")
            receiveRuby = it.getString(ARG_RUBY_KEY,"")
            receiveDate = it.getString(ARG_DATE_KEY,"2023/10/1")
            receiveRelation = it.getString(ARG_RELATION_KEY,"2023/10/1")
            receiveNotify = it.getBoolean(ARG_NOTIFY_KEY,true)
            receiveMemo = it.getString(ARG_MEMO_KEY,"")
        }
        return inflater.inflate(R.layout.fragment_input_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Button
        val backButton: ImageButton= view.findViewById(R.id.back_button)
        val registerButton:ImageButton = view.findViewById(R.id.register_button)

        // 入力View
        nameEdit = view.findViewById(R.id.name_edit)
        rubyEdit = view.findViewById(R.id.ruby_edit)
        dateEditButton = view.findViewById(R.id.date_edit_button)
        relationSpinner = view.findViewById(R.id.relation_spinner)
        notifySwitch = view.findViewById(R.id.notify_edit_button)
        memoEdit = view.findViewById(R.id.memo_edit)

        // 詳細画面からの遷移の場合は情報をセット
        if (receiveId != null) {
            setReceiveUIText()
        }

        // 戻るボタン
        backButton.setOnClickListener {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        // 登録ボタン
        registerButton.setOnClickListener {
            val name = nameEdit.text.toString()
            val ruby = rubyEdit.text.toString()
            val date = dateEditButton.text.toString()
            val relation = selectRelation
            var notify = notifySwitch.isChecked
            val memo = memoEdit.text.toString()

            if (!name.isEmpty()) {

                if (receiveId == null) {
                    viewModel.insertPerson(name,ruby,date,relation,memo,notify)
                    Snackbar.make(view,"追加しました。", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(view.context,R.color.positive_color))
                        .show()
                    showOffKeyboard()
                    parentFragmentManager.apply {
                        popBackStack()
                    }
                } else {
                    // 編集モード
                    viewModel.updatePerson(receiveId!!,name,ruby,date,relation,memo,notify)
                    showOffKeyboard()
                    Snackbar.make(view,"更新しました。", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(ContextCompat.getColor(view.context,R.color.positive_color))
                        .show()
                    parentFragmentManager.apply {
                        // トップまで戻す
                        popBackStack()
                        popBackStack()
                    }

                }

            } else {
                Snackbar.make(view,"名前を入力してください。", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(view.context,R.color.negative_color))
                    .show()
            }
        }

        // 日付ダイアログ表示ボタン
        dateEditButton.setOnClickListener {
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
            val dialog = DatePickerDialog(requireContext(), android.R.style.Theme_Holo_Dialog,dateListener, year, month, day)
            dialog.show()
        }

        // スピナーセット
        val spinnerAdapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        Relation.values().forEach {
            spinnerAdapter.add(it.value())
        }
        relationSpinner.adapter = spinnerAdapter
        relationSpinner.onItemSelectedListener = spinnerAdapterListener

        // 選択される日付を観測
        viewModel.selectDate.observe(this.requireActivity(), Observer {
            dateEditButton.text = it
        })

    }

    // DatePickerFragmentから選択された日付を反映させる
    private fun updateDate(year: Int, month: Int, day: Int) {
        val formattedDate = String.format("%04d年%02d月%02d日", year, month + 1, day)
        viewModel.setSelectDate(formattedDate)
    }

    // 日付ピッカーから選択された時に呼ばれるリスナー
    private var dateListener =  DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        updateDate(year, month, dayOfMonth)
    }

    private val spinnerAdapterListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
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
        nameEdit.setText(receiveName)
        rubyEdit.setText(receiveRuby)
        viewModel.setSelectDate(receiveDate)
        relationSpinner.setSelection(0)
        notifySwitch.isChecked = receiveNotify
        memoEdit.setText(receiveMemo)
    }

    companion object {
        @JvmStatic
        fun editInstance(id: Int, name: String, ruby: String, date: String, relation: String, notify: Boolean, memo: String) =
            InputPersonFragment().apply {
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


