from flask import Blueprint, request
from datetime import datetime
from app.models.rekomendasi_model import RekomendasiModel
from app.models.produksi_model import ProduksiModel
from app.utils import response_error, response_sukses

produksi_bp = Blueprint("produksi", __name__, url_prefix="/api/produksi")

_FIELD_WAJIB = ["tanggal_produksi", "jumlah_produksi", "rekomendasi_id"]

def _validasi_data(d: dict) -> bool:
    return all(field in d and d[field] is not None for field in _FIELD_WAJIB)

@produksi_bp.route("", methods=['POST'])
def simpan_produksi():
    body = request.get_json(silent=True)

    if not body or not _validasi_data(body):
        return response_error(
            pesan="Data tidak lengkap",
            detail=f"Field wajib: {_FIELD_WAJIB}"
        )

    if body["jumlah_produksi"] <= 0:
        return response_error(pesan="Jumlah produksi tidak boleh negatif")

    rekomendasi = RekomendasiModel.get_by_id(body["rekomendasi_id"])
    if rekomendasi is None:
        return response_error(pesan="Rekomendasi tidak ditemukan", kode=404)

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

@produksi_bp.route("", methods=["GET"])
def get_all_produksi():
    try:
        data = ProduksiModel.get_agregasi_bulanan()
        return response_sukses(data, pesan="Data produksi berhasil diambil")
    except Exception as e:

        return response_error(pesan="Gagal mengambil data produksi", detail=str(e), kode=500)

@produksi_bp.route("/agregasi-bulanan", methods=["GET"])
def get_agregasi_bulanan():
    try:
        data = ProduksiModel.get_agregasi_bulanan()
        return response_sukses(data, pesan="Data produksi berhasil diambil")
    except Exception as e:
        return response_error(pesan="Gagal mengambil data agregasi produksi", detail=str(e), kode=500)

# Tambah endpoint baru di bawah endpoint yang sudah ada

@produksi_bp.route("/<int:produksi_id>", methods=["DELETE"])
def hapus_produksi(produksi_id: int):
    try:
        berhasil = ProduksiModel.hapus(produksi_id)
        if not berhasil:
            return response_error(
                pesan="Data produksi tidak ditemukan atau sudah dihapus",
                kode=404
            )
        return response_sukses(pesan="Data produksi berhasil dihapus")
    except Exception as e:
        return response_error(
            pesan="Gagal menghapus data produksi",
            detail=str(e),
            kode=500
        )