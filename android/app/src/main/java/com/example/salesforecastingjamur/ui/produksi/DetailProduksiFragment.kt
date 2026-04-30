package com.example.salesforecastingjamur.ui.produksi

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingjamur.R
import com.example.salesforecastingjamur.data.model.ProduksiRequest
import com.example.salesforecastingjamur.databinding.DialogTambahPenjualanBinding
import com.example.salesforecastingjamur.databinding.FragmentDetailProduksiBinding
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.FormatUtils
import com.example.salesforecastingjamur.utils.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class DetailProduksiFragment : Fragment() {

    private var _binding: FragmentDetailProduksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProduksiViewModel by viewModels()
    private val args: DetailProduksiFragmentArgs by navArgs()
    private lateinit var produksiAdapter: ProduksiAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProduksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupFab()
        viewModel.loadProduksiBulanan(args.bulan, args.tahun)
    }

    private fun setupRecyclerView() {
        produksiAdapter = ProduksiAdapter()
        binding.rvPenjualan.apply {
            adapter = produksiAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.produksiBulanan.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEmpty.visibility     = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvEmpty.visibility    = View.VISIBLE
                        binding.rvPenjualan.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility    = View.GONE
                        binding.rvPenjualan.visibility = View.VISIBLE
                        produksiAdapter.submitList(result.data)
                        val total = result.data.sumOf { it.jumlahProduksi.toDouble() }.toFloat()
                        binding.tvTotalPenjualan.text = FormatUtils.formatKg(total)
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility     = View.VISIBLE
                    binding.tvEmpty.text           = result.message
                }
            }
        }

        viewModel.simpanResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Snackbar.make(binding.root, getString(R.string.berhasil_simpan), Snackbar.LENGTH_SHORT).show()
                    viewModel.loadProduksiBulanan(args.bulan, args.tahun)
                }
                is Result.Error ->
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun setupFab() {
        binding.fabTambah.setOnClickListener { showDialogTambah() }
    }

    private fun showDialogTambah() {
        if (args.rekomendasiId == -1) {
            Snackbar.make(
                binding.root,
                "Belum ada rekomendasi untuk periode ini",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        val dialogBinding = DialogTambahPenjualanBinding.inflate(layoutInflater)

        // Sembunyikan dropdown kumbung — tidak dipakai di produksi
        dialogBinding.tilKumbung.visibility = View.GONE

        var tanggalDipilih = DateUtils.today()
        dialogBinding.etTanggal.setText(DateUtils.formatTanggal(tanggalDipilih))

        dialogBinding.etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    tanggalDipilih = "%04d-%02d-%02d".format(y, m + 1, d)
                    dialogBinding.etTanggal.setText(DateUtils.formatTanggal(tanggalDipilih))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnBatal.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSimpan.setOnClickListener {
            val jumlahStr = dialogBinding.etJumlah.text.toString()
            if (jumlahStr.isBlank()) {
                dialogBinding.etJumlah.error = "Jumlah wajib diisi"
                return@setOnClickListener
            }
            viewModel.simpanProduksi(
                ProduksiRequest(
                    tanggalProduksi = tanggalDipilih,
                    jumlah        = jumlahStr.toFloat(),
                    rekomendasiId = args.rekomendasiId
                )
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
