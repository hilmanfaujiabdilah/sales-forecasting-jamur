package com.example.salesforecastingapps.ui.penjualan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.salesforecastingapps.R
import com.example.salesforecastingapps.data.model.Kumbung

class KumbungDropdownAdapter(
    context: Context,
    private var kumbungList: MutableList<Kumbung> = mutableListOf()
) : ArrayAdapter<Any>(context, R.layout.list_item_dropdown) {

    companion object {
        const val ID_TAMBAH = -1
        val ITEM_TAMBAH = Kumbung(kumbungId = ID_TAMBAH, namaKumbung = "➕ Tambah Kumbung Baru")
    }

    private fun displayList(): List<Kumbung> = kumbungList + ITEM_TAMBAH

    fun submitList(data: List<Kumbung>) {
        kumbungList = data.toMutableList()
        notifyDataSetChanged()
    }

    fun addKumbung(kumbung: Kumbung) {
        kumbungList.add(kumbung)
        notifyDataSetChanged()
    }

    fun getKumbungList(): List<Kumbung> = kumbungList.toList()

    override fun getCount(): Int = displayList().size

    override fun getItem(position: Int): Kumbung = displayList()[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getFilter(): Filter = NoFilter()

    private inner class NoFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults().apply {
            values = displayList()
            count  = displayList().size
        }
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_dropdown, parent, false)

        val tv = view as TextView
        val item = displayList()[position]

        tv.text = item.namaKumbung

        if (item.kumbungId == ID_TAMBAH) {
            tv.setTextColor(context.getColor(R.color.hijau_utama))
            tv.setTypeface(tv.typeface, android.graphics.Typeface.BOLD)
        } else {
            tv.setTextColor(context.getColor(R.color.teks_utama))
            tv.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        return view
    }
}
