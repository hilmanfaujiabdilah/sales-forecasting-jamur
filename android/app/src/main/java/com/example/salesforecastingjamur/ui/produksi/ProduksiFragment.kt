package com.example.salesforecastingjamur.ui.produksi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salesforecastingjamur.R
import com.example.salesforecastingjamur.data.repository.PrediksiRepository
import com.example.salesforecastingjamur.databinding.FragmentProduksiBinding
import com.example.salesforecastingjamur.ui.penjualan.PeriodeAdapter
import com.example.salesforecastingjamur.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProduksiFragment : Fragment() {

    private var _binding: FragmentProduksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProduksiViewModel by viewModels()
    private val prediksiRepo = PrediksiRepository()
    private lateinit var periodeAdapter: PeriodeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProduksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        periodeAdapter = PeriodeAdapter { periode ->
            // Ambil rekomendasiId dari prediksi terbaru
            CoroutineScope(Dispatchers.Main).launch {
                val prediksiResult = prediksiRepo.getAllPrediksi()
                val rekId = if (prediksiResult is Result.Success) {
                    val prediksi = prediksiResult.data.firstOrNull()
                    if (prediksi != null) {
                        val rekResult = prediksiRepo.getRekomendasiByPrediksi(prediksi.prediksiId)
                        (rekResult as? Result.Success)?.data?.rekomendasiId ?: -1
                    } else -1
                } else -1

                val action = ProduksiFragmentDirections
                    .actionProduksiToDetail(periode.bulan, periode.tahun, rekId)
                findNavController().navigate(action)
            }
        }

        binding.rvPeriode.apply {
            adapter = periodeAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Gunakan periode dari prediksi yang sudah ada
        CoroutineScope(Dispatchers.Main).launch {
            val result = prediksiRepo.getAllPrediksi()
            if (result is Result.Success) {
                val periodeList = result.data.map {
                    val parts = it.periodePred1.split("-")
                    com.example.salesforecastingjamur.data.model.Periode(
                        bulan = parts[1].toInt(),
                        tahun = parts[0].toInt()
                    )
                }.distinctBy { "${it.tahun}-${it.bulan}" }
                periodeAdapter.submitList(periodeList)

                if (periodeList.isEmpty()) binding.tvEmpty.visibility = View.VISIBLE
            }
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.hijau_utama)
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
