package com.example.salesforecastingjamur.ui.produksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingjamur.data.model.*
import com.example.salesforecastingjamur.data.repository.ProduksiRepository
import com.example.salesforecastingjamur.utils.Result
import kotlinx.coroutines.launch

class ProduksiViewModel : ViewModel() {

    private val repo = ProduksiRepository()

    private val _produksiBulanan = MutableLiveData<Result<List<Produksi>>>()
    val produksiBulanan: LiveData<Result<List<Produksi>>> = _produksiBulanan

    private val _simpanResult = MutableLiveData<Result<String>>()
    val simpanResult: LiveData<Result<String>> = _simpanResult

    fun loadProduksiBulanan(bulan: Int, tahun: Int) = viewModelScope.launch {
        _produksiBulanan.value = Result.Loading
        _produksiBulanan.value = repo.getProduksiByPeriode(bulan, tahun)
    }

    fun simpanProduksi(request: ProduksiRequest) = viewModelScope.launch {
        _simpanResult.value = Result.Loading
        _simpanResult.value = repo.simpanProduksi(request)
    }
}
