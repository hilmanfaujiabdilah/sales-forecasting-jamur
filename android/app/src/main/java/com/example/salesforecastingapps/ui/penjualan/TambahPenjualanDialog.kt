package com.example.salesforecastingapps.ui.penjualan

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.salesforecastingapps.data.model.Kumbung
import com.example.salesforecastingapps.data.model.PenjualanRequest
import com.example.salesforecastingapps.databinding.DialogTambahPenjualanBinding
import com.example.salesforecastingapps.utils.Result
import java.text.SimpleDateFormat
import java.util.*

class TambahPenjualanDialog : DialogFragment() {

    private var _binding: DialogTambahPenjualanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PenjualanViewModel by activityViewModels()

    private lateinit var kumbungAdapter: KumbungDropdownAdapter
    private var kumbungTerpilih: Kumbung? = null
    private var prediksiId: Int = -1

    companion object {
        const val TAG = "TambahPenjualanDialog"
        private const val ARG_PREDIKSI_ID = "prediksi_id"

        fun newInstance(prediksiId: Int): TambahPenjualanDialog {
            return TambahPenjualanDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PREDIKSI_ID, prediksiId)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        prediksiId = arguments?.getInt(ARG_PREDIKSI_ID) ?: -1

        _binding = DialogTambahPenjualanBinding.inflate(LayoutInflater.from(requireContext()))

        setupTanggal()
        setupDropdownKumbung()
        setupPanelTambahKumbung()
        setupTombol()
        observeViewModel()

        // Load kumbung saat dialog dibuka
        viewModel.loadKumbung()

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setupTanggal() {
        // Isi dengan tanggal hari ini sebagai default
        val today    = Calendar.getInstance()
        val sdf      = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfLabel = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
        binding.etTanggal.setText(sdfLabel.format(today.time))

        // Simpan nilai aktual (format API) secara terpisah
        var tanggalApi = sdf.format(today.time)

        binding.etTanggal.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val cal = Calendar.getInstance().apply { set(y, m, d) }
                    binding.etTanggal.setText(sdfLabel.format(cal.time))
                    tanggalApi = sdf.format(cal.time)

                    binding.etTanggal.tag = tanggalApi
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etTanggal.tag = tanggalApi
    }

    private fun setupDropdownKumbung() {
        kumbungAdapter = KumbungDropdownAdapter(requireContext())
        binding.actvKumbung.setAdapter(kumbungAdapter)

        binding.actvKumbung.setOnClickListener {
            binding.actvKumbung.showDropDown()
        }

        binding.actvKumbung.setOnItemClickListener { _, _, position, _ ->
            val item = kumbungAdapter.getItem(position)

            if (item.kumbungId == KumbungDropdownAdapter.ID_TAMBAH) {
                binding.actvKumbung.setText("", false)
                kumbungTerpilih = null
                tampilkanPanelTambahKumbung()
            } else {
                kumbungTerpilih = item
                binding.actvKumbung.setText(item.namaKumbung, false)
                sembunyikanPanelTambahKumbung()
            }
        }
    }

    private fun setupPanelTambahKumbung() {
        binding.panelTambahKumbung.isVisible = false

        binding.btnSimpanKumbung.setOnClickListener {
            val nama = binding.etNamaKumbung.text?.toString()?.trim() ?: ""
            if (nama.isEmpty()) {
                binding.tilNamaKumbung.error = "Nama kumbung tidak boleh kosong"
                return@setOnClickListener
            }
            binding.tilNamaKumbung.error = null
            setInputKumbungEnabled(false)
            viewModel.tambahKumbung(nama)
        }

        binding.btnBatalKumbung.setOnClickListener {
            sembunyikanPanelTambahKumbung()
            binding.etNamaKumbung.text?.clear()
        }

        binding.etNamaKumbung.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSimpanKumbung.performClick()
                true
            } else false
        }

        binding.etNamaKumbung.doAfterTextChanged {
            if (!it.isNullOrEmpty()) binding.tilNamaKumbung.error = null
        }
    }

    private fun tampilkanPanelTambahKumbung() {
        binding.panelTambahKumbung.isVisible = true
        binding.etNamaKumbung.requestFocus()
    }

    private fun sembunyikanPanelTambahKumbung() {
        binding.panelTambahKumbung.isVisible = false
        setInputKumbungEnabled(true)
    }

    private fun setInputKumbungEnabled(enabled: Boolean) {
        binding.etNamaKumbung.isEnabled    = enabled
        binding.btnSimpanKumbung.isEnabled = enabled
        binding.btnBatalKumbung.isEnabled  = enabled
        binding.progressKumbung.isVisible  = !enabled
    }

    private fun setupTombol() {
        binding.btnBatal.setOnClickListener { dismiss() }

        binding.btnSimpan.setOnClickListener {
            val tanggal = binding.etTanggal.tag as? String ?: return@setOnClickListener
            val jumlah  = binding.etJumlah.text?.toString()?.trim()?.toFloatOrNull()
            val kumbung = kumbungTerpilih

            when {
                jumlah == null || jumlah <= 0f -> {
                    binding.tilJumlah.error = "Masukkan jumlah penjualan yang valid"
                    return@setOnClickListener
                }
                kumbung == null -> {
                    binding.tilKumbung.error = "Pilih kumbung terlebih dahulu"
                    return@setOnClickListener
                }
                prediksiId == -1 -> {
                    Toast.makeText(requireContext(), "Data prediksi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            binding.tilJumlah.error  = null
            binding.tilKumbung.error = null

            val request = PenjualanRequest(
                tanggal    = tanggal,
                jumlah     = jumlah!!,
                kumbungId  = kumbungTerpilih!!.kumbungId,
                prediksiId = prediksiId
            )
            viewModel.simpanPenjualan(request)
        }
    }

    private fun observeViewModel() {
        viewModel.kumbungList.observe(this) { result ->
            if (result is Result.Success) {
                kumbungAdapter.submitList(result.data)
            }
        }

        viewModel.tambahKumbungResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {  }

                is Result.Success -> {
                    val kumbungBaru = result.data

                    kumbungAdapter.addKumbung(kumbungBaru)
                    kumbungTerpilih = kumbungBaru
                    binding.actvKumbung.setText(kumbungBaru.namaKumbung, false)
                    sembunyikanPanelTambahKumbung()
                    binding.etNamaKumbung.text?.clear()
                    Toast.makeText(
                        requireContext(),
                        "Kumbung \"${kumbungBaru.namaKumbung}\" berhasil ditambahkan",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Result.Error -> {
                    setInputKumbungEnabled(true)
                    binding.tilNamaKumbung.error = result.message
                }
            }
        }

        viewModel.simpanResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.btnSimpan.isEnabled = false
                    binding.btnBatal.isEnabled  = false
                }
                is Result.Success -> {
                    Toast.makeText(requireContext(), "Penjualan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is Result.Error -> {
                    binding.btnSimpan.isEnabled = true
                    binding.btnBatal.isEnabled  = true
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
