package com.amefure.minnanotanjyoubi.View.Fragment

import BillingManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amefure.minnanotanjyoubi.View.Adapter.BillingItemAdapter
import com.amefure.minnanotanjyoubi.databinding.FragmentInAppBillingBinding
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.launch

class InAppBillingFragment : Fragment() {

    /** アプリ内課金管理クラス */
    private lateinit var billingManager: BillingManager

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

        billingManager = BillingManager(this.requireContext())

        // 戻るボタン
        binding.componentBackUpperContainer.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        lifecycleScope.launch {
            try {
                billingManager.initialize()
            } catch (e: Exception) {
                binding.noItemView.visibility = View.VISIBLE
                binding.itemRecyclerView.visibility = View.GONE
            }

            // 商品情報が取得できたらリストをセットアップ
            billingManager.productDetailsList
                .collect { list ->
                    // 商品アイテムリストをセットアップ
                    binding.itemRecyclerView.layoutManager = LinearLayoutManager(this@InAppBillingFragment.requireContext())
                    val adapter = BillingItemAdapter(this@InAppBillingFragment.requireContext(), list)
                    adapter.setUpListener(
                        object: BillingItemAdapter.OnBillingListener {
                            override fun onPurchaseButtonClick(product: ProductDetails) {
                                val activity = activity ?: return
                                lifecycleScope.launch {
                                    billingManager.launchPurchaseFlow(activity, product)
                                }
                            }

                            override fun isPurchased(product: ProductDetails): Boolean {
                               return billingManager.isPurchased(product.productId)
                            }
                        }

                    )
                    binding.itemRecyclerView.adapter = adapter
                }
        }
    }

    override fun onDestroy() {
        billingManager.destroy()
        _binding = null
        super.onDestroy()
    }
}