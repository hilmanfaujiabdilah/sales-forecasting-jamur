package com.example.salesforecastingapps.ui.prediksi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingapps.data.model.Prediksi
import com.example.salesforecastingapps.databinding.ItemPrediksiBinding
import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.FormatUtils

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
            val periode = item.periodePrediksi?.let { DateUtils.formatPeriode(it) } ?: "-"

            binding.tvPeriodePrediksi.text = "$periode"
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