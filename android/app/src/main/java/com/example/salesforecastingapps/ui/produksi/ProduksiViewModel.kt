package com.example.salesforecastingapps.ui.produksi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingapps.data.model.*
import com.example.salesforecastingapps.data.repository.ProduksiRepository
import com.example.salesforecastingapps.utils.Result
import kotlinx.coroutines.launch

class ProduksiViewModel : ViewModel() {

    private val repo = ProduksiRepository()

    private val _produksiBulanan = MutableLiveData<Result<List<Produksi>>>()
    val produksiBulanan: LiveData<Result<List<Produksi>>> = _produksiBulanan

    private val _simpanResult = MutableLiveData<Result<String>>()
    val simpanResult: LiveData<Result<String>> = _simpanResult

    private val _agregasiBulanan = MutableLiveData<Result<List<AgregasiBulanan>>>()
    val agregasiBulanan: LiveData<Result<List<AgregasiBulanan>>> = _agregasiBulanan

    private val _periodeProduksi = MutableLiveData<Result<List<Periode>>>()
    val periodeProduksi: LiveData<Result<List<Periode>>> = _periodeProduksi

    private val _hapusResult = MutableLiveData<Result<String>>()
    val hapusResult: LiveData<Result<String>> = _hapusResult

    fun loadProduksiBulanan(bulan: Int, tahun: Int) = viewModelScope.launch {
        _produksiBulanan.value = Result.Loading
        _produksiBulanan.value = repo.getProduksiByPeriode(bulan, tahun)
    }

    fun simpanProduksi(request: ProduksiRequest) = viewModelScope.launch {
        _simpanResult.value = Result.Loading
        _simpanResult.value = repo.simpanProduksi(request)
    }

    fun loadAgregasiBulanan() = viewModelScope.launch {
        _agregasiBulanan.value = Result.Loading
        val result = repo.getProduksiAll()
        if (result is Result.Success) {
            val filtered = result.data
                .sortedBy { it.periode }
                .takeLast(24) // ← ambil 24 bulan terakhir
            _agregasiBulanan.value = Result.Success(filtered)
        } else {
            _agregasiBulanan.value = result
        }
    }

    fun loadPeriodeProduksi() = viewModelScope.launch {

        _periodeProduksi.value = Result.Loading
        _periodeProduksi.value = repo.getPeriodeProduksi()
    }

    fun hapusProduksi(produksiId: Int) = viewModelScope.launch {
        _hapusResult.value = Result.Loading
        _hapusResult.value = repo.hapusProduksi(produksiId)
    }
}
