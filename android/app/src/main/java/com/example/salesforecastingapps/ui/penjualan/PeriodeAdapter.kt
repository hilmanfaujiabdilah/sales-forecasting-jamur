package com.example.salesforecastingapps.ui.penjualan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingapps.data.model.AgregasiBulanan
import com.example.salesforecastingapps.data.model.Periode
import com.example.salesforecastingapps.databinding.ItemPeriodeBinding
import com.example.salesforecastingapps.utils.DateUtils

class PeriodeAdapter(
    private val onItemClick: (Periode) -> Unit
) : RecyclerView.Adapter<PeriodeAdapter.ViewHolder>() {

    private var list = listOf<Periode>()

    fun submitList(data: List<Periode>) {
        list = data.sortedBy { it.tahun * 100 + it.bulan }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPeriodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Periode) {
            binding.tvNamaBulan.text = DateUtils.namaBulan(item.bulan)
            binding.tvTahun.text     = item.tahun.toString()
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPeriodeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount() = list.size
}
