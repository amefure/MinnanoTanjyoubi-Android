package com.amefure.minnanotanjyoubi.View.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amefure.minnanotanjyoubi.databinding.ComponentBillingItemRowBinding
import com.android.billingclient.api.ProductDetails

class BillingItemAdapter(productItems: List<ProductDetails>): RecyclerView.Adapter<BillingItemAdapter.MainViewHolder>() {

    private val _productItems: MutableList<ProductDetails> = productItems.toMutableList()

    override fun getItemCount(): Int = _productItems.size

    private var _binding: ComponentBillingItemRowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        _binding = ComponentBillingItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val product = _productItems[position]
        holder.title.text = product.title
        holder.desc.text = product.description
        holder.amount.text = product.name
    }

    class MainViewHolder(binding: ComponentBillingItemRowBinding): RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.itemTitle
        val desc: TextView = binding.itemDesc
        val amount: TextView = binding.itemAmount
        val purchaseButton: Button = binding.purchaseButton
    }
}