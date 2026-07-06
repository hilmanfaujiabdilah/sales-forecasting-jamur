package com.example.salesforecastingapps.ui.penjualan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingapps.R
import com.example.salesforecastingapps.data.model.Periode
import com.example.salesforecastingapps.databinding.FragmentPenjualanBinding
import com.example.salesforecastingapps.utils.ChartHelper
import com.example.salesforecastingapps.utils.Result

class PenjualanFragment : Fragment() {

    private var _binding: FragmentPenjualanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PenjualanViewModel by viewModels()
    private lateinit var periodeAdapter: PeriodeAdapter
    var periodeTerpilih: Periode? = null
    private var sudahNavigasi = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPenjualanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        viewModel.loadPeriode()
        viewModel.loadAgregasiBulanan()
    }

    private fun setupRecyclerView() {
        periodeAdapter = PeriodeAdapter { periode ->

            periodeTerpilih = periode
            sudahNavigasi = false
            viewModel.loadPrediksiByPeriode(periode.bulan, periode.tahun)
        }
        binding.rvPeriode.apply {
            adapter = periodeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.periodeList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEmpty.visibility     = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvPeriode.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvPeriode.visibility = View.VISIBLE
                        periodeAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = result.message
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            }
        }

        viewModel.agregasiBulanan.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                ChartHelper.setupBarChart(binding.chartAgregasi, result.data)
            }
        }

        viewModel.prediksiPeriode.observe(viewLifecycleOwner) { result ->
            if (sudahNavigasi) return@observe
            val periode = periodeTerpilih ?: return@observe

            if (result is Result.Loading) return@observe

            sudahNavigasi = true

            val prediksiId = if (result is Result.Success) result.data.prediksiId else -1
            val predNilai  = if (result is Result.Success) result.data.predPeriode1 else 0f
            val action = PenjualanFragmentDirections
                .actionPenjualanToDetail(periode.bulan, periode.tahun, prediksiId, predNilai)
            findNavController().navigate(action)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.hijau_utama)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPeriode()
            viewModel.loadAgregasiBulanan()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
