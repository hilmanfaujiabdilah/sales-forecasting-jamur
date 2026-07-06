package com.example.salesforecastingapps.data.model

import com.google.gson.annotations.SerializedName

data class Produksi(
    @SerializedName("produksi_id") val produksiId: Int,
    @SerializedName("tanggal_produksi") val tanggalProduksi: String,
    @SerializedName("jumlah_produksi") val jumlahProduksi: Float,
    @SerializedName("rekomendasi_id") val rekomendasiId: Int,
    @SerializedName("est_kebutuhan_baglog") val estKebutuhanBaglog: Int?,
    @SerializedName("baglog_baru") val baglogBaru: Int?,
    @SerializedName("created_at") val createdAt: String,
)

data class ProduksiRequest(
    @SerializedName("tanggal_produksi") val tanggalProduksi: String,
    @SerializedName("jumlah_produksi") val jumlah: Float,
    @SerializedName("rekomendasi_id") val rekomendasiId: Int?
)

data class PeriodeRekomendasi(
    @SerializedName("bulan") val bulan: Int,
    @SerializedName("tahun") val tahun: Int,
    @SerializedName("rekomendasi_id") val rekomendasiId: Int? = null
)