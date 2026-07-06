package com.example.salesforecastingapps.ui.penjualan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingapps.R
import com.example.salesforecastingapps.data.model.Penjualan
import com.example.salesforecastingapps.data.model.PenjualanRequest
import com.example.salesforecastingapps.databinding.DialogTambahPenjualanBinding
import com.example.salesforecastingapps.databinding.FragmentDetailPenjualanBinding
import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.FormatUtils
import com.example.salesforecastingapps.utils.Result
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
        setupSwipeToDelete()
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

        viewModel.hapusResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Snackbar.make(binding.root, "Data berhasil dihapus", Snackbar.LENGTH_SHORT).show()
                    viewModel.loadPenjualanBulanan(args.bulan, args.tahun)
                }
                is Result.Error ->
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun setupFab() {
        binding.fabTambah.setOnClickListener {
            TambahPenjualanDialog
                .newInstance(prediksiId = args.prediksiId)
                .show(childFragmentManager, TambahPenjualanDialog.TAG)
        }
    }

//    private fun showDialogTambah() {
//        val dialogBinding = DialogTambahPenjualanBinding.inflate(layoutInflater)
//        var tanggalDipilih = DateUtils.today()
//        dialogBinding.etTanggal.setText(DateUtils.formatTanggal(tanggalDipilih))
//
//        // DatePicker
//        dialogBinding.etTanggal.setOnClickListener {
//            val cal = Calendar.getInstance()
//            DatePickerDialog(requireContext(), { _, y, m, d ->
//                tanggalDipilih = "%04d-%02d-%02d".format(y, m + 1, d)
//                dialogBinding.etTanggal.setText(DateUtils.formatTanggal(tanggalDipilih))
//            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
//        }
//
//        // Dropdown kumbung
//        val kumbungList = (viewModel.kumbungList.value as? Result.Success)?.data ?: emptyList()
//        val kumbungNames = kumbungList.map { it.namaKumbung }
//        val kumbungAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kumbungNames)
//        dialogBinding.actvKumbung.setAdapter(kumbungAdapter)
//
//        val dialog = MaterialAlertDialogBuilder(requireContext())
//            .setView(dialogBinding.root)
//            .create()
//
//        dialogBinding.btnBatal.setOnClickListener { dialog.dismiss() }
//
//        dialogBinding.btnSimpan.setOnClickListener {
//            val jumlahStr = dialogBinding.etJumlah.text.toString()
//            val kumbungNama = dialogBinding.actvKumbung.text.toString()
//            val kumbung = kumbungList.find { it.namaKumbung == kumbungNama }
//
//            val bagianTanggal = tanggalDipilih.split("-")
//            val tahunInput = bagianTanggal[0].toInt()
//            val bulanInput = bagianTanggal[1].toInt()
//            val bulanSesuai = (bulanInput == args.bulan && tahunInput == args.tahun)
//
//            when {
//                jumlahStr.isBlank() ->
//                    dialogBinding.etJumlah.error = "Jumlah wajib diisi"
//                jumlahStr.toFloatOrNull() == null || jumlahStr.toFloat() <= 0f ->
//                    dialogBinding.etJumlah.error = "Jumlah harus lebih dari 0"
//                kumbung == null ->
//                    Snackbar.make(binding.root, "Pilih kumbung terlebih dahulu", Snackbar.LENGTH_SHORT).show()
//                !bulanSesuai ->
//                    Snackbar.make(
//                        binding.root,
//                        "Tanggal harus berada di bulan ${DateUtils.namaBulan(args.bulan)} ${args.tahun}",
//                        Snackbar.LENGTH_LONG
//                    ).show()
//                args.prediksiId == -1 ->
//                    Snackbar.make(binding.root, "Belum ada prediksi untuk periode ini", Snackbar.LENGTH_LONG).show()
//                else -> {
//                    viewModel.simpanPenjualan(
//                        PenjualanRequest(
//                            tanggal    = tanggalDipilih,
//                            jumlah     = jumlahStr.toFloat(),
//                            kumbungId  = kumbung.kumbungId,
//                            prediksiId = args.prediksiId
//                        )
//                    )
//                    dialog.dismiss()
//                }
//            }
//        }
//        dialog.show()
//    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val penjualan = penjualanAdapter.getItem(position)
                penjualanAdapter.notifyItemChanged(position)   // kembalikan item sebelum dialog
                showDialogKonfirmasiHapus(penjualan)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvPenjualan)
    }

    private fun showDialogKonfirmasiHapus(penjualan: Penjualan) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Data")
            .setMessage(
                "Hapus data penjualan tanggal ${DateUtils.formatTanggal(penjualan.tanggal)}?"
            )
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.hapusPenjualan(penjualan.penjualanId)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
