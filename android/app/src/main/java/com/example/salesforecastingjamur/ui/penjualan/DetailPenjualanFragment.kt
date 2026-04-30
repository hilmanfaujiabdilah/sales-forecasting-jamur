package com.example.salesforecastingjamur.ui.penjualan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingjamur.R
import com.example.salesforecastingjamur.data.model.PenjualanRequest
import com.example.salesforecastingjamur.databinding.DialogTambahPenjualanBinding
import com.example.salesforecastingjamur.databinding.FragmentDetailPenjualanBinding
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.FormatUtils
import com.example.salesforecastingjamur.utils.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class DetailPenjualanFragment : Fragment() {

    private var _binding: FragmentDetailPenjualanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PenjualanViewModel by viewModels()
    private val args: DetailPenjualanFragmentArgs by navArgs()
    private lateinit var penjualanAdapter: PenjualanAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailPenjualanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tampilkan nilai prediksi jika ada
        if (args.predPeriode1 > 0) {
            binding.tvPrediksiPeriode.text = FormatUtils.formatKg(args.predPeriode1)
        }

        setupRecyclerView()
        setupObservers()
        setupFab()
        viewModel.loadPenjualanBulanan(args.bulan, args.tahun)
        viewModel.loadKumbung()
    }

    private fun setupRecyclerView() {
        penjualanAdapter = PenjualanAdapter()
        binding.rvPenjualan.apply {
            adapter = penjualanAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.penjualanBulanan.observe(viewLifecycleOwner) { result ->
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
                        penjualanAdapter.submitList(result.data)
                        val total = result.data.sumOf { it.jumlah.toDouble() }.toFloat()
                        binding.tvTotalPenjualan.text = FormatUtils.formatKg(total)
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility     = View.VISIBLE
                    binding.tvEmpty.text = result.message
                }
            }
        }

        viewModel.simpanResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Snackbar.make(binding.root, getString(R.string.berhasil_simpan), Snackbar.LENGTH_SHORT).show()
                    viewModel.loadPenjualanBulanan(args.bulan, args.tahun)
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
        val dialogBinding = DialogTambahPenjualanBinding.inflate(layoutInflater)
        var tanggalDipilih = DateUtils.today()
        dialogBinding.etTanggal.setText(DateUtils.formatTanggal(tanggalDipilih))

        // DatePicker
        dialogBinding.etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                tanggalDipilih = "%04d-%02d-%02d".format(y, m + 1, d)
                dialogBinding.etTanggal.setText(DateUtils.formatTanggal(tanggalDipilih))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Dropdown kumbung
        val kumbungList = (viewModel.kumbungList.value as? Result.Success)?.data ?: emptyList()
        val kumbungNames = kumbungList.map { it.namaKumbung }
        val kumbungAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kumbungNames)
        dialogBinding.actvKumbung.setAdapter(kumbungAdapter)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnBatal.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSimpan.setOnClickListener {
            val jumlahStr = dialogBinding.etJumlah.text.toString()
            val kumbungNama = dialogBinding.actvKumbung.text.toString()
            val kumbung = kumbungList.find { it.namaKumbung == kumbungNama }

            when {
                jumlahStr.isBlank() ->
                    dialogBinding.etJumlah.error = "Jumlah wajib diisi"
                kumbung == null ->
                    Snackbar.make(binding.root, "Pilih kumbung terlebih dahulu", Snackbar.LENGTH_SHORT).show()
                args.prediksiId == -1 ->
                    Snackbar.make(binding.root, "Belum ada prediksi untuk periode ini", Snackbar.LENGTH_LONG).show()
                else -> {
                    viewModel.simpanPenjualan(
                        PenjualanRequest(
                            tanggal    = tanggalDipilih,
                            jumlah     = jumlahStr.toFloat(),
                            kumbungId  = kumbung.kumbungId,
                            prediksiId = args.prediksiId
                        )
                    )
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
