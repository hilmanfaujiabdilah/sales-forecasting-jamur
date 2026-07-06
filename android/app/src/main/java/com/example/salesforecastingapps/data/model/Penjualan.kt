package com.example.salesforecastingapps.data.model

import com.google.gson.annotations.SerializedName

data class Penjualan(
    @SerializedName("penjualan_id") val penjualanId: Int,
    @SerializedName("tanggal_penjualan") val tanggal: String,
    @SerializedName("jumlah_penjualan") val jumlah: Float,
    @SerializedName("kumbung_id") val kumbungId: Int,
    @SerializedName("nama_kumbung") val namaKumbung: String,
    @SerializedName("prediksi_id") val prediksiId: Int,
    @SerializedName("create_at") val createAt: String
)

data class PenjualanRequest(
    @SerializedName("tanggal_penjualan") val tanggal: String,
    @SerializedName("jumlah_penjualan") val jumlah: Float,
    @SerializedName("kumbung_id") val kumbungId: Int,
    @SerializedName("prediksi_id") val prediksiId: Int
)

//data class AgregasiBulanan(
//    @SerializedName("periode") val periode: String,
//    @SerializedName("total_penjualan") val totalPenjualan: Float,
//    @SerializedName("rekomendasi_id") val rekomendasiId: Int? = null
//)

data class AgregasiBulanan(
    @SerializedName("periode") val periode: String,
    @SerializedName("total_penjualan") val totalPenjualan: Float = 0f,
    @SerializedName("total_produksi") val totalProduksi: Float = 0f,  // ← tambah ini
    @SerializedName("bulan") val bulan: Int = 0,
    @SerializedName("tahun") val tahun: Int = 0,
    @SerializedName("rekomendasi_id") val rekomendasiId: Int? = null
)

data class Periode(
    val bulan: Int,
    val tahun: Int,
    val rekomendasiId: Int? = null
)