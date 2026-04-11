import math
import calendar
from datetime import date
from dateutil.relativedelta import relativedelta
from flask import Blueprint, request
from app.models import BahanBakuModel, RekomendasiModel
from app.models.produksi_model import ProduksiModel
from app.models.prediksi_model import PrediksiModel
from app.models.penjualan_model import PenjualanModel
from app.controller.model_regresi import ModelRegresi
from app.utils.response import response_sukses, response_error

prediksi_bp = Blueprint("prediksi", __name__, url_prefix="/api/prediksi")

# -----------------------------------------------------------------------
# Konstanta bisnis — diturunkan dari data SOP usaha jamur tiram
#
# Data acuan (dari pemilik usaha):
#   2500 baglog → menghasilkan 25 kg jamur
#   Kebutuhan bahan baku untuk 2500 baglog:
#     Serbuk Kayu : 62 kg
#     Air         : 3000 liter
#     Bibit       : 170 botol
#     Dedak       : 250 kg
#     Kapur       : 62 kg
#     Kapas Majun : 3 kg
#     Kayu Bakar  : 1 mobil
#     Karet       : 1 kg
# -----------------------------------------------------------------------

# Acuan produksi
BAGLOG_ACUAN        = 2500   # jumlah baglog dalam satu siklus acuan
HASIL_ACUAN_KG      = 25     # hasil panen (kg) dari BAGLOG_ACUAN baglog

# Hasil panen per baglog (kg):
#   25 kg / 2500 baglog = 0.01 kg per baglog
HASIL_PANEN_PER_BAGLOG = HASIL_ACUAN_KG / BAGLOG_ACUAN   # → 0.01

# Rasio kebutuhan bahan baku per baglog
# Diturunkan dari: jumlah_bahan_baku / BAGLOG_ACUAN
RASIO_BAHAN_BAKU = {
    #  nama            : (rasio per baglog              , satuan  )
    "Serbuk Kayu"  : (62   / BAGLOG_ACUAN, "kg"    ),  # 0.0248
    "Air"          : (3000 / BAGLOG_ACUAN, "liter" ),  # 1.2
    "Bibit"        : (170  / BAGLOG_ACUAN, "botol" ),  # 0.068
    "Dedak"        : (250  / BAGLOG_ACUAN, "kg"    ),  # 0.1
    "Kapur"        : (62   / BAGLOG_ACUAN, "kg"    ),  # 0.0248
    "Kapas Majun"  : (3    / BAGLOG_ACUAN, "kg"    ),  # 0.0012
    "Kayu Bakar"   : (1    / BAGLOG_ACUAN, "mobil" ),  # 0.0004
    "Karet"        : (1    / BAGLOG_ACUAN, "kg"    ),  # 0.0004
}

# HELPER FUNCTION
def _hitung_estimasi_baglog(pred_penjualan2: float) -> int:
    """
    Hitung estimasi kebutuhan total baglog berdasarkan prediksi penjualan.
    """
    return math.ceil(pred_penjualan2 / HASIL_PANEN_PER_BAGLOG)

def _hitung_baglog_aktif(periode_pred2: date) -> int:
    """
    Rumus:
        batas_awal       = periode_pred2 - 3 bulan
        batas_akhir      = periode_pred2 - 1 bulan
        est_baglog_aktif = SUM(baglog_baru dari produksi)
                           WHERE tanggal_produksi BETWEEN batas_awal AND batas_akhir
    """

    batas_awal = periode_pred2 - relativedelta(months=3)
    batas_akhir = periode_pred2 - relativedelta(months=1)

    # ambil semua produksi yang ada pada rentang batas_awal - batas_akhir
    rekap = ProduksiModel.get_baglog_aktif(batas_awal, batas_akhir)
    if not rekap:
        return 0

    total_produksi = sum(r["jumlah_produksi"] for r in rekap)
    return math.ceil(total_produksi / HASIL_PANEN_PER_BAGLOG)

def _hitung_baglog_baru(kebutuhan: int, aktif: int) -> int:
    return max(0, kebutuhan - aktif)

def _hitung_bahan_baku(baglog_baru: int) -> list:
    return [
        {
            "nama_bahan_baku" : nama,
            "jumlah" : round(baglog_baru * rasio, 4),
            "satuan" : satuan,
        }
        for nama, (rasio, satuan) in RASIO_BAHAN_BAKU.items()
    ]

def _simpan_bahan_baku(rekomendasi_id: int, bahan_baku_list: list) -> bool:
    semua_jenis = BahanBakuModel.get_all_jenis()
    jenis_map = {j["nama_bahan_baku"]: j["jenis_bahan_baku_id"] for j in semua_jenis}

    insert_data = []
    for item in bahan_baku_list:
        jenis_id = jenis_map.get(item["nama_bahan_baku"])
        if jenis_id:
            insert_data.append(
                {
                    "rekomendasi_id": rekomendasi_id,
                    "jenis_bahan_baku_id": jenis_id,
                    "jumlah_bahan_baku": item["jumlah"],
                }
            )
    if insert_data:
        BahanBakuModel.insert_bahan_baku(insert_data)
    return True

def _validasi_data_historis(data: list) -> tuple[bool, str]:
    hari_ini = date.today()
    hari_terakhir = calendar.monthrange(hari_ini.year, hari_ini.month)[1]

    # Validasi tanggal terakhir
    if hari_ini.day != hari_terakhir:
        return (
            False,
            f"Prediksi hanya dapat dilakukan pada tanggal terakhir bulan berjalan"
            f"(tanggal {hari_terakhir} {hari_ini.strftime('%B %Y')})."
        )

    if not data:
        return False, "Belum ada data penjualan sama sekali."

    bulan_terakhir_db = data[-1]["periode"][:7] # format MM-YYYY
    bulan_berjalan = hari_ini.strftime("%m-%Y")

    if bulan_terakhir_db != bulan_berjalan:
        return (
            False,
            f"Data penjualan bulan berjalan ({bulan_berjalan}) belum tersedia"
            f"Data terakhir yang ada: {bulan_terakhir_db}."
        )

    return True, "Valid"

# ENDPOINTS

@prediksi_bp.route("/proses", methods=['POST'])
def proses_prediksi():
    # Pipeline Prediksi

    # Mengambil data historis
    data_historis = PenjualanModel.get_all_agregasi_bulanan()

    valid, pesan = _validasi_data_historis(data_historis)

    if not valid:
        return response_error(pesan=pesan, kode=422)

    n = len(data_historis)

    # Melatih Model
    model = ModelRegresi()
    try:
        model.train_model(data_historis)
    except Exception as e:
        return response_error(pesan=str(e), kode=422)

    # Prediksi dua periode ke depan
    pred1 = max(0.0, model.predict(n + 1))
    pred2 = max(0.0, model.predict(n + 2))

    # Menghitung error
    y_aktual = [d["total_penjualan"] for d in data_historis]
    y_pred_insample = [max(0.0, model.predict(i + 1)) for i in range(n)]
    error = model.hitung_error(y_aktual, y_pred_insample)

    # Tentukan periode prediksi
    last_periode_str = data_historis[-1]["periode"][:7] # format YYYY-MM
    last_date = date.fromisoformat(last_periode_str + "-01")
    periode_pred1 = last_date + relativedelta(months=1)
    periode_pred2 = last_date + relativedelta(months=2)

    # Menyimpan ke tabel prediksi
    prediksi_id = PrediksiModel.insert(
        {
            "periode_prediksi": periode_pred1,
            "pred_periode1": pred1,
            "pred_periode2": pred2,
            "slope": model.slope,
            "intercept": model.intercept,
            "nilai_mae": error["mae"],
            "nilai_rmse": error["rmse"],
            "nilai_mape": error["mape"],
        }
    )

    # Menghitung estimas baglog
    est_kebutuhan = _hitung_estimasi_baglog(pred2)
    est_aktif = _hitung_estimasi_baglog(periode_pred2)
    baglog_baru = _hitung_baglog_baru(est_kebutuhan, est_aktif)

    rekomendasi_id = RekomendasiModel.insert(
        {
            "est_kebutuhan_baglog": est_kebutuhan,
            "est_baglog_aktif": est_aktif,
            "baglog_baru": baglog_baru,
            "prediksi_id": prediksi_id,
        }
    )

    # Hitung dan simpan kebutuhan bahan baku
    bahan_baku_list = _hitung_bahan_baku(baglog_baru)
    _simpan_bahan_baku(rekomendasi_id, bahan_baku_list)

    # Susunan respons
    hasil = {
        "prediksi": {
            "prediksi_id": prediksi_id,
            "periode_pred1": str(periode_pred1),
            "periode_pred2": str(periode_pred2),
            "pred_periode1": round(pred1, 2),
            "pred_periode2": round(pred2, 2),
            "slope": round(model.slope, 4),
            "intercept": round(model.intercept, 4),
            "nilai_mae": round(error["mae"], 4),
            "nilai_rmse": round(error["rmse"], 4),
            "nilai_mape": round(error["mape"], 4),
        },
        "rekomendasi":{
            "rekomendasi_id": rekomendasi_id,
            "est_kebutuhan_baglog": est_kebutuhan,
            "est_baglog_aktif": est_aktif,
            "baglog_baru": baglog_baru,
        },
        "bahan_baku": bahan_baku_list,
    }
    return response_sukses(hasil, pesan="Prediksi berhasil diproses", kode=201)

@prediksi_bp.route("", methods=['GET'])
def get_all_prediksi():
    try:
        data = PrediksiModel.get_all()
        return response_sukses(data, pesan="Data prediksi berhasil diambil")
    except Exception as e:
        return response_error(pesan="Gagal mengambil dat  prediksi", detail=str(e), kode=500)

@prediksi_bp.route("/periode", methods=['GET'])
def get_prediksi_by_periode():
    bulan = request.args.get("bulan", type=int)
    tahun = request.args.get("tahun", type=int)

    if not bulan or not tahun:
        return response_error(pesan="Parameter 'bulan' dan 'tahun' wajib diisi")

    try:
        data = PrediksiModel.get_by_periode(bulan, tahun)
        return response_sukses(data)
    except Exception as e:
        return response_error(pesan="Gagal mengambil data prediksi", detail=str(e), kode=500)

@prediksi_bp.route("/rekomendasi/<int:prediksi_id>", methods=['GET'])
def get_rekomendasi_by_prediksi(prediksi_id):
    try:
        rek = RekomendasiModel.get_by_prediksi_id(prediksi_id)
        if rek is None:
            return response_error(pesan="Rekomendasi tidak ditemukan", kode=404)
        return response_sukses(rek)
    except Exception as e:
        return response_error(pesan="Gagal mengambil rekomendasi", detail=str(e), kode=500)

@prediksi_bp.route("/bahan-baku/<int:rekomendasi_id>", methods=['GET'])
def get_bahan_baku_by_rekomendasi_id(rekomendasi_id):
    try:
        data = BahanBakuModel.get_by_rekomendasi_id(rekomendasi_id)
        return response_sukses(data)
    except Exception as e:
        return response_error(pesan="Gagal mengambil data bahan baku", detail=str(e), kode=500)