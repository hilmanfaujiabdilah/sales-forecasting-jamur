package com.example.salesforecastingapps.ui.prediksi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingapps.R
import com.example.salesforecastingapps.databinding.FragmentPrediksiBinding
import com.example.salesforecastingapps.utils.Result
import com.google.android.material.button.MaterialButton
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
        setupRecyclerView()
        setupObservers()
        viewModel.loadPrediksiList()
        viewModel.loadPeriodeTersedia()
    }

    private fun konfirmasiPrediksi() {
        val periode = viewModel.selectedPeriode

        if (periode == null) {
            Snackbar.make(
                binding.root,
                "Pilih periode prediksi terlebih dahulu",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Prediksi")
            .setMessage("Proses prediksi untuk periode ${periode.label}?")
            .setPositiveButton("Proses") { _, _ ->
                viewModel.prosesPrediksi(periode.bulan, periode.tahun)
            }
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

        viewModel.periodeTersedia.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

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
                    Snackbar.make(binding.root, "Prediksi berhasil!", Snackbar.LENGTH_SHORT).show()
                    viewModel.loadPrediksiList()
                    viewModel.loadPeriodeTersedia()
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.hijau_utama)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPrediksiList()
            viewModel.loadPeriodeTersedia()
            binding.swipeRefresh.isRefreshing = false
        }

        // Tombol buka dialog prediksi
        binding.btnProsesPrediksi.setOnClickListener {
            tampilkanDialogPrediksi()
        }
    }

    private fun tampilkanDialogPrediksi() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_proses_prediksi, null)
        val actvPeriode = dialogView.findViewById<AutoCompleteTextView>(R.id.actvPeriode)
        val btnBatal    = dialogView.findViewById<MaterialButton>(R.id.btnBatal)
        val btnProses   = dialogView.findViewById<MaterialButton>(R.id.btnProses)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Observe periode tersedia untuk isi dropdown di dalam dialog
        viewModel.periodeTersedia.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                val labels = result.data.map { it.label }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    labels
                )
                actvPeriode.setAdapter(adapter)
                actvPeriode.setOnItemClickListener { _, _, position, _ ->
                    viewModel.selectedPeriode = result.data[position]
                }
            }
        }

        viewModel.loadPeriodeTersedia()

        btnBatal.setOnClickListener {
            dialog.dismiss()
            viewModel.selectedPeriode = null
        }

        btnProses.setOnClickListener {
            val periode = viewModel.selectedPeriode
            if (periode == null) {
                Snackbar.make(binding.root, "Pilih periode terlebih dahulu", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            viewModel.prosesPrediksi(periode.bulan, periode.tahun)
            viewModel.selectedPeriode = null
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}