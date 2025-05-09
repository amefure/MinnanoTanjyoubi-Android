package com.amefure.minnanotanjyoubi.View.Fragment

import BillingManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amefure.minnanotanjyoubi.View.Adapter.BillingItemAdapter
import com.amefure.minnanotanjyoubi.databinding.FragmentInAppBillingBinding
import kotlinx.coroutines.launch

class InAppBillingFragment : Fragment() {

    private var _binding: FragmentInAppBillingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInAppBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val billingManager = BillingManager(this.requireContext())
        lifecycleScope.launch {
            billingManager.initialize()
        }


        binding.itemRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        binding.itemRecyclerView.addItemDecoration(
            DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL)
        )
        binding.itemRecyclerView.adapter = BillingItemAdapter(emptyList())
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}