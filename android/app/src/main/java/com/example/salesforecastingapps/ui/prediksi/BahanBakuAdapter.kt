package com.example.salesforecastingapps.ui.prediksi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingapps.data.model.BahanBaku
import com.example.salesforecastingapps.databinding.ItemBahanBakuBinding

class BahanBakuAdapter : RecyclerView.Adapter<BahanBakuAdapter.ViewHolder>() {

    private var list = listOf<BahanBaku>()

    fun submitList(data: List<BahanBaku>) {
        list = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemBahanBakuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BahanBaku) {
            binding.tvNamaBahan.text   = item.nama
//            val satuanText = item.satuan ?: ""
            binding.tvJumlahBahan.text = "%.2f".format(item.jumlah)
            binding.tvSatuan.text = item.satuan ?: ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBahanBakuBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount() = list.size
}