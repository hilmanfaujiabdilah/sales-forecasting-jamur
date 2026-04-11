from flask import Blueprint, request
from app.models import RekomendasiModel
from app.models.produksi_model import ProduksiModel
from app.utils import response_error, response_sukses

produksi_bp = Blueprint("produksi", __name__, url_prefix="/api/produksi")

_FIELD_WAJIB = ["tanggal_produksi", "jumlah_penjualan", "rekomendasi_id"]

def _validasi_data(d: dict) -> bool:
    return all(field in d and d[field] is not None for field in _FIELD_WAJIB)

@produksi_bp.route("", methods=['GET'])
def simpan_produksi():
    body = request.get_json(silent=True)
    if not body or not _validasi_data(body):
        return response_error(
            pesan="Data tidak lengkap",
            detail=f"Field wajib: {_FIELD_WAJIB}"
        )

    if body["jumlah_produksi"] < 0:
        return response_error(pesan="Jumlah produksi tidak boleh negatif")

    rek = RekomendasiModel.get_by_prediksi_id(
        body.get("prediksi_id", -1)
    )
    # Catatan: validasi lengkap dilakukan oleh FK constraint PostgreSQL

    try:
        ProduksiModel.insert(body)
        return response_sukses(pesan="Data produksi berhasil disimpan", kode=201)
    except Exception as e:
        return response_error(pesan="Gagal menyimpan data produksi", detail=str(e), kode=500)

@produksi_bp.route("/periode", methods=['GET'])
def get_produksi_by_periode():
    bulan = request.args.get("bulan", type=int)
    tahun = request.args.get("tahun", type=int)

    if not bulan or not tahun:
        return response_error(pesan="Parameter 'bulan' dan 'tahun' wajib diisi")

    try:
        data = ProduksiModel.get_by_periode(bulan,tahun)
        return response_sukses(data, pesan="Data produksi berhasil diambil")
    except Exception as e:
        return response_error(pesan="Gagal mengambil data produksi", detail=str(e), kode=500)