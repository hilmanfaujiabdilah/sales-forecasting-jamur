import math
from datetime import date
from sqlalchemy import select
from dateutil.relativedelta import relativedelta
from flask import Blueprint, request
from app.models import BahanBakuModel, RekomendasiModel, ProduksiModel
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
PANEN_PER_BULAN     = 2
SIKLUS_HIDUP_BULAN  = 4

HASIL_PANEN_PER_BAGLOG_PER_PANEN = HASIL_ACUAN_KG / BAGLOG_ACUAN  # → 0.01
HASIL_PANEN_PER_BAGLOG_PER_BULAN = HASIL_PANEN_PER_BAGLOG_PER_PANEN * PANEN_PER_BULAN  # → 0.02
USIA_AKTIF_MAKS_BULAN = SIKLUS_HIDUP_BULAN - 1  # → 3 bulan


# Hasil panen per baglog (kg):
#   25 kg / 2500 baglog = 0.01 kg per baglog
# HASIL_PANEN_PER_BAGLOG = HASIL_ACUAN_KG / BAGLOG_ACUAN   # → 0.01

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
    "Plastik": (1 / BAGLOG_ACUAN, "pack"),  # 0.0004
}

# HELPER FUNCTION
def _hitung_estimasi_baglog(pred_penjualan2: float) -> int:
    """
    Hitung estimasi kebutuhan total baglog berdasarkan prediksi penjualan.
    """
    if HASIL_PANEN_PER_BAGLOG_PER_BULAN <= 0:
        raise ValueError("HASIL_PANEN_PER_BAGLOG_PER_BULAN harus > 0")
    return math.ceil(pred_penjualan2 / HASIL_PANEN_PER_BAGLOG_PER_BULAN)

# def _hitung_baglog_aktif(periode_pred1: date) -> int:
#     """
#     Rumus:
#         batas_awal       = periode_pred2 - 3 bulan
#         batas_akhir      = periode_pred2 - 1 bulan
#         est_baglog_aktif = SUM(baglog_baru dari produksi)
#                            WHERE tanggal_produksi BETWEEN batas_awal AND batas_akhir
#     """
#
#     # batas_awal = periode_pred2 - relativedelta(months=USIA_AKTIF_MAKS_BULAN)
#     # batas_akhir = periode_pred2 - relativedelta(months=1)
#
#     batas_awal = date(
#         (periode_pred1 - relativedelta(months=USIA_AKTIF_MAKS_BULAN)).year,
#         (periode_pred1 - relativedelta(months=USIA_AKTIF_MAKS_BULAN)).month,
#         1
#     )
#
#     batas_akhir = date(
#         (periode_pred1 - relativedelta(months=1)).year,
#         (periode_pred1 - relativedelta(months=1)).month,
#         1
#     )
#     # ambil semua produksi yang ada pada rentang batas_awal - batas_akhir
#     # rekap = ProduksiModel.get_baglog_aktif(batas_awal, batas_akhir)
#     # if not rekap:
#     #     return 0
#     #
#     # total_produksi = sum(r["jumlah_produksi"] for r in rekap)
#     # return math.ceil(total_produksi)
#
#     with get_session() as session:
#         hasil = session.execute(
#             select(Rekomendasi.baglog_baru)
#             .join(Prediksi, Rekomendasi.prediksi_id == Prediksi.prediksi_id)
#             .where(
#                 Prediksi.periode_prediksi >= batas_awal,
#                 Prediksi.periode_prediksi <= batas_akhir,
#             )
#         ).scalars().all()
#
#     if not hasil:
#         return 0
#
#     return sum(hasil)

# def _hitung_baglog_aktif(periode_pred1: date) -> int:
#     """
#     Baglog aktif = baglog yang diproduksi 2 s/d 4 bulan sebelum periode_pred1,
#     karena:
#       - bulan ke-1 = inkubasi (belum aktif)
#       - bulan ke-2 s/d ke-4 = aktif berproduksi
#       - bulan ke-5 dst = sudah habis masa hidup
#
#     batas_awal  = periode_pred1 - 4 bulan  (misal: Feb - 4 = Oktober)
#     batas_akhir = periode_pred1 - 2 bulan  (misal: Feb - 2 = Desember)
#     """
#     batas_awal = date(
#         (periode_pred1 - relativedelta(months=SIKLUS_HIDUP_BULAN)).year,
#         (periode_pred1 - relativedelta(months=SIKLUS_HIDUP_BULAN)).month,
#         1
#     )
#
#     batas_akhir = date(
#         (periode_pred1 - relativedelta(months=2)).year,
#         (periode_pred1 - relativedelta(months=2)).month,
#         1
#     )
#
#     with get_session() as session:
#         hasil = session.execute(
#             select(Rekomendasi.baglog_baru)
#             .join(Prediksi, Rekomendasi.prediksi_id == Prediksi.prediksi_id)
#             .where(
#                 Prediksi.periode_prediksi >= batas_awal,
#                 Prediksi.periode_prediksi <= batas_akhir,
#             )
#         ).scalars().all()
#
#     if not hasil:
#         return 0
#
#     return sum(hasil)

def _hitung_baglog_aktif(periode_pred1: date) -> int:
    """
    Baglog aktif = baglog yang diproduksi dalam 3 bulan sebelum periode_pred1.

    Contoh: periode_pred1 = Januari 2026
      batas_awal  = Oktober 2025  (Januari - 3 bulan)
      batas_akhir = Desember 2025 (Januari - 1 bulan)

    Baglog dari Okt, Nov, Des masih aktif di Januari (masa aktif 4 bulan),
    sedangkan baglog baru akan diproduksi di bulan Januari itu sendiri.
    """
    batas_awal = date(
        (periode_pred1 - relativedelta(months=SIKLUS_HIDUP_BULAN - 1)).year,
        (periode_pred1 - relativedelta(months=SIKLUS_HIDUP_BULAN - 1)).month,
        1
    )
    batas_akhir = date(
        (periode_pred1 - relativedelta(months=1)).year,
        (periode_pred1 - relativedelta(months=1)).month,
        1
    )

    return ProduksiModel.get_baglog_aktif(batas_awal, batas_akhir)

def _hitung_baglog_baru(kebutuhan: int, aktif: int) -> int:
    return max(0, kebutuhan - aktif)

def _hitung_bahan_baku(baglog_baru: int) -> list:
    return [
        {
            "nama_bahan_baku" : nama,
            "jumlah" : round(baglog_baru * rasio, 2),
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

# ── FUNGSI VALIDASI TERPISAH ──────────────────────────────────────────

def _validasi_input(bulan_input, tahun_input) -> tuple[bool, str, object]:
    """
    Validasi parameter input dari request body.
    Return: (valid: bool, pesan: str, periode_pred1: date | None)
    """
    if not bulan_input or not tahun_input:
        return False, "Parameter 'bulan' dan 'tahun' wajib diisi", None

    try:
        periode_pred1 = date(int(tahun_input), int(bulan_input), 1)
    except (ValueError, TypeError):
        return False, "Nilai bulan atau tahun tidak valid", None

    return True, "Valid", periode_pred1


def _validasi_data_historis(data_historis: list, periode_pred1: date) -> tuple[bool, str]:
    """
    Validasi kelayakan data historis sebelum prediksi dijalankan.
    Cek:
      1. Duplikasi — periode ini sudah pernah diprediksi?
      2. Data tersedia minimal 2 bulan (syarat regresi linier)
      3. Data mencakup bulan tepat sebelum periode yang dipilih
    Return: (valid: bool, pesan: str)
    """
    # 1. Cek duplikasi periode
    existing = PrediksiModel.get_by_bulan_tahun(
        bulan=periode_pred1.month,
        tahun=periode_pred1.year
    )
    if existing:
        return (
            False,
            f"Prediksi periode {periode_pred1.strftime('%B %Y')} "
            f"sudah pernah dilakukan. Prediksi hanya bisa dilakukan satu kali per periode."
        )

    # 2. Cek ketersediaan data
    if not data_historis:
        return False, "Data penjualan tidak tersedia. Pastikan data sudah diinput."

    if len(data_historis) < 2:
        return False, "Data penjualan minimal 2 bulan untuk menjalankan regresi linier."

    # 3. Cek apakah bulan tepat sebelum periode yang dipilih sudah ada
    bulan_sebelumnya = (periode_pred1 - relativedelta(months=1)).strftime("%Y-%m")
    ada_bulan_sebelumnya = any(
        d["periode"].startswith(bulan_sebelumnya)
        for d in data_historis
    )
    if not ada_bulan_sebelumnya:
        return (
            False,
            f"Data penjualan bulan {bulan_sebelumnya} belum tersedia. "
            f"Pastikan data bulan sebelum periode prediksi sudah diinput."
        )

    return True, "Valid"

# ENDPOINTS


@prediksi_bp.route("/proses", methods=['POST'])
def proses_prediksi():
    body = request.get_json(silent=True) or {}

    # 1. Validasi input
    ok, pesan, periode_pred1 = _validasi_input(
        body.get("bulan"),
        body.get("tahun")
    )
    if not ok:
        return response_error(pesan=pesan, kode=400)

    periode_pred2 = periode_pred1 + relativedelta(months=1)

    # 2. Ambil data historis (maks 24 bulan terakhir)
    data_historis = PenjualanModel.get_all_agregasi_bulanan()
    data_historis = data_historis[-24:] if len(data_historis) > 24 else data_historis

    # 3. Validasi data historis (duplikasi + kecukupan data)
    ok, pesan = _validasi_data_historis(data_historis, periode_pred1)
    if not ok:
        return response_error(pesan=pesan, kode=422)

    n = len(data_historis)

    # 4. Latih model
    model = ModelRegresi()
    try:
        model.train_model(data_historis)
    except Exception as e:
        return response_error(pesan=str(e), kode=422)

    # 5. Prediksi dua periode ke depan
    pred1 = max(0.0, model.predict(n + 1))
    pred2 = max(0.0, model.predict(n + 2))

    # 6. Hitung error
    y_aktual        = [d["total_penjualan"] for d in data_historis]
    y_pred_insample = [model.predict(i + 1) for i in range(n)]
    error = model.hitung_error(y_aktual, y_pred_insample)

    # 7. Simpan — periode_pred1 dari input user, bukan dari last_date
    prediksi_id = PrediksiModel.insert({
        "periode_prediksi": periode_pred1,   # ← pakai input user
        "pred_periode1":    pred1,
        "pred_periode2":    pred2,
        "slope":            model.slope,
        "intercept":        model.intercept,
        "nilai_mae":        error["mae"],
        "nilai_rmse":       error["rmse"],
        "nilai_mape":       error["mape"],
        "nilai_r2":         error["r2"],
    })

    est_kebutuhan  = _hitung_estimasi_baglog(pred2)
    est_aktif      = _hitung_baglog_aktif(periode_pred1)
    baglog_baru    = _hitung_baglog_baru(est_kebutuhan, est_aktif)

    rekomendasi_id = RekomendasiModel.insert({
        "est_kebutuhan_baglog": est_kebutuhan,
        "est_baglog_aktif":     est_aktif,
        "baglog_baru":          baglog_baru,
        "prediksi_id":          prediksi_id,
    })

    bahan_baku_list = _hitung_bahan_baku(baglog_baru)
    _simpan_bahan_baku(rekomendasi_id, bahan_baku_list)

    hasil = {
        "prediksi": {
            "prediksi_id":    prediksi_id,
            "periode_pred1":  str(periode_pred1),
            "periode_pred2":  str(periode_pred2),
            "pred_periode1":  round(pred1, 2),
            "pred_periode2":  round(pred2, 2),
            "slope":          round(model.slope, 4),
            "intercept":      round(model.intercept, 4),
            "nilai_mae":      round(error["mae"], 4),
            "nilai_rmse":     round(error["rmse"], 4),
            "nilai_mape":     round(error["mape"], 4),
            "nilai_r2":       round(error["r2"], 4),
        },
        "rekomendasi": {
            "rekomendasi_id":       rekomendasi_id,
            "est_kebutuhan_baglog": est_kebutuhan,
            "est_baglog_aktif":     est_aktif,
            "baglog_baru":          baglog_baru,
        },
        "bahan_baku": bahan_baku_list,
    }
    return response_sukses(hasil, pesan="Prediksi berhasil diproses", kode=201)

# @prediksi_bp.route("/proses", methods=['POST'])
# def proses_prediksi():
#     # Ambil periode dari request body
#     body = request.get_json(silent=True) or {}
#     bulan_input = body.get("bulan")   # int 1-12
#     tahun_input = body.get("tahun")   # int misal 2025
#
#     if not bulan_input or not tahun_input:
#         return response_error(pesan="Parameter 'bulan' dan 'tahun' wajib diisi", kode=400)
#
#     try:
#         periode_pred1 = date(tahun_input, bulan_input, 1)
#     except ValueError:
#         return response_error(pesan="Nilai bulan atau tahun tidak valid", kode=400)
#
#     periode_pred2 = periode_pred1 + relativedelta(months=1)
#
#     # Cek duplikasi — periode yang dipilih sudah pernah diprediksi?
#     prediksi_existing = PrediksiModel.get_by_bulan_tahun(
#         bulan=periode_pred1.month,
#         tahun=periode_pred1.year
#     )
#     if prediksi_existing:
#         return response_error(
#             pesan=f"Prediksi periode {periode_pred1.strftime('%B %Y')} sudah pernah dilakukan.",
#             kode=422
#         )
#
#     # Ambil & batasi data historis
#     data_historis = PenjualanModel.get_all_agregasi_bulanan()
#     data_historis = data_historis[-24:] if len(data_historis) > 24 else data_historis
#
#     if not data_historis:
#         return response_error(pesan="Data penjualan tidak tersedia.", kode=422)
#
#     n = len(data_historis)
#
#     # Melatih Model
#     model = ModelRegresi()
#     try:
#         model.train_model(data_historis)
#     except Exception as e:
#         return response_error(pesan=str(e), kode=422)
#
#     # Prediksi dua periode ke depan
#     pred1 = max(0.0, model.predict(n + 1))
#     pred2 = max(0.0, model.predict(n + 2))
#
#     # Menghitung error
#     y_aktual = [d["total_penjualan"] for d in data_historis]
#     y_pred_insample = [model.predict(i + 1) for i in range(n)]
#     error = model.hitung_error(y_aktual, y_pred_insample)
#
#     # Tentukan periode prediksi
#     last_periode_str = data_historis[-1]["periode"][:7] # format YYYY-MM
#     last_date = date.fromisoformat(last_periode_str + "-01")
#     periode_pred1 = last_date + relativedelta(months=1)
#     periode_pred2 = last_date + relativedelta(months=2)
#
#     # Menyimpan ke tabel prediksi
#     prediksi_id = PrediksiModel.insert(
#         {
#             "periode_prediksi": periode_pred1,
#             "pred_periode1": pred1,
#             "pred_periode2": pred2,
#             "slope": model.slope,
#             "intercept": model.intercept,
#             "nilai_mae": error["mae"],
#             "nilai_rmse": error["rmse"],
#             "nilai_mape": error["mape"],
#         }
#     )
#
#     # Menghitung estimas baglog
#     est_kebutuhan = _hitung_estimasi_baglog(pred2)
#     est_aktif = _hitung_baglog_aktif(periode_pred2)
#     baglog_baru = _hitung_baglog_baru(est_kebutuhan, est_aktif)
#
#     rekomendasi_id = RekomendasiModel.insert(
#         {
#             "est_kebutuhan_baglog": est_kebutuhan,
#             "est_baglog_aktif": est_aktif,
#             "baglog_baru": baglog_baru,
#             "prediksi_id": prediksi_id,
#         }
#     )
#
#     # Hitung dan simpan kebutuhan bahan baku
#     bahan_baku_list = _hitung_bahan_baku(baglog_baru)
#     _simpan_bahan_baku(rekomendasi_id, bahan_baku_list)
#
#     # Susunan respons
#     hasil = {
#         "prediksi": {
#             "prediksi_id": prediksi_id,
#             "periode_pred1": str(periode_pred1),
#             "periode_pred2": str(periode_pred2),
#             "pred_periode1": round(pred1, 2),
#             "pred_periode2": round(pred2, 2),
#             "slope": round(model.slope, 4),
#             "intercept": round(model.intercept, 4),
#             "nilai_mae": round(error["mae"], 4),
#             "nilai_rmse": round(error["rmse"], 4),
#             "nilai_mape": round(error["mape"], 4),
#         },
#         "rekomendasi":{
#             "rekomendasi_id": rekomendasi_id,
#             "est_kebutuhan_baglog": est_kebutuhan,
#             "est_baglog_aktif": est_aktif,
#             "baglog_baru": baglog_baru,
#         },
#         "bahan_baku": bahan_baku_list,
#     }
#     return response_sukses(hasil, pesan="Prediksi berhasil diproses", kode=201)

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
    tahun = request.args.get("bulan", type=int)

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

@prediksi_bp.route("/periode-tersedia", methods=['GET'])
def get_periode_tersedia():
    try:
        semua_periode = PenjualanModel.get_all_agregasi_bulanan()
        if not semua_periode:
            return response_error(pesan="Belum ada data penjualan", kode=422)

        semua_periode = semua_periode[-24:] if len(semua_periode) > 24 else semua_periode

        # Set periode dari penjualan
        periode_penjualan = {d["periode"][:7] for d in semua_periode}  # ← langsung set

        # Set periode yang sudah diprediksi
        sudah_diprediksi = {
            p["periode_prediksi"][:7]
            for p in PrediksiModel.get_all()
            if p.get("periode_prediksi")
        }

        # Periode terbaru dari gabungan penjualan dan prediksi
        all_periode = periode_penjualan | sudah_diprediksi  # ← sekarang set | set ✓
        last_date = date.fromisoformat(sorted(all_periode)[-1] + "-01")
        next_periode_str = (last_date + relativedelta(months=1)).strftime("%Y-%m")

        # Kandidat = periode penjualan + 1 periode berikutnya
        kandidat = periode_penjualan | {next_periode_str}

        # Filter yang sudah diprediksi
        tersedia = sorted(kandidat - sudah_diprediksi)

        nama_bulan = [
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        ]

        hasil = []
        for p in tersedia:
            d = date.fromisoformat(p + "-01")
            hasil.append({
                "periode": p,
                "bulan":   d.month,
                "tahun":   d.year,
                "label":   f"{nama_bulan[d.month - 1]} {d.year}"
            })

        return response_sukses(hasil, pesan="Periode tersedia berhasil diambil")

    except Exception as e:
        return response_error(pesan="Gagal mengambil periode tersedia", detail=str(e), kode=500)