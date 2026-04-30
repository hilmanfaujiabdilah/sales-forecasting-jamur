package com.example.salesforecastingjamur.data.repository

import com.example.salesforecastingjamur.data.model.Kumbung
import com.example.salesforecastingjamur.data.model.Penjualan
import com.example.salesforecastingjamur.data.model.PenjualanRequest
import com.example.salesforecastingjamur.data.model.Periode
import com.example.salesforecastingjamur.data.remote.RetrofitClient
import com.example.salesforecastingjamur.utils.Result

class PenjualanRepository {

    private val api = RetrofitClient.apiService

    suspend fun getAllPenjualan(): Result<List<Penjualan>> = try {
        val res = api.getAllPenjualan()
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil data penjualan")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun getAllPeriode(): Result<List<Periode>> = try {
        val res = api.getAllPeriode()
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil periode")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun simpanPenjualan(request: PenjualanRequest)= try {
        val res = api.simpanPenjualan(request)
        if (res.isSuccessful)
            Result.Success(res.body()?.pesan ?: "Behasil disimpan")
        else
            Result.Error(res.errorBody()?.string() ?: "Gagal menyimpan")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun getAllKumbung(): Result<List<Kumbung>> = try {
        val res = api.getAllKumbung()
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil data")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }
 }