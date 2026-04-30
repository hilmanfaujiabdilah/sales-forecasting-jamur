package com.example.salesforecastingjamur.ui.penjualan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingjamur.data.model.*
import com.example.salesforecastingjamur.data.repository.PenjualanRepository
import com.example.salesforecastingjamur.utils.Result
import kotlinx.coroutines.launch

class PenjualanViewModel : ViewModel() {

    private val repo = PenjualanRepository()

    private val _periodeList = MutableLiveData<Result<List<Periode>>>()
    val periodeList: LiveData<Result<List<Periode>>> = _periodeList

    private val _penjualanBulanan = MutableLiveData<Result<List<Penjualan>>>()
    val penjualanBulanan: LiveData<Result<List<Penjualan>>> = _penjualanBulanan

    private val _agregasiBulanan = MutableLiveData<Result<List<AgregasiBulanan>>>()
    val agregasiBulanan: LiveData<Result<List<AgregasiBulanan>>> = _agregasiBulanan

    private val _kumbungList = MutableLiveData<Result<List<Kumbung>>>()
    val kumbungList: LiveData<Result<List<Kumbung>>> = _kumbungList

    private val _simpanResult = MutableLiveData<Result<String>>()
    val simpanResult: LiveData<Result<String>> = _simpanResult

    fun loadPeriode() = viewModelScope.launch {
        _periodeList.value = Result.Loading
        _periodeList.value = repo.getAllPeriode()
    }

    fun loadPenjualanBulanan(bulan: Int, tahun: Int) = viewModelScope.launch {
        _penjualanBulanan.value = Result.Loading
        val all = repo.getAllPenjualan()
        if (all is Result.Success) {
            val filtered = all.data.filter {
                val parts = it.tanggal.split("-")
                parts[0].toInt() == tahun && parts[1].toInt() == bulan
            }
            _penjualanBulanan.value = Result.Success(filtered)
        } else {
            _penjualanBulanan.value = Result.Error("Gagal memuat penjualan")
        }
    }

    fun loadAgregasiBulanan() = viewModelScope.launch {
        _agregasiBulanan.value = Result.Loading
        val all = repo.getAllPenjualan()
        if (all is Result.Success) {
            val agregasi = all.data
                .groupBy { it.tanggal.substring(0, 7) }
                .map { (periode, list) ->
                    AgregasiBulanan(
                        periode = periode,
                        totalPenjualan = list.sumOf { it.jumlah.toDouble() }.toFloat()
                    )
                }.sortedBy { it.periode }
            _agregasiBulanan.value = Result.Success(agregasi)
        } else {
            _agregasiBulanan.value = Result.Error("Gagal memuat grafik")
        }
    }

    fun loadKumbung() = viewModelScope.launch {
        _kumbungList.value = repo.getAllKumbung()
    }

    fun simpanPenjualan(request: PenjualanRequest) = viewModelScope.launch {
        _simpanResult.value = Result.Loading
        _simpanResult.value = repo.simpanPenjualan(request)
    }
}
