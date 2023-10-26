package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.amefure.minnanotanjyoubi.Model.Relation
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.ViewModel.InputPersonViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class InputPersonFragment : Fragment() {

    private val viewModel:InputPersonViewModel by viewModels()

    private var selectRelation: String = Relation.FRIEND.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 入力View
        val nameEdit:EditText= view.findViewById(R.id.name_edit)
        val rubyEdit:EditText= view.findViewById(R.id.ruby_edit)
        val memoEdit:EditText= view.findViewById(R.id.memo_edit)
        val relationSpinner:Spinner = view.findViewById(R.id.relation_spinner)


        val spinnerAdapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        Relation.values().forEach {
            spinnerAdapter.add(it.name)
        }

        relationSpinner.adapter = spinnerAdapter
        relationSpinner.onItemSelectedListener = spinnerAdapterListener

        // 登録ボタン
        val registerButton:ImageButton = view.findViewById(R.id.register_button)
        // 日付ピッカーダイアログの表示
        val dateEditButton:Button = view.findViewById(R.id.date_edit_button)

         registerButton.setOnClickListener {
             val name = nameEdit.text.toString()
             val ruby = rubyEdit.text.toString()
             val date = dateEditButton.text.toString()
             val relation = selectRelation
             val memo = memoEdit.text.toString()
             viewModel.insertPerson(name,ruby,date,relation,memo,false)
             Snackbar.make(view,"追加しました。", Snackbar.LENGTH_SHORT)
                 .setBackgroundTint(ContextCompat.getColor(view.context,R.color.positive_color))
                 .show()
             parentFragmentManager.apply {
                 popBackStack()
             }
        }

        viewModel.selectDate.observe(this.requireActivity(), Observer {
            dateEditButton.text = it
        })


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
}
