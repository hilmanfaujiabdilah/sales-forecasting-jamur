package com.example.salesforecastingjamur.ui.produksi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingjamur.data.model.Produksi
import com.example.salesforecastingjamur.databinding.ItemPenjualanBinding
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.FormatUtils

class ProduksiAdapter : RecyclerView.Adapter<ProduksiAdapter.ViewHolder>() {

    private var list = listOf<Produksi>()

    fun submitList(data: List<Produksi>) {
        list = data.sortedByDescending { it.tanggalProduksi }
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPenjualanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Produksi) {
            binding.tvTanggal.text = DateUtils.formatTanggal(item.tanggalProduksi)
            binding.tvKumbung.text = "Rekomendasi: ${item.baglogBaru ?: "-"} baglog baru"
            binding.tvJumlah.text  = FormatUtils.formatKg(item.jumlahProduksi)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPenjualanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount() = list.size
}
