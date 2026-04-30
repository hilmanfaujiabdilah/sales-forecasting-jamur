package com.example.salesforecastingjamur.ui.penjualan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingjamur.data.model.Penjualan
import com.example.salesforecastingjamur.databinding.ItemPenjualanBinding
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.FormatUtils

class PenjualanAdapter : RecyclerView.Adapter<PenjualanAdapter.ViewHolder>() {

    private var list = listOf<Penjualan>()

    fun submitList(data: List<Penjualan>) {
        list = data.sortedByDescending { it.tanggal }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPenjualanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Penjualan) {
            binding.tvTanggal.text = DateUtils.formatTanggal(item.tanggal)
            binding.tvKumbung.text = item.namaKumbung
            binding.tvJumlah.text  = FormatUtils.formatKg(item.jumlah)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPenjualanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount() = list.size
}
