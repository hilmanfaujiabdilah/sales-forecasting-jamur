package com.example.salesforecastingapps.ui.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.salesforecastingapps.databinding.FragmentBerandaBinding
import com.example.salesforecastingapps.utils.ChartHelper
import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.FormatUtils
import com.example.salesforecastingapps.utils.Result
import com.example.salesforecastingapps.R

class BerandaFragment : Fragment() {

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

        // Tanggal hari ini (sudah ada sebelumnya)
        binding.tvTanggalHariIni.text = DateUtils.formatTanggal(DateUtils.today())

        // Isi semua label dinamis berbasis bulan/tahun sekarang
        setupLabelDinamis()

        setupObservers()
        setupSwipeRefresh()
        viewModel.loadData()
    }

    /**
     * Mengisi label-label yang bergantung pada bulan/tahun saat ini:
     * - tvPeriodeRingkasan   : "April 2026"
     * - tvLabelBaglogAktif   : "di bulan Mei 2026"   (bulan+1)
     * - tvLabelEstKebutuhan  : "di bulan Mei 2026"   (bulan+1)
     * - tvLabelSudahDiproduksi: "di bulan April 2026" (bulan ini)
     * - tvLabelBaglogBaru    : "di bulan April 2026"  (bulan ini)
     */
    private fun setupLabelDinamis() {
        val cal           = java.util.Calendar.getInstance()
        val bulanSekarang = cal.get(java.util.Calendar.MONTH) + 1
        val tahunSekarang = cal.get(java.util.Calendar.YEAR)
        val bulanDepan    = if (bulanSekarang == 12) 1 else bulanSekarang + 1
        val tahunDepan    = if (bulanSekarang == 12) tahunSekarang + 1 else tahunSekarang

        binding.tvPeriodeRingkasan.text =
            "${namaBulan(bulanSekarang)} $tahunSekarang"

        binding.tvLabelBaglogAktif.text =
            "di bulan ${namaBulan(bulanDepan)} $tahunDepan"

        binding.tvLabelEstKebutuhan.text =
            "di bulan ${namaBulan(bulanDepan)} $tahunDepan"

        binding.tvLabelSudahDiproduksi.text =
            "di bulan ${namaBulan(bulanSekarang)} $tahunSekarang"

        binding.tvLabelBaglogBaru.text =
            "di bulan ${namaBulan(bulanSekarang)} $tahunSekarang"
    }

    private fun setupObservers() {

        viewModel.agregasiBulanan.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.progressBar.visibility = View.VISIBLE

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    updateChart()

                    // Subtitle periode grafik: "Mei 2024 – April 2026"
                    if (result.data.isNotEmpty()) {
                        val periodeAwal  = result.data.first().periode.substring(0, 7)
                        val periodeAkhir = result.data.last().periode.substring(0, 7)
                        binding.tvPeriodeGrafik.text =
                            "${formatPeriodeLabel(periodeAwal)} – ${formatPeriodeLabel(periodeAkhir)}"
                    }

                    // Total penjualan bulan ini
                    val bulanIni = DateUtils.today().substring(0, 7)
                    val totalBulanIni = result.data
                        .filter { it.periode.startsWith(bulanIni) }
                        .sumOf { it.totalPenjualan.toDouble() }.toFloat()
                    binding.tvPenjualanAktual.text = FormatUtils.formatKg(totalBulanIni)
                }

                is Result.Error -> binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.prediksiPeriodeIni.observe(viewLifecycleOwner) { prediksi ->
            if (prediksi != null) {
                binding.tvPenjualanPrediksi.text = FormatUtils.formatKg(prediksi.predPeriode1)
            } else {
                binding.tvPenjualanPrediksi.text = "0,0 Kg"
            }
        }

        viewModel.rekomendasiBulanIni.observe(viewLifecycleOwner) { rekomendasi ->
            if (rekomendasi != null) {
                binding.tvBaglogAktif.text   = "${rekomendasi.estBaglogAktif} baglog"
                binding.tvEstKebutuhan.text  = "${rekomendasi.estKebutuhanBaglog} baglog"
                binding.tvBaglogBaru.text    = "${rekomendasi.baglogBaru} baglog"
            } else {
                binding.tvBaglogAktif.text   = "0 baglog"
                binding.tvEstKebutuhan.text  = "0 baglog"
                binding.tvBaglogBaru.text    = "0 baglog"
            }
        }

        viewModel.produksiAktualBulanIni.observe(viewLifecycleOwner) { sudahProduksi ->
            val kebutuhan = (viewModel.rekomendasiBulanIni.value?.estKebutuhanBaglog ?: 0)
            binding.tvSudahDiproduksi.text = "$sudahProduksi baglog"
            if (kebutuhan > 0) {
                val persen = (sudahProduksi * 100 / kebutuhan).coerceIn(0, 100)
                binding.progressProduksi.progress = persen
                binding.tvPersenProduksi.text = "$persen% dari kebutuhan"
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.hijau_utama)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData() // reload setiap kali kembali ke beranda
    }

    private fun updateChart() {
        val agregasi = (viewModel.agregasiBulanan.value as? Result.Success)?.data ?: return
        val prediksiList = (viewModel.prediksiTerbaru.value as? Result.Success)?.data ?: emptyList()

        val periodeAktual = agregasi.map { it.periode.substring(0, 7) }.toSet()
        val prediksiFiltered = prediksiList.filter {
            it.periodePrediksi?.substring(0, 7) in periodeAktual
        }

        ChartHelper.setupLineChart(binding.chartPenjualan, agregasi, prediksiFiltered)
    }

    // -------------------------------------------------------------------------
    // Helper functions
    // -------------------------------------------------------------------------

    private fun formatPeriodeLabel(yyyyMM: String): String {
        return try {
            val parts = yyyyMM.split("-")
            "${namaBulan(parts[1].toInt())} ${parts[0]}"
        } catch (e: Exception) {
            yyyyMM
        }
    }

    private fun namaBulan(bulan: Int): String {
        val nama = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        return nama[(bulan - 1).coerceIn(0, 11)]
    }
}