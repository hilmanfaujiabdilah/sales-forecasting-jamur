package com.example.salesforecastingjamur.data.model

import com.google.gson.annotations.SerializedName

data class Kumbung(
    @SerializedName("kumbung_id") val kumbungId: Int,
    @SerializedName("nama_kumbung") val namaKumbung: String
)