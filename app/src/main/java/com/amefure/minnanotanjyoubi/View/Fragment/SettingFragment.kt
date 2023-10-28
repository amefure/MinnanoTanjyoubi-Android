package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import com.amefure.minnanotanjyoubi.R
import java.util.Calendar

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Button
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val notifySettingTimeButton: ImageButton = view.findViewById(R.id.notify_setting_time_button)

        // 戻るボタン
        backButton.setOnClickListener {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        notifySettingTimeButton.setOnClickListener {
//            var year = 0
//            var month = 0
//            var day = 0
//            if (viewModel.selectDate.value != null) {
//                // 日付を選択済みの場合は初期値を変更
//                val dateStr = viewModel.selectDate.value!!
//                val parts = dateStr.split("[年月日]".toRegex())
//                year = parts[0].toInt()
//                month = parts[1].toInt() - 1
//                day = parts[2].toInt()
//            } else {
//                // 現在の日付情報を取得
//                val c = Calendar.getInstance()
//                year = c.get(Calendar.YEAR)
//                month = c.get(Calendar.MONTH)
//                day = c.get(Calendar.DAY_OF_MONTH)
//            }
//            val dialog = DatePickerDialog(requireContext(), android.R.style.Theme_Holo_Dialog,dateListener, year, month, day)
//            dialog.show()
        }
    }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}