package com.example.salesforecastingjamur.data.repository

import com.example.salesforecastingjamur.data.model.Produksi
import com.example.salesforecastingjamur.data.model.ProduksiRequest
import com.example.salesforecastingjamur.data.remote.RetrofitClient
import com.example.salesforecastingjamur.utils.Result


class ProduksiRepository {

    private val api = RetrofitClient.apiService

    suspend fun getProduksiByPeriode(bulan: Int, tahun: Int): Result<List<Produksi>> = try {
        val res = api.getProduksiByPeriode(bulan, tahun)
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil data produksi")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun simpanProduksi(request: ProduksiRequest) = try {
        val res = api.simpanProduksi(request)
        if (res.isSuccessful) Result.Success(res.body()?.pesan ?: "Berhasil disimpan")
        else Result.Error(res.errorBody()?.string() ?: "Gagal menyimpan")
    } catch (e: Exception) { Result.Error(e.message ?: "Kesalahan jaringan") }
}