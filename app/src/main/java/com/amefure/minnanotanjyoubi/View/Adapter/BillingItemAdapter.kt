package com.amefure.minnanotanjyoubi.View.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amefure.minnanotanjyoubi.R
import com.amefure.minnanotanjyoubi.databinding.ComponentBillingItemRowBinding
import com.android.billingclient.api.ProductDetails

/** 課金アイテムリサイクルビュー用Adapter */
class BillingItemAdapter(
    private var context: Context,
    productItems: List<ProductDetails>
): RecyclerView.Adapter<BillingItemAdapter.MainViewHolder>() {

    /** 課金アイテムリスト */
    private val _productItems: MutableList<ProductDetails> = productItems.toMutableList()
    /** アイテム数 */
    override fun getItemCount(): Int = _productItems.size

    private var _binding: ComponentBillingItemRowBinding? = null
    private val binding get() = _binding!!

    private lateinit var listener: OnBillingListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        _binding = ComponentBillingItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    /** アイテムタップ用クリックリスナー */
    interface OnBillingListener {
        fun onPurchaseButtonClick(product: ProductDetails)
        fun isPurchased(product: ProductDetails): Boolean
    }

    /** リスナーセットアップ */
    public fun setUpListener(listener: OnBillingListener) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val product = _productItems[position]
        holder.title.text = product.name
        holder.desc.text = product.description
        holder.amount.text = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "￥0"
        if (listener.isPurchased(product)) {
            holder.purchaseButton.text = context.getString(R.string.in_app_billing_purchased)
            holder.purchaseButton.isEnabled = false
            val colorValue = ContextCompat.getColorStateList(context, R.color.thema_gray_light)
            holder.purchaseButton.backgroundTintList = colorValue
        } else {
            holder.purchaseButton.setOnClickListener {
                listener.onPurchaseButtonClick(product)
            }
        }
    }

    class MainViewHolder(binding: ComponentBillingItemRowBinding): RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val desc: TextView = binding.itemDesc
        val amount: TextView = binding.itemAmount
        val purchaseButton: Button = binding.purchaseButton
    }
}