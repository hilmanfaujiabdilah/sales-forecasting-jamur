package com.example.salesforecastingapps.data.repository

import com.example.salesforecastingapps.data.model.Kumbung
import com.example.salesforecastingapps.data.model.KumbungRequest
import com.example.salesforecastingapps.data.model.Penjualan
import com.example.salesforecastingapps.data.model.PenjualanRequest
import com.example.salesforecastingapps.data.model.Periode
import com.example.salesforecastingapps.data.model.Prediksi
import com.example.salesforecastingapps.data.remote.RetrofitClient
import com.example.salesforecastingapps.data.remote.RetrofitClient.apiService
import com.example.salesforecastingapps.utils.Result

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

    suspend fun tambahKumbung(namaKumbung: String): Result<Kumbung> = try {
        val res = api.tambahKumbung(KumbungRequest(namaKumbung))
        if (res.isSuccessful && res.body()?.data != null)
            Result.Success(res.body()!!.data!!)
        else
            Result.Error(res.body()?.pesan ?: "Gagal menambahkan kumbung")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun getPrediksiByPeriode(bulan: Int, tahun: Int): Result<Prediksi> {
        return try {
            val response = apiService.getPrediksiByPeriode(bulan, tahun)
            val data = response.body()?.data
            if (response.isSuccessful && !data.isNullOrEmpty()) {
                Result.Success(data.first())
            } else {
                Result.Error("Prediksi tidak ditemukan untuk periode ini")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun getAllPrediksi(): Result<List<Prediksi>> = try {
        val res = api.getAllPrediksi()
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil data prediksi")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun hapusPenjualan(penjualanId: Int): Result<String> {
        return try {
            val response = api.hapusPenjualan(penjualanId)
            if (response.isSuccessful)
                Result.Success(response.body()?.pesan ?: "Berhasil dihapus")
            else
                Result.Error(response.errorBody()?.string() ?: "Gagal menghapus")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Kesalahan jaringan")
        }
    }
 }