package com.example.salesforecastingjamur.ui.prediksi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingjamur.data.model.Prediksi
import com.example.salesforecastingjamur.databinding.ItemPrediksiBinding
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.FormatUtils

class PrediksiAdapter(
    private val onItemClick: (Prediksi) -> Unit
) : RecyclerView.Adapter<PrediksiAdapter.ViewHolder>() {

    private var list = listOf<Prediksi>()

    fun submitList(data: List<Prediksi>) {
        list = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPrediksiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Prediksi) {
            binding.tvPeriodePrediksi.text =
                "${DateUtils.formatPeriode(item.periodePred1)} — ${DateUtils.formatPeriode(item.periodePred2)}"
            binding.tvNilaiPrediksi.text =
                "P1: ${FormatUtils.formatKg(item.predPeriode1)}  |  P2: ${FormatUtils.formatKg(item.predPeriode2)}"
            binding.tvMape.text = "MAPE ${FormatUtils.formatPersen(item.nilaiMape)}"
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPrediksiBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    override fun getItemCount() = list.size
}
