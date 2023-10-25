package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.amefure.minnanotanjyoubi.R
import java.util.Calendar

class DatePickerDialogFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {

    // ダイアログの生成
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 現在の日付情報を取得
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(requireContext(), android.R.style.Theme_Holo_Dialog,this, year, month, day)
        return  dialog
    }

    // 日付が選択されたときに呼び出されるコールバックメソッド
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

    }
}