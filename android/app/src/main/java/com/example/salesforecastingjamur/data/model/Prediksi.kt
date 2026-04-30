package com.example.salesforecastingjamur.data.model

import com.google.gson.annotations.SerializedName

data class Prediksi(
    @SerializedName("prediksi_id") val prediksiId: Int,
    @SerializedName("periode_pred1") val periodePred1: String,
    @SerializedName("preiode_perd2") val periodePred2: String,
    @SerializedName("pred_periode1") val predPeriode1: Float,
    @SerializedName("pred_periode2") val predPeriode2: Float,
    @SerializedName("slope") val slope: Float,
    @SerializedName("intercept") val intercept: Float,
    @SerializedName("nilai_mae") val nilaiMae: Float,
    @SerializedName("nilai_rmse") val nilaiRmse: Float,
    @SerializedName("nilai_mape") val nilaiMape: Float,
    @SerializedName("created_at") val createdAt: String,
)

data class Rekomendasi(
    @SerializedName("rekomendasi_id") val rekomendasiId: Int,
    @SerializedName("est_kebutuhan_baglog") val estKebutuhanBaglog: Int,
    @SerializedName("est_baglog_aktif") val estBaglogAktif: Int,
    @SerializedName("baglog_baru") val baglogBaru: Int,
    @SerializedName("prediksi_id") val prediksiId: Int,
)

data class BahanBaku(
    @SerializedName("nama_bahan_baku") val nama: String,
    @SerializedName("jumlah") val jumlah: Float,
    @SerializedName("satuan") val satuan: String,
)

data class HasilPrediksi(
    val prediksi: Prediksi,
    val rekomendasi: Rekomendasi,
    @SerializedName("bahan_baku") val bahanBaku: List<BahanBaku>
)