package com.amefure.minnanotanjyoubi.View.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.amefure.minnanotanjyoubi.R

class InputPersonFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_input_person, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 日付ピッカーダイアログの表示
        val dateEditButton:Button = view.findViewById(R.id.date_edit_button)
        dateEditButton.setOnClickListener {
            val dialog = DatePickerDialogFragment()
            dialog.show(parentFragmentManager, "date")
        }
    }
}