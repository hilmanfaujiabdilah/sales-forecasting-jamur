package com.example.salesforecastingapps.ui.produksi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingapps.R
import com.example.salesforecastingapps.databinding.FragmentProduksiBinding
import com.example.salesforecastingapps.ui.penjualan.PeriodeAdapter
import com.example.salesforecastingapps.utils.ChartHelper
import com.example.salesforecastingapps.utils.Result

class ProduksiFragment : Fragment() {

    private var _binding: FragmentProduksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProduksiViewModel by viewModels()
    private lateinit var periodeAdapter: PeriodeAdapter

//    private val viewModel: ProduksiViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProduksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapeter()
        setupObservers()
        viewModel.loadPeriodeProduksi()
        viewModel.loadAgregasiBulanan()
//        loadPeriodeProduksi()

//        viewModel.loadAgregasiBulanan()

        binding.swipeRefresh.setColorSchemeResources(R.color.hijau_utama)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPeriodeProduksi()
            viewModel.loadAgregasiBulanan()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupAdapeter(){
        periodeAdapter = PeriodeAdapter { periode ->
            val action = ProduksiFragmentDirections
                .actionProduksiToDetail(
                    periode.bulan,
                    periode.tahun,
                    periode.rekomendasiId ?: -1)
            findNavController().navigate(
                action
            )
        }
        binding.rvPeriode.apply {
            adapter = periodeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.periodeProduksi.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
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
            when (result) {
                is Result.Success -> ChartHelper.setupBarChart(binding.chartAgregasi, result.data)
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
