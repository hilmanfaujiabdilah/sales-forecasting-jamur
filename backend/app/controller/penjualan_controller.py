from flask import Blueprint, request
from app.models.prediksi_model import PrediksiModel
from app.models.penjualan_model import PenjualanModel
from app.models.kumbung_model import KumbungModel
from app.utils.response import response_error, response_sukses

penjualan_bp = Blueprint("penjualan", __name__, url_prefix="/api/penjualan")

_FIELD_WAJIB = ["tanggal_penjualan", "jumlah_penjualan", "kumbung_id", "prediksi_id"]

def validasi_data(d: dict) -> bool:
    return all(field in d and d[field] is not None for field in _FIELD_WAJIB)


@penjualan_bp.route("", methods=['GET'])
def get_all_penjualan():
    try:
        data = PenjualanModel.get_all_penjualan()
        return response_sukses(data, pesan="Data penjualan berhasil diambil")
    except Exception as e:
        return response_error(pesan="Gagal mengambil data penjualan", detail=str(e), kode=500)

@penjualan_bp.route("/periode", methods=['GET'])
def get_all_periode():
    try:
        data = PenjualanModel.get_all_periode()
        return response_sukses(data, pesan="Data periode berhasil diambil")
    except Exception as e:
        return response_error(pesan="Gagal mengambil data periode", detail=str(e), kode=500)

@penjualan_bp.route("", methods=['POST'])
def simpan_penjualan():
    """
    Body json wajib: tanggal_penjualan, jumlah_penjualan, kumbung_id, prediksi_id
    """

    body = request.get_json(silent=True)
    if not body or not validasi_data(body):
        return response_error(
            pesan="Data tidak lengkap",
            detail=f"Field wajib: {_FIELD_WAJIB}"
        )

    kumbung = KumbungModel.get_by_id(body["kumbung_id"])
    if kumbung is None:
        return response_error(pesan=f"Kumbung dengan ID {body['kumbung_id']} tidak ditemukan")

    semua_prediksi = PrediksiModel.get_all()
    prediksi = next((p for p in semua_prediksi if p["prediksi_id"] == body["prediksi_id"]), None)
    if prediksi is None:
        return response_error(
            pesan=f"Prediksi dengan ID {body['prediksi_id']} tidak ditemukan",
            kode=404
        )

    try:
        if body["jumlah_penjualan"] <= 0:
            return response_error(pesan="Jumlah penjualan tidak boleh nol atau negatif")

        PenjualanModel.insert(body)
        return response_sukses(pesan="Data penjualan berhasil disimpan", kode=201)
    except Exception as e:
        return response_error(pesan="Gagal menyimpan data penjualan", detail=str(e), kode=500)

# Tambahkan di bagian bawah file, setelah endpoint simpan_penjualan

@penjualan_bp.route("/<int:penjualan_id>", methods=["DELETE"])
def hapus_penjualan(penjualan_id: int):
    try:
        berhasil = PenjualanModel.hapus(penjualan_id)
        if not berhasil:
            return response_error(
                pesan="Data penjualan tidak ditemukan atau sudah dihapus",
                kode=404
            )
        return response_sukses(pesan="Data penjualan berhasil dihapus")
    except Exception as e:
        return response_error(
            pesan="Gagal menghapus data penjualan",
            detail=str(e),
            kode=500
        )