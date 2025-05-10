package com.amefure.minnanotanjyoubi.View.Fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.databinding.FragmentInputNotifyMsgBinding
import kotlinx.coroutines.launch

class InputNotifyMsgDialogFragment : DialogFragment() {

    private lateinit var dataStoreManager: DataStoreManager

    private var _binding: FragmentInputNotifyMsgBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dataStoreManager = DataStoreManager(this.requireContext())
        _binding = FragmentInputNotifyMsgBinding.inflate(LayoutInflater.from(requireContext()))
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        observeNotifyMsg()
        binding.registerMsgButton.setOnClickListener {
            lifecycleScope.launch{
                dataStoreManager.saveNotifyMsg(binding.notifyMsgEdit.text.toString())
                showOffKeyboard()
                dismiss()
            }
        }
        return builder.create()
    }

    private fun observeNotifyMsg() {
        lifecycleScope.launch{
            dataStoreManager.observeNotifyMsg().collect {
                if (it != null && _binding != null) {
                    binding.notifyMsgEdit.setText(it)
                }
            }
        }
    }

    private fun showOffKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}