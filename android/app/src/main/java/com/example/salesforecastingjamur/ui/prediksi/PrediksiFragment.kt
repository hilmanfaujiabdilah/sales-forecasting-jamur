package com.example.salesforecastingjamur.ui.prediksi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingjamur.R
import com.example.salesforecastingjamur.databinding.FragmentPrediksiBinding
import com.example.salesforecastingjamur.utils.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class PrediksiFragment : Fragment() {

    private var _binding: FragmentPrediksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrediksiViewModel by viewModels()
    private lateinit var prediksiAdapter: PrediksiAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPrediksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupObservers()
        viewModel.loadPrediksiList()
    }

    private fun setupUI() {
        // Tampilkan info jika bukan akhir bulan
        if (!viewModel.isAkhirBulan) {
            binding.tvInfoAkhirBulan.visibility = View.VISIBLE
        }

        binding.btnProsesPrediksi.setOnClickListener {
            if (!viewModel.isAkhirBulan) {
                Snackbar.make(binding.root,
                    getString(R.string.prediksi_bukan_akhir_bulan),
                    Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            konfirmasiPrediksi()
        }
    }

    private fun konfirmasiPrediksi() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Prediksi")
            .setMessage("Proses prediksi penjualan untuk 2 periode ke depan?")
            .setPositiveButton("Proses") { _, _ -> viewModel.prosesPrediksi() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupRecyclerView() {
        prediksiAdapter = PrediksiAdapter { prediksi ->
            val action = PrediksiFragmentDirections
                .actionPrediksiToDetail(prediksi.prediksiId, -1)
            findNavController().navigate(action)
        }
        binding.rvPrediksi.apply {
            adapter = prediksiAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.prediksiList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    prediksiAdapter.submitList(result.data)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.hasilPrediksi.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, "Prediksi berhasil diproses!", Snackbar.LENGTH_SHORT).show()
                    viewModel.loadPrediksiList()
                    // Navigasi ke detail prediksi baru
                    val action = PrediksiFragmentDirections.actionPrediksiToDetail(
                        result.data.prediksi.prediksiId,
                        result.data.rekomendasi.rekomendasiId
                    )
                    findNavController().navigate(action)
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.hijau_utama)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPrediksiList()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
