package com.example.salesforecastingjamur.ui.prediksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingjamur.data.model.*
import com.example.salesforecastingjamur.data.repository.PrediksiRepository
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.Result
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

    val isAkhirBulan: Boolean get() = DateUtils.isAkhirBulan()

    fun loadPrediksiList() = viewModelScope.launch {
        _prediksiList.value = Result.Loading
        _prediksiList.value = repo.getAllPrediksi()
    }

    fun prosesPrediksi() = viewModelScope.launch {
        _hasilPrediksi.value = Result.Loading
        val result = repo.prosesPrediksi()
        when (result) {
            is Result.Success -> _hasilPrediksi.value = Result.Success(result.data)
            is Result.Error   -> _hasilPrediksi.value = Result.Error(result.message)
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
}
