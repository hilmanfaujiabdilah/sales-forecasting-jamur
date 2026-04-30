package com.example.salesforecastingjamur.ui.prediksi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingjamur.databinding.FragmentDetailPrediksiBinding
import com.example.salesforecastingjamur.utils.DateUtils
import com.example.salesforecastingjamur.utils.FormatUtils
import com.example.salesforecastingjamur.utils.Result

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
        binding.rvBahanBaku.apply {
            adapter = bahanBakuAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        setupObservers()
        viewModel.loadRekomendasi(args.prediksiId)

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
                    binding.tvLabelPeriode1.text = DateUtils.formatPeriode(p.periodePred1)
                    binding.tvLabelPeriode2.text = DateUtils.formatPeriode(p.periodePred2)
                    binding.tvMae.text            = FormatUtils.formatAngka(p.nilaiMae)
                    binding.tvRmse.text           = FormatUtils.formatAngka(p.nilaiRmse)
                    binding.tvMape.text           = FormatUtils.formatPersen(p.nilaiMape)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
