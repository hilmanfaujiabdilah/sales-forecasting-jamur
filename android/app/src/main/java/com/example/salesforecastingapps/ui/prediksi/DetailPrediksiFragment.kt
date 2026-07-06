package com.example.salesforecastingapps.ui.prediksi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingapps.databinding.FragmentDetailPrediksiBinding
import com.example.salesforecastingapps.utils.DateUtils
import com.example.salesforecastingapps.utils.FormatUtils
import com.example.salesforecastingapps.utils.Result
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.salesforecastingapps.R

class DetailPrediksiFragment : Fragment() {

    private var _binding: FragmentDetailPrediksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrediksiViewModel by viewModels()
    private val args: DetailPrediksiFragmentArgs by navArgs()
    private lateinit var bahanBakuAdapter: BahanBakuAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailPrediksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bahanBakuAdapter = BahanBakuAdapter()
        binding.tvBahanBaku.apply {
            adapter = bahanBakuAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        setupObservers()
        viewModel.loadRekomendasi(args.prediksiId)

        setupInfoError()

        // Load detail prediksi dari list
        viewModel.loadPrediksiList()
    }

    private fun setupObservers() {
        viewModel.prediksiList.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                val prediksi = result.data.find { it.prediksiId == args.prediksiId }
                prediksi?.let { p ->
                    binding.tvPredPeriode1.text  = FormatUtils.formatKg(p.predPeriode1)
                    binding.tvPredPeriode2.text  = FormatUtils.formatKg(p.predPeriode2)
                    binding.tvLabelPeriode1.text = DateUtils.formatPeriode(p.periodePrediksi)
                    binding.tvMae.text            = FormatUtils.formatAngka(p.nilaiMae)
                    binding.tvRmse.text           = FormatUtils.formatAngka(p.nilaiRmse)
                    binding.tvMape.text           = FormatUtils.formatPersen(p.nilaiMape)
                    binding.tvR2.text             = if (p.nilaiR2 != null) "%.4f".format(p.nilaiR2) else "-"
                    binding.tvKesimpulanError.text = buatKesimpulanEvaluasi(
                        p.nilaiMae?.toDouble(), p.nilaiMape?.toDouble(), p.nilaiRmse?.toDouble(), p.predPeriode1.toDouble(), p.predPeriode2.toDouble()
                    )
                }
            }
        }

        viewModel.rekomendasi.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                result.data?.let { rek ->
                    binding.tvKebutuhanBaglog.text = FormatUtils.formatBaglog(rek.estKebutuhanBaglog)
                    binding.tvBaglogAktif.text     = FormatUtils.formatBaglog(rek.estBaglogAktif)
                    binding.tvBaglogBaru.text      = FormatUtils.formatBaglog(rek.baglogBaru)
                    viewModel.loadBahanBaku(rek.rekomendasiId)
                }
            }
        }

        viewModel.bahanBaku.observe(viewLifecycleOwner) { result ->
            if (result is Result.Success) {
                bahanBakuAdapter.submitList(result.data)
            }
        }
    }

    private fun showPenjelasanError(highlightId: Int? = null) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_penjelasan_error, null)
        dialog.setContentView(view)

        // Sorot blok tertentu jika dibuka dari salah satu kotak metrik
        highlightId?.let { id ->
            view.findViewById<View?>(id)?.setBackgroundColor(
                requireContext().getColor(R.color.hijau_latar)
            )
        }

        dialog.show()
    }

    private fun setupInfoError() {
        // Ikon info di header -> tampilkan semua penjelasan
        binding.btnInfoError.setOnClickListener { showPenjelasanError() }

        // (Opsional) tiap kotak metrik membuka sheet & menyorot bagian terkait
        binding.layoutMae.setOnClickListener  { showPenjelasanError(R.id.blokMae) }
        binding.layoutRmse.setOnClickListener { showPenjelasanError(R.id.blokRmse) }
        binding.layoutMape.setOnClickListener { showPenjelasanError(R.id.blokMape) }
        binding.layoutR2.setOnClickListener   { showPenjelasanError(R.id.blokR2) }
    }

    /**
     * Membuat kalimat kesimpulan dengan bahasa sederhana (mudah dipahami petani),
     * berdasarkan nilai evaluasi model. Tingkat akurasi ditentukan dari
     * persentase kesalahan (MAPE), namun istilah teknis tidak ditampilkan:
     *  - < 10%  -> Sangat Baik
     *  - < 20%  -> Baik
     *  - < 50%  -> Cukup
     *  - >= 50% -> Kurang Baik
     */
    private fun buatKesimpulanEvaluasi(
        mae: Double?,
        mape: Double?,
        rmse: Double?,
        pred1: Double?,
        pred2: Double?
    ): CharSequence {
        if (mae == null || mape == null) {
            return "Perkiraan belum bisa disimpulkan karena datanya masih belum lengkap."
        }

        // Normalisasi MAPE ke bentuk persen (0-100), baik tersimpan sebagai 0.14 maupun 14.
        val mapePersen = if (mape <= 1.0) mape * 100 else mape

        val kategori: String
        val keandalan: String
        when {
            mapePersen < 10 -> { kategori = "Sangat Baik"; keandalan = "sangat bisa diandalkan" }
            mapePersen < 20 -> { kategori = "Baik";        keandalan = "bisa diandalkan" }
            mapePersen < 50 -> { kategori = "Cukup";       keandalan = "hanya jadi gambaran" }
            else            -> { kategori = "Kurang Baik"; keandalan = "Kurang bisa diandalkan" }
        }

        val maeStr = FormatUtils.formatAngka(Math.round(mae).toFloat())
        val persenStr = "%.0f".format(mapePersen)

        val sb = StringBuilder()
        sb.append("Prediksi tergolong <b>").append(kategori).append("</b>, ")
        sb.append("rata-rata kesalahan meleset sekitar <b>").append(maeStr).append(" KG</b> ")
        sb.append("(<b>").append(persenStr).append("%</b>), model tergolong ").append(keandalan).append(".")

        // Arah tren penjualan antar periode (ringkas)
        if (pred1 != null && pred2 != null) {
            val selisih = pred2 - pred1
            val selisihStr = FormatUtils.formatAngka(Math.round(Math.abs(selisih)).toFloat())
            val arah = when {
                selisih > 0 -> "<b>naik</b> sekitar $selisihStr KG"
                selisih < 0 -> "<b>turun</b> sekitar $selisihStr KG"
                else        -> "<b>relatif stabil</b>"
            }
            sb.append(" Tren penjualan diperkirakan ").append(arah).append(".")
        }

        // Catatan risiko hanya bila kesalahan berpotensi meleset jauh (RMSE >> MAE)
        if (rmse != null && rmse > mae * 1.3) {
            sb.append(" Sesekali bisa meleset lebih jauh, siapkan cadangan.")
        }

        return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
