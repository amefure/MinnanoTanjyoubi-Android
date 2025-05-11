package com.amefure.minnanotanjyoubi.View.Fragment

import BillingManager
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amefure.minnanotanjyoubi.BuildConfig
import com.amefure.minnanotanjyoubi.Model.DataStore.DataStoreManager
import com.amefure.minnanotanjyoubi.View.Adapter.BillingItemAdapter
import com.amefure.minnanotanjyoubi.databinding.FragmentInAppBillingBinding
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.launch

class InAppBillingFragment : Fragment() {

    /** アプリ内課金管理クラス */
    private lateinit var billingManager: BillingManager
    private lateinit var dataStoreManager: DataStoreManager
    private var billingItemAdapter: BillingItemAdapter? = null

    private var _binding: FragmentInAppBillingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInAppBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        billingManager = BillingManager(this.requireContext())
        dataStoreManager = DataStoreManager(this.requireContext())

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
                    billingItemAdapter = BillingItemAdapter(this@InAppBillingFragment.requireContext(), list)
                    billingItemAdapter?.setUpListener(
                        object: BillingItemAdapter.OnBillingListener {
                            override fun onPurchaseButtonClick(product: ProductDetails) {
                                val activity = activity ?: return
                                lifecycleScope.launch {
                                    val result = billingManager.launchPurchaseFlow(activity, product)
                                    result.onSuccess { id ->
                                        if (id.products.firstOrNull() == BuildConfig.IN_APP_REMOVE_ADS_ID) {
                                            Log.d("InApp", "購入成功：広告削除")
                                            dataStoreManager.saveInAppRemoveAdsFlag(true)
                                            binding.itemRecyclerView.adapter?.notifyDataSetChanged()
                                        } else if (id.products.firstOrNull() == BuildConfig.IN_APP_UNLOCK_STORAGE_ID) {
                                            Log.d("InApp", "購入成功：容量解放")
                                            dataStoreManager.saveInAppUnlockStorage(true)
                                            binding.itemRecyclerView.adapter?.notifyDataSetChanged()
                                        } else {
                                            AlertDialog.Builder(this@InAppBillingFragment.requireContext())
                                                .setTitle("ERROR")
                                                .setMessage("予期せぬエラーが発生し、購入に失敗しました。時間をあけてから再度お試してください。")
                                                .setPositiveButton("OK", { _, _ -> })
                                                .show()
                                        }

                                    }.onFailure { error ->
                                        Log.d("InApp", "購入失敗")
                                        AlertDialog.Builder(this@InAppBillingFragment.requireContext())
                                            .setTitle("ERROR")
                                            .setMessage("予期せぬエラーが発生し、購入に失敗しました。時間をあけてから再度お試してください。")
                                            .setPositiveButton("OK", { _, _ -> })
                                            .show()
                                    }
                                }
                            }

                            override fun isPurchased(product: ProductDetails): Boolean {
                               return billingManager.isPurchased(product.productId)
                            }
                        }

                    )
                    binding.itemRecyclerView.adapter = billingItemAdapter
                }
        }
    }

    override fun onDestroy() {
        billingManager.destroy()
        billingItemAdapter = null
        binding.itemRecyclerView.adapter = null
        _binding = null
        super.onDestroy()
    }
}