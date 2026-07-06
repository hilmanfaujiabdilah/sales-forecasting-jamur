package com.example.salesforecastingapps.ui.penjualan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salesforecastingapps.data.model.*
import com.example.salesforecastingapps.data.repository.PenjualanRepository
import com.example.salesforecastingapps.utils.Result
import com.example.salesforecastingapps.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class PenjualanViewModel : ViewModel() {

    private val repo = PenjualanRepository()

    private val _periodeList = MutableLiveData<Result<List<Periode>>>()
    val periodeList: LiveData<Result<List<Periode>>> = _periodeList

    private val _penjualanBulanan = MutableLiveData<Result<List<Penjualan>>>()
    val penjualanBulanan: LiveData<Result<List<Penjualan>>> = _penjualanBulanan

    private val _prediksiPeriode = MutableLiveData<Result<Prediksi>>()
    val prediksiPeriode: LiveData<Result<Prediksi>> = _prediksiPeriode

    private val _agregasiBulanan = MutableLiveData<Result<List<AgregasiBulanan>>>()
    val agregasiBulanan: LiveData<Result<List<AgregasiBulanan>>> = _agregasiBulanan

    private val _kumbungList = MutableLiveData<Result<List<Kumbung>>>()
    val kumbungList: LiveData<Result<List<Kumbung>>> = _kumbungList

    private val _simpanResult = SingleLiveEvent<Result<String>>()
    val simpanResult: LiveData<Result<String>> = _simpanResult

    private val _tambahKumbungResult = SingleLiveEvent<Result<Kumbung>>()
    val tambahKumbungResult: LiveData<Result<Kumbung>> = _tambahKumbungResult

    private val _hapusResult = SingleLiveEvent<Result<String>>()
    val hapusResult: LiveData<Result<String>> = _hapusResult


    fun loadPeriode() = viewModelScope.launch {
        _periodeList.value = Result.Loading

        val periodeResult = repo.getAllPeriode()
        val prediksiResult = repo.getAllPrediksi()

        if (periodeResult is Result.Success && prediksiResult is Result.Success) {
            val periodeFromPrediksi = prediksiResult.data.mapNotNull { prediksi ->
                val tgl = prediksi.periodePrediksi ?: return@mapNotNull null
                val parts = tgl.split("-")
                if (parts.size >= 2)
                    Periode(bulan = parts[1].toInt(), tahun = parts[0].toInt())
                else null
            }

            val semuaPeriode = (periodeResult.data + periodeFromPrediksi)
                .distinctBy { it.tahun * 100 + it.bulan }
                .sortedBy { it.tahun * 100 + it.bulan }

            _periodeList.value = Result.Success(semuaPeriode)
        } else {
            _periodeList.value = periodeResult
        }
    }

    fun loadPrediksiByPeriode(bulan: Int, tahun: Int) = viewModelScope.launch{
        _prediksiPeriode.value = Result.Loading
        _prediksiPeriode.value = repo.getPrediksiByPeriode(bulan, tahun)
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
                }
                .sortedBy { it.periode }
                .takeLast(24)
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

    fun tambahKumbung(namaKumbung: String) = viewModelScope.launch {
        _tambahKumbungResult.value = Result.Loading
        _tambahKumbungResult.value = repo.tambahKumbung(namaKumbung)
    }

    fun hapusPenjualan(penjualanId: Int) = viewModelScope.launch {
        _hapusResult.value = Result.Loading
        _hapusResult.value = repo.hapusPenjualan(penjualanId)
    }
}
