package com.example.salesforecastingapps.ui.produksi

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.salesforecastingapps.R
import com.example.salesforecastingapps.data.model.Produksi
import com.example.salesforecastingapps.data.model.ProduksiRequest
import com.example.salesforecastingapps.databinding.DialogTambahProduksiBinding
import com.example.salesforecastingapps.databinding.FragmentDetailProduksiBinding
import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.Result
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
        binding.rvProduksi.apply {
            adapter = produksiAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupSwipeToDelete()
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
                        binding.rvProduksi.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility    = View.GONE
                        binding.rvProduksi.visibility = View.VISIBLE
                        produksiAdapter.submitList(result.data)

                        // DIUBAH: total dalam baglog (integer, tanpa koma)
                        val total = result.data.sumOf { it.jumlahProduksi.toInt() }
                        binding.tvTotalPenjualan.text = "$total baglog"

                        val baglogBaru = result.data.firstOrNull()?.baglogBaru
                        binding.tvRekomendasiBaglog.text = if (baglogBaru != null) "$baglogBaru baglog" else "0 baglog"
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility     = View.VISIBLE
                    binding.tvEmpty.text           = result.message
                    binding.tvRekomendasiBaglog.text = "0 baglog"
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

        viewModel.hapusResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Snackbar.make(binding.root, "Data berhasil dihapus", Snackbar.LENGTH_SHORT).show()
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

    private fun showDialogKonfirmasiHapus(produksi: Produksi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Data")
            .setMessage(
                "Hapus data produksi tanggal ${DateUtils.formatTanggal(produksi.tanggalProduksi)}?"
            )
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.hapusProduksi(produksi.produksiId)
            }
            .show()
    }

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
                val produksi = produksiAdapter.getItem(position)

                produksiAdapter.notifyItemChanged(position)
                showDialogKonfirmasiHapus(produksi)
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvProduksi)
    }

    private fun showDialogTambah() {
//        if (args.rekomendasiId == -1) {
//            Snackbar.make(
//                binding.root,
//                "Belum ada rekomendasi untuk periode ini",
//                Snackbar.LENGTH_LONG
//            ).show()
//            return
//        }

        val dialogBinding = DialogTambahProduksiBinding.inflate(layoutInflater)

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

            val bagianTanggal = tanggalDipilih.split("-")
            val tahunInput = bagianTanggal[0].toInt()
            val bulanInput = bagianTanggal[1].toInt()
            val bulanSesuai = (bulanInput == args.bulan && tahunInput == args.tahun)

            when {
                jumlahStr.isBlank() ->
                    dialogBinding.etJumlah.error = "Jumlah wajib diisi"

                // DIUBAH: validasi integer, bukan float
                jumlahStr.toIntOrNull() == null || jumlahStr.toInt() <= 0 ->
                    dialogBinding.etJumlah.error = "Jumlah harus lebih dari 0"

                !bulanSesuai ->
                    Snackbar.make(
                        binding.root,
                        "Tanggal harus berada di bulan ${DateUtils.namaBulan(args.bulan)} ${args.tahun}",
                        Snackbar.LENGTH_LONG
                    ).show()

                args.rekomendasiId == -1 ->
                    Snackbar.make(
                        binding.root,
                        "Belum ada rekomendasi untuk periode ini",
                        Snackbar.LENGTH_LONG
                    ).show()

                else -> {
                    viewModel.simpanProduksi(
                        ProduksiRequest(
                            tanggalProduksi = tanggalDipilih,
                            // DIUBAH: parse sebagai Int lalu konversi ke Float untuk model
                            jumlah          = jumlahStr.toInt().toFloat(),
                            rekomendasiId   = args.rekomendasiId
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