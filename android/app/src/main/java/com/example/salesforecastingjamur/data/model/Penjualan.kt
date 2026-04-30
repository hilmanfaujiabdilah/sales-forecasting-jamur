package com.example.salesforecastingjamur.data.model

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

data class AgregasiBulanan(
    val periode: String,
    @SerializedName("total_penjualan") val totalPenjualan: Float
)

data class Periode(
    val bulan: Int,
    val tahun: Int
)