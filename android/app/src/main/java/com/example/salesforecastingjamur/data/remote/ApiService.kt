package com.example.salesforecastingjamur.data.remote
import com.example.salesforecastingjamur.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

//    API Kumbung
    @GET("api/kumbung")
    suspend fun getAllKumbung(): Response<ApiResponse<List<Kumbung>>>

//    Penjualan
    @GET("api/penjualan")
    suspend fun getAllPenjualan(): Response<ApiResponse<List<Penjualan>>>

    @GET("api/penjualan/periode")
    suspend fun getAllPeriode(): Response<ApiResponse<List<Periode>>>

    @POST("api/penjualan")
    suspend fun simpanPenjualan(@Body request: PenjualanRequest): Response<ApiResponse<Unit>>

//    Prediksi
    @GET("api/prediksi")
    suspend fun getAllPrediksi(): Response<ApiResponse<List<Prediksi>>>

    @POST("api/prediksi/proses")
    suspend fun prosesPrediksi(): Response<ApiResponse<HasilPrediksi>>

    @GET("api/prediksi/rekomendasi/{prediksi_id}")
    suspend fun getRekomendasiByPrediksi(@Path("prediksi_id") prediksiId: Int): Response<ApiResponse<Rekomendasi>>

    @GET("api/prediksi/bahan-baku/{rekomendasi_id}")
    suspend fun getBahanBakuByRekomendasi(@Path("rekomendasi_id") rekomendasiId: Int): Response<ApiResponse<List<BahanBaku>>>

//    Produksi
    @GET("api/produksi/periode")
    suspend fun getProduksiByPeriode(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int,
    ): Response<ApiResponse<List<Produksi>>>

    @POST("api/produksi")
    suspend fun simpanProduksi(@Body request: ProduksiRequest): Response<ApiResponse<Unit>>
}

