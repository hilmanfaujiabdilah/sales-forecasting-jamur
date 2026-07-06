package com.example.salesforecastingapps.ui.prediksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingapps.data.model.*
import com.example.salesforecastingapps.data.repository.PrediksiRepository
//import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.Result
import kotlinx.coroutines.launch

class PrediksiViewModel : ViewModel() {

    private val repo = PrediksiRepository()

    private val _prediksiList = MutableLiveData<Result<List<Prediksi>>>()
    val prediksiList: LiveData<Result<List<Prediksi>>> = _prediksiList

    private val _hasilPrediksi = MutableLiveData<Result<HasilPrediksi>>()
    val hasilPrediksi: LiveData<Result<HasilPrediksi>> = _hasilPrediksi

    private val _rekomendasi = MutableLiveData<Result<Rekomendasi?>>()
    val rekomendasi: LiveData<Result<Rekomendasi?>> = _rekomendasi

    private val _bahanBaku = MutableLiveData<Result<List<BahanBaku>>>()
    val bahanBaku: LiveData<Result<List<BahanBaku>>> = _bahanBaku

//    private val _sudahAdaPrediksiBulanIni = MutableLiveData<Boolean>(false)
//    val sudahAdaPrediksiBulanIni: LiveData<Boolean> = _sudahAdaPrediksiBulanIni

    private val _periodeTersedia = MutableLiveData<Result<List<PeriodeTersedia>>>()
    val periodeTersedia: LiveData<Result<List<PeriodeTersedia>>> = _periodeTersedia

    var selectedPeriode: PeriodeTersedia? = null

    private val _selectedBulan = MutableLiveData<Int?>()
    private val _selectedTahun = MutableLiveData<Int?>()

//    fun setBulanTahun(bulan: Int, tahun: Int) {
//        _selectedBulan.value = bulan
//        _selectedTahun.value = tahun
//    }
//
//    fun cekPrediksiBulanIni() = viewModelScope.launch {
//        val result = repo.getAllPrediksi()
//        if (result is Result.Success) {
//            val bulananDepan = DateUtils.bulanDepan()
//            val sudahAda = result.data.any { prediksi ->
//                prediksi.periodePrediksi?.startsWith(bulananDepan) == true
//            }
//            _sudahAdaPrediksiBulanIni.value = sudahAda
//        }
//    }

    fun loadPrediksiList() = viewModelScope.launch {
        _prediksiList.value = Result.Loading
        val result = repo.getAllPrediksi()

        if (result is Result.Success) {
            val sorted = result.data.sortedBy { it.periodePrediksi }
            _prediksiList.value = Result.Success(sorted)
        } else {
            _prediksiList.value = result
        }
    }

    fun prosesPrediksi(bulan: Int, tahun: Int) = viewModelScope.launch {
        _hasilPrediksi.value = Result.Loading
        val result = repo.prosesPrediksi(bulan, tahun)
        when (result) {
            is Result.Success -> {
                _hasilPrediksi.value = Result.Success(result.data)
                loadPrediksiList()
                loadPeriodeTersedia()   // ← refresh dropdown setelah prediksi
            }
            is Result.Error -> _hasilPrediksi.value = Result.Error(result.message)
            else -> {}
        }
    }

    fun loadRekomendasi(prediksiId: Int) = viewModelScope.launch {
        _rekomendasi.value = Result.Loading
        _rekomendasi.value = repo.getRekomendasiByPrediksi(prediksiId)
    }

    fun loadBahanBaku(rekomendasiId: Int) = viewModelScope.launch {
        _bahanBaku.value = Result.Loading
        _bahanBaku.value = repo.getBahanBakuByRekomendasi(rekomendasiId)
    }

    fun loadPeriodeTersedia() = viewModelScope.launch {
        _periodeTersedia.value = Result.Loading
        _periodeTersedia.value = repo.getPeriodeTersedia()
    }

//    fun prosesPrediksi() = viewModelScope.launch {
//        val bulan = _selectedBulan.value ?: return@launch
//        val tahun = _selectedTahun.value ?: return@launch
//
//        _hasilPrediksi.value = Result.Loading
//        val result = repo.prosesPrediksi(bulan, tahun)
//        when (result) {
//            is Result.Success -> {
//                _hasilPrediksi.value = Result.Success(result.data)
//                loadPrediksiList()
//                loadPeriodeTersedia()
//            }
//            is Result.Error -> _hasilPrediksi.value = Result.Error(result.message)
//            else -> {}
//        }
//    }
}