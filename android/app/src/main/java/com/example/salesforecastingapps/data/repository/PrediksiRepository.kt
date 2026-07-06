package com.example.salesforecastingapps.data.repository

import com.example.salesforecastingapps.data.model.BahanBaku
import com.example.salesforecastingapps.data.model.HasilPrediksi
import com.example.salesforecastingapps.data.model.PeriodeTersedia
import com.example.salesforecastingapps.data.model.Prediksi
import com.example.salesforecastingapps.data.model.PrediksiRequest
import com.example.salesforecastingapps.data.remote.RetrofitClient
import com.example.salesforecastingapps.utils.Result

class PrediksiRepository {
    private val api = RetrofitClient.apiService

    suspend fun getAllPrediksi(): Result<List<Prediksi>> = try {
        val res = api.getAllPrediksi()
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil data prediksi")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun prosesPrediksi(bulan: Int, tahun: Int): Result<HasilPrediksi> {
        return try {
            val res = api.prosesPrediksi(PrediksiRequest(bulan, tahun))
            if (res.isSuccessful && res.body()?.data != null) {
                Result.Success(res.body()!!.data!!)
            } else {
                val errorMsg = res.body()?.pesan
                    ?: res.errorBody()?.string()
                    ?: "Gagal memproses prediksi"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    suspend fun getRekomendasiByPrediksi(prediksiId: Int) = try {
        val res = api.getRekomendasiByPrediksi(prediksiId)
        if (res.isSuccessful)
            Result.Success(res.body()?.data)
        else
            Result.Error("Gagal mengambil rekomendasi")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun getBahanBakuByRekomendasi(rekomendasiId: Int): Result<List<BahanBaku>> = try {
        val res = api.getBahanBakuByRekomendasi(rekomendasiId)
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil bahan baku")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }

    suspend fun getPeriodeTersedia(): Result<List<PeriodeTersedia>> = try {
        val res = api.getPeriodeTersedia()
        if (res.isSuccessful)
            Result.Success(res.body()?.data ?: emptyList())
        else
            Result.Error("Gagal mengambil periode tersedia")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Kesalahan jaringan")
    }
}