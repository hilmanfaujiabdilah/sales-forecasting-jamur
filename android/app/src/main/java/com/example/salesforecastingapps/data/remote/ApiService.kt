package com.example.salesforecastingapps.data.remote
import com.example.salesforecastingapps.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

//    API Kumbung
    @GET("api/kumbung")
    suspend fun getAllKumbung(): Response<ApiResponse<List<Kumbung>>>

    @POST("api/kumbung")
    suspend fun tambahKumbung(@Body request: KumbungRequest): Response<ApiResponse<Kumbung>>

//    Penjualan
    @GET("api/penjualan")
    suspend fun getAllPenjualan(): Response<ApiResponse<List<Penjualan>>>

    @GET("api/penjualan/periode")
    suspend fun getAllPeriode(): Response<ApiResponse<List<Periode>>>

    @POST("api/penjualan")
    suspend fun simpanPenjualan(@Body request: PenjualanRequest): Response<ApiResponse<Unit>>

    @DELETE("api/penjualan/{id}")
    suspend fun hapusPenjualan(
        @Path("id") penjualanId: Int
    ): Response<ApiResponse<String>>

//    Prediksi
    @GET("api/prediksi")
    suspend fun getAllPrediksi(): Response<ApiResponse<List<Prediksi>>>

    @GET("api/prediksi/periode")
    suspend fun getPrediksiByPeriode(
        @Query("bulan") bulan: Int,
        @Query("tahun") tahun: Int
    ): Response<ApiResponse<List<Prediksi>>>

    @POST("api/prediksi/proses")
    suspend fun prosesPrediksi(@Body request: PrediksiRequest): Response<ApiResponse<HasilPrediksi>>

    @GET("api/prediksi/periode-tersedia")
    suspend fun getPeriodeTersedia(): Response<ApiResponse<List<PeriodeTersedia>>>

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

    @DELETE("api/produksi/{id}")
    suspend fun hapusProduksi(
        @Path("id") produksiId: Int
    ): Response<ApiResponse<String>>

    @POST("api/produksi")
    suspend fun simpanProduksi(@Body request: ProduksiRequest): Response<ApiResponse<Unit>>

    @GET("api/produksi/agregasi-bulanan")
    suspend fun getAllProduksi(): Response<ApiResponse<List<AgregasiBulanan>>>

    @GET("api/produksi/periode-dengan-rekomendasi")
    suspend fun getPeriodeDenganRekomendasi(): Response<ApiResponse<List<PeriodeRekomendasi>>>
}