package com.example.salesforecastingjamur.ui.beranda

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingjamur.data.model.AgregasiBulanan
import com.example.salesforecastingjamur.data.model.Prediksi
import com.example.salesforecastingjamur.data.repository.PenjualanRepository
import com.example.salesforecastingjamur.data.repository.PrediksiRepository
import com.example.salesforecastingjamur.utils.Result
import kotlinx.coroutines.launch

class BerandaViewModel : ViewModel() {

    private val penjualanRepo = PenjualanRepository()
    private val prediksiRepo  = PrediksiRepository()

    private val _agregasiBulanan = MutableLiveData<Result<List<AgregasiBulanan>>>()
    val agregasiBulanan: LiveData<Result<List<AgregasiBulanan>>> = _agregasiBulanan

    private val _prediksiTerbaru = MutableLiveData<Result<Prediksi?>>()
    val prediksiTerbaru: LiveData<Result<Prediksi?>> = _prediksiTerbaru

    fun loadData() {
        loadAgregasiBulanan()
        loadPrediksiTerbaru()
    }

    private fun loadAgregasiBulanan() = viewModelScope.launch {
        _agregasiBulanan.value = Result.Loading
        val penjualan = penjualanRepo.getAllPenjualan()
        if (penjualan is Result.Success) {
            // Agregasi di sisi Android: kelompokkan per bulan, ambil 12 terakhir
            val agregasi = penjualan.data
                .groupBy { it.tanggal.substring(0, 7) } // YYYY-MM
                .map { (periode, list) ->
                    AgregasiBulanan(
                        periode = periode,
                        totalPenjualan = list.sumOf { it.jumlah.toDouble() }.toFloat()
                    )
                }
                .sortedBy { it.periode }
                .takeLast(12)
            _agregasiBulanan.value = Result.Success(agregasi)
        } else {
            _agregasiBulanan.value = Result.Error("Gagal memuat data grafik")
        }
    }

    private fun loadPrediksiTerbaru() = viewModelScope.launch {
        _prediksiTerbaru.value = Result.Loading
        val result = prediksiRepo.getAllPrediksi()
        if (result is Result.Success) {
            _prediksiTerbaru.value = Result.Success(result.data.firstOrNull())
        } else {
            _prediksiTerbaru.value = Result.Error("Gagal memuat prediksi")
        }
    }
}