package com.amefure.minnanotanjyoubi.View.Fragment.Setting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.databinding.FragmentInputNotifyMsgBinding
import kotlinx.coroutines.launch

class InputNotifyMsgFragment : Fragment() {
    private lateinit var dataStoreManager: DataStoreManager

    private var _binding: FragmentInputNotifyMsgBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInputNotifyMsgBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        dataStoreManager = DataStoreManager(this.requireContext())
        observeNotifyMsg()

        // 戻るボタン
        binding.componentBackUpperContainer.backButton.setOnClickListener {
            showOffKeyboard()
            parentFragmentManager.popBackStack()
        }

        binding.registerMsgButton.setOnClickListener {
            lifecycleScope.launch {
                dataStoreManager.saveNotifyMsg(binding.notifyMsgEdit.text.toString())

                AlertDialog
                    .Builder(this@InputNotifyMsgFragment.requireContext())
                    .setTitle("Success")
                    .setMessage("通知メッセージを変更しました。")
                    .setPositiveButton("OK", { _, _ ->
                        showOffKeyboard()
                        parentFragmentManager.popBackStack()
                    })
                    .show()
            }
        }
    }

    private fun observeNotifyMsg() {
        lifecycleScope.launch {
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
