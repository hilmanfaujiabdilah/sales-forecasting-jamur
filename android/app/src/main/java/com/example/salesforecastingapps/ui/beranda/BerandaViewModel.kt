package com.example.salesforecastingapps.ui.beranda

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingapps.data.model.AgregasiBulanan
import com.example.salesforecastingapps.data.model.Prediksi
import com.example.salesforecastingapps.data.model.Rekomendasi
import com.example.salesforecastingapps.data.repository.PenjualanRepository
import com.example.salesforecastingapps.data.repository.PrediksiRepository
import com.example.salesforecastingapps.data.repository.ProduksiRepository
import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.Result
//import com.example.salesforecastingapps.utils.today
import kotlinx.coroutines.launch

class BerandaViewModel : ViewModel() {

    private val penjualanRepo = PenjualanRepository()
    private val prediksiRepo  = PrediksiRepository()
    private val produksiRepo = ProduksiRepository()

    private val _agregasiBulanan = MutableLiveData<Result<List<AgregasiBulanan>>>()
    val agregasiBulanan: LiveData<Result<List<AgregasiBulanan>>> = _agregasiBulanan

    private val _prediksiTerbaru = MutableLiveData<Result<List<Prediksi>>>()
    val prediksiTerbaru: LiveData<Result<List<Prediksi>>> = _prediksiTerbaru

    private val _prediksiPeriodeIni = MutableLiveData<Prediksi?>()
    val prediksiPeriodeIni: LiveData<Prediksi?> = _prediksiPeriodeIni

    private val _rekomendasiBulanIni = MutableLiveData<Rekomendasi?>()
    val rekomendasiBulanIni: LiveData<Rekomendasi?> = _rekomendasiBulanIni

    private val _produksiAktualBulanIni = MutableLiveData<Int>(0)
    val produksiAktualBulanIni: LiveData<Int> = _produksiAktualBulanIni


//    private val _prediksiTerbaru = MutableLiveData<Result<Prediksi?>>()
//    val prediksiTerbaru: LiveData<Result<Prediksi?>> = _prediksiTerbaru

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
                .takeLast(24)
            _agregasiBulanan.value = Result.Success(agregasi)
        } else {
            _agregasiBulanan.value = Result.Error("Gagal memuat data grafik")
        }
    }

    private fun loadPrediksiTerbaru() = viewModelScope.launch {
        _prediksiTerbaru.value = Result.Loading
        val result = prediksiRepo.getAllPrediksi()
        if (result is Result.Success) {
            val bulanIni = DateUtils.today().substring(0, 7)

            val unique = result.data
                .groupBy { it.periodePrediksi?.substring(0, 7) }
                .mapNotNull { (_, list) -> list.maxByOrNull { it.prediksiId } }
                .sortedBy { it.periodePrediksi }

            _prediksiTerbaru.value = Result.Success(unique)

            val preduksiBulanIni = unique.find {
                it.periodePrediksi?.startsWith(bulanIni) == true
            }
            _prediksiPeriodeIni.value = preduksiBulanIni

            preduksiBulanIni?.let { pred ->
                // Ambil rekomendasi
                val rek = prediksiRepo.getRekomendasiByPrediksi(pred.prediksiId)
                if (rek is Result.Success) {
                    _rekomendasiBulanIni.value = rek.data
                }

                // Ambil produksi aktual bulan ini
                val cal   = java.util.Calendar.getInstance()
                val bulan = cal.get(java.util.Calendar.MONTH) + 1
                val tahun = cal.get(java.util.Calendar.YEAR)

                val produksiResult = produksiRepo.getProduksiByPeriode(bulan, tahun)
                if (produksiResult is Result.Success) {
                    // Konversi kg ke baglog
                    // 1 baglog = 0.01 kg jamur → 1 kg = 100 baglog
                    val totalBaglog = produksiResult.data.sumOf { it.jumlahProduksi.toDouble() }.toInt()
                    _produksiAktualBulanIni.value = totalBaglog
                }
            }
        } else {
            _prediksiTerbaru.value = Result.Error("Gagal memuat prediksi")
        }
    }
}