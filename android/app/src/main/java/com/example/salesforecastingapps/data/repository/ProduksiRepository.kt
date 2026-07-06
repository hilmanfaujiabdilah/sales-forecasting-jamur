package com.example.salesforecastingapps.data.repository

import com.example.salesforecastingapps.data.model.AgregasiBulanan
import com.example.salesforecastingapps.data.model.Periode
import com.example.salesforecastingapps.data.model.Produksi
import com.example.salesforecastingapps.data.model.ProduksiRequest
import com.example.salesforecastingapps.data.remote.RetrofitClient
import com.example.salesforecastingapps.utils.Result


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

    suspend fun getProduksiAll(): Result<List<AgregasiBulanan>> {
        return try {
            val response = api.getAllProduksi()
            if (response.isSuccessful) {
                Result.Success(response.body()?.data ?: emptyList())
            } else {
                Result.Error("Gagal mengambil data")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error")
        }

    }

    suspend fun getPeriodeProduksi(): Result<List<Periode>> {
        return try {
            val response = api.getPeriodeDenganRekomendasi()
            if (response.isSuccessful) {
                val list = response.body()?.data?.map {
                    Periode(
                        bulan = it.bulan,
                        tahun = it.tahun,
                        rekomendasiId = it.rekomendasiId ?: -1  // null → -1
                    )
                } ?: emptyList()
                Result.Success(list)
            } else {
                Result.Error("Gagal mengambil periode produksi")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error")
        }
//        return try {
//            val response = api.getAllProduksi()
//            if (response.isSuccessful) {
//                val periodeList = response.body()?.data
//                    ?.mapNotNull { produksi ->
//                        try {
//                            val parts = produksi.periode.split("-")
//                            Periode(
//                                bulan = parts[1].toInt(),
//                                tahun = parts[0].toInt(),
//                                rekomendasiId = produksi.rekomendasiId
//                            )
//                        } catch (e: Exception) {
//                            null
//                        }
//                    }
//                    ?.distinctBy { "${it.tahun}-${it.bulan}"}
//                    ?: emptyList()
//                Result.Success(periodeList)
//            } else {
//                Result.Error("Gagal mengambil periode produksi")
//            }
//        } catch (e: Exception) {
//            Result.Error(e.message ?: "Error")
//        }
    }

    suspend fun hapusProduksi(produksiId: Int): Result<String> {
        return try {
            val response = api.hapusProduksi(produksiId)
            if (response.isSuccessful)
                Result.Success(response.body()?.pesan ?: "Berhasil dihapus")  // pesan dari ApiResponse
            else
                Result.Error(response.errorBody()?.string() ?: "Gagal menghapus")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Kesalahan jaringan")
        }
    }
}