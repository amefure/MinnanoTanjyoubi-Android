package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.R
import kotlinx.coroutines.launch

class InputNotifyMsgDialogFragment : DialogFragment() {

    lateinit var dataStoreManager: DataStoreManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dataStoreManager = DataStoreManager(this.requireContext())
        val builder = AlertDialog.Builder(this.requireContext())
        val inflater = this.requireActivity().layoutInflater
        val dialog = inflater.inflate(R.layout.fragment_input_notify_msg_dialog,null)
        val registerMsgButton: Button = dialog.findViewById(R.id.register_msg_button)
        val notifyMsgEdit: EditText = dialog.findViewById(R.id.notify_msg_edit)
        observeNotifyMsg(dialog)
        registerMsgButton.setOnClickListener {
            lifecycleScope.launch{
                dataStoreManager.saveNotifyMsg(notifyMsgEdit.text.toString())
                showOffKeyboard()
                dismiss()
            }
        }
        builder.setView(dialog)
        return builder.create()
    }

    private fun observeNotifyMsg(view: View) {
        val notifyMsgEdit: EditText = view.findViewById(R.id.notify_msg_edit)
        lifecycleScope.launch{
            dataStoreManager.observeNotifyMsg().collect {
                if (it != null) {
                    notifyMsgEdit.setText(it)
                }
            }
        }
    }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}