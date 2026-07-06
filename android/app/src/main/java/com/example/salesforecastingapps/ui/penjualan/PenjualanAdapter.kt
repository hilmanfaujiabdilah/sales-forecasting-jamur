package com.example.salesforecastingapps.ui.penjualan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingapps.data.model.Penjualan
import com.example.salesforecastingapps.databinding.ItemPenjualanBinding
import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.FormatUtils

class PenjualanAdapter : RecyclerView.Adapter<PenjualanAdapter.ViewHolder>() {

    private var list = listOf<Penjualan>()

    fun getItem(position: Int): Penjualan = list[position]

    fun submitList(data: List<Penjualan>) {
        list = data.sortedBy { it.tanggal }
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
