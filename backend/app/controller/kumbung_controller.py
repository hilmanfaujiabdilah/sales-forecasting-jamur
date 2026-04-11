from flask import Blueprint, request
from app.models.kumbung_model import KumbungModel
from app.utils.response import response_sukses, response_error

kumbung_bp = Blueprint('kumbung', __name__, url_prefix="/api/kumbung")

@kumbung_bp.route('/', methods=['GET'])
def get_all_kumbung():
    try:
        data = KumbungModel.get_all()
        return response_sukses(data, pesan="Data kumbung berhasil diambil")
    except Exception as e:
        return response_error(pesan="Gagal mengambil data kumbung", detail=str(e), kode=500)

@kumbung_bp.route("/<int:kumbung_id>", methods=['GET'])
def get_kumbung_by_id(kumbung_id):
    try:
        data = KumbungModel.get_by_id(kumbung_id)
        if data is None:
            return response_error(pesan="Kumbung tidak ditemukan", kode=404)
        return response_sukses(data)
    except Exception as e:
        return response_error(pesan="Gagal mengambil data kumbung", detail=str(e), kode=500)

@kumbung_bp.route("", methods=['POST'])
def tambah_kumbung():
    body = request.get_json(silent=True)
    if not body or not body.get("nama_kumbung"):
        return response_error(pesan="Field 'nama_kumbung' wajib diisi")

    try:
        new_id = KumbungModel.insert(body["nama_kumbung"])
        return response_sukses(
            {"kumbung_id": new_id, "nama_kumbung": body["nama_kumbung"]},
            pesan="Kumbung berhasil diisi",
            kode=201,
        )
    except Exception as e:
        return response_error(pesan="Gagal menambahkan kumbung", detail=str(e), kode=500)

@kumbung_bp.route("/<int:kumbung_id>", methods=['DELETE'])
def hapus_kumbung(kumbung_id):
    try:
        deleted = KumbungModel.delete(kumbung_id)
        if not deleted:
            return response_error(pesan="Kumbung tidak ditemukan", kode=404)
        return response_sukses(pesan="Kumbung berhasil dihapus")
    except Exception as e:
        return response_error(pesan="Gagal menghapus kumbung", detail=str(e), kode=500)