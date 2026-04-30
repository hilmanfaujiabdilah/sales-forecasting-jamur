package com.example.salesforecastingjamur.data.model

data class ApiResponse<T>(
    val status: String,
    val pesan: String,
    val data: T? = null
)