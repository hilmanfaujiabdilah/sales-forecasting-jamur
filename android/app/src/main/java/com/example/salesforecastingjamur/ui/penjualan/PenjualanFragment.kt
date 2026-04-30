package com.example.salesforecastingjamur.ui.penjualan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingjamur.R
import com.example.salesforecastingjamur.databinding.FragmentPenjualanBinding
import com.example.salesforecastingjamur.utils.ChartHelper
import com.example.salesforecastingjamur.utils.Result

class PenjualanFragment : Fragment() {

    private var _binding: FragmentPenjualanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PenjualanViewModel by viewModels()
    private lateinit var periodeAdapter: PeriodeAdapter

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
            val action = PenjualanFragmentDirections
                .actionPenjualanToDetail(periode.bulan, periode.tahun, -1, 0f)
            findNavController().navigate(action)
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
