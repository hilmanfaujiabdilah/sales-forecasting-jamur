package com.example.salesforecastingjamur.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.salesforecastingjamur.databinding.FragmentBerandaBinding
import com.example.salesforecastingjamur.utils.ChartHelper
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.FormatUtils
import com.example.salesforecastingjamur.utils.Result
import com.example.salesforecastingjamur.R

class BerandaFragment: Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BerandaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTanggalHariIni.text = DateUtils.formatTanggal(DateUtils.today())

        setupObservers()
        setupSwipeRefresh()
        viewModel.loadData()
    }

    private fun setupObservers() {
        viewModel.agregasiBulanan.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val prediksi = (viewModel.prediksiTerbaru.value as? Result.Success)?.data
                    ChartHelper.setupLineChart(binding.chartPenjualan, result.data, prediksi)

                    // Hitung total penjualan bulan ini
                    val bulanIni = DateUtils.today().substring(0, 7)
                    val totalBulanIni = result.data
                        .filter { it.periode.startsWith(bulanIni) }
                        .sumOf { it.totalPenjualan.toDouble() }.toFloat()
                    binding.tvPenjualanAktual.text = FormatUtils.formatKg(totalBulanIni)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.prediksiTerbaru.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                result.data?.let { prediksi ->
                    binding.tvPenjualanPrediksi.text = FormatUtils.formatKg(prediksi.predPeriode1)
                    binding.tvBaglogRekomendasi.text  = "- baglog"
                    binding.tvBaglogBaru.text         = "- baglog"
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.hijau_utama
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}