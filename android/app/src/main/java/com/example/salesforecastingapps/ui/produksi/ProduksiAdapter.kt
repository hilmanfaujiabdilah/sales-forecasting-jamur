package com.example.salesforecastingapps.ui.produksi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingapps.data.model.Produksi
import com.example.salesforecastingapps.databinding.ItemPenjualanBinding
import com.example.salesforecastingapps.utils.DateUtils

class ProduksiAdapter : RecyclerView.Adapter<ProduksiAdapter.ViewHolder>() {

    private var list = listOf<Produksi>()

    fun submitList(data: List<Produksi>) {
        list = data.sortedBy { it.tanggalProduksi }
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Produksi = list[position]

    inner class ViewHolder(private val binding: ItemPenjualanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Produksi) {
            binding.tvTanggal.text = DateUtils.formatTanggal(item.tanggalProduksi)
            binding.tvKumbung.text = "Rekomendasi: ${item.baglogBaru ?: "-"} baglog baru"
            // DIUBAH: tampilkan sebagai integer + satuan baglog (tanpa koma)
            binding.tvJumlah.text  = "${item.jumlahProduksi.toInt()} baglog"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPenjualanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount() = list.size
}