# Forecasting Jamur Tiram — Backend API

Backend Flask REST API untuk sistem peramalan penjualan dan rekomendasi produksi
jamur tiram berbasis regresi linier sederhana.

---

## Struktur Folder

```
forecasting_backend/
├── run.py                          # Entry point aplikasi
├── requirements.txt
├── .env.example                    # Contoh variabel environment
│
├── config/
│   ├── __init__.py
│   └── database.py                 # Konfigurasi & connection pool PostgreSQL
│
├── migrations/
│   └── create_database.sql         # DDL + seed master data
│
└── app/
    ├── __init__.py                  # Application factory (create_app)
    │
    ├── models/                      # Layer Model — akses database
    │   ├── __init__.py
    │   ├── kumbung_model.py
    │   ├── penjualan_model.py
    │   ├── prediksi_model.py
    │   ├── rekomendasi_model.py     # RekomendasiModel + BahanBakuModel
    │   ├── produksi_model.py
    │   └── model_regresi.py        # Wrapper scikit-learn LinearRegression
    │
    ├── controllers/                 # Layer Controller — route & business logic
    │   ├── __init__.py
    │   ├── kumbung_controller.py
    │   ├── penjualan_controller.py
    │   ├── prediksi_controller.py   # Pipeline utama forecasting
    │   └── produksi_controller.py
    │
    └── utils/
        ├── __init__.py
        └── response.py              # Helper response_sukses / response_error
```

---

## Instalasi & Menjalankan

### 1. Clone & masuk ke folder
```bash
cd forecasting_backend
```

### 2. Buat virtual environment
```bash
python -m venv venv
source venv/bin/activate        # Linux/Mac
venv\Scripts\activate           # Windows
```

### 3. Install dependensi
```bash
pip install -r requirements.txt
```

### 4. Konfigurasi environment
```bash
cp .env.example .env
# Edit .env sesuai konfigurasi PostgreSQL Anda
```

### 5. Buat database & jalankan DDL
```bash
psql -U postgres -c "CREATE DATABASE forecasting_db;"
psql -U postgres -d forecasting_db -f migrations/create_database.sql
```

### 6. Jalankan server
```bash
python run.py
```
Server berjalan di `http://localhost:5000`

---

## Endpoint API

### Health Check
| Method | URL | Keterangan |
|--------|-----|------------|
| GET | `/api/health` | Cek status server |

### Kumbung
| Method | URL | Keterangan |
|--------|-----|------------|
| GET | `/api/kumbung` | Ambil semua kumbung |
| GET | `/api/kumbung/<id>` | Ambil kumbung by ID |
| POST | `/api/kumbung` | Tambah kumbung baru |
| DELETE | `/api/kumbung/<id>` | Hapus kumbung |

### Penjualan
| Method | URL | Keterangan |
|--------|-----|------------|
| GET | `/api/penjualan` | Ambil semua penjualan |
| GET | `/api/penjualan/periode` | Daftar periode tersedia |
| POST | `/api/penjualan` | Catat penjualan baru |

### Prediksi & Rekomendasi
| Method | URL | Keterangan |
|--------|-----|------------|
| POST | `/api/prediksi/proses` | Jalankan pipeline forecasting |
| GET | `/api/prediksi` | Semua riwayat prediksi |
| GET | `/api/prediksi/periode?bulan=&tahun=` | Prediksi by periode |
| GET | `/api/prediksi/rekomendasi/<prediksi_id>` | Rekomendasi baglog |
| GET | `/api/prediksi/bahan-baku/<rekomendasi_id>` | Kebutuhan bahan baku |

### Produksi
| Method | URL | Keterangan |
|--------|-----|------------|
| POST | `/api/produksi` | Catat realisasi produksi |
| GET | `/api/produksi/periode?bulan=&tahun=` | Produksi by periode |

---

## Format Response

Semua endpoint mengembalikan JSON standar:

**Sukses:**
```json
{
  "status": "sukses",
  "pesan": "...",
  "data": { ... }
}
```

**Error:**
```json
{
  "status": "error",
  "pesan": "...",
  "detail": "..."
}
```

---

## Contoh Request: Proses Prediksi

```http
POST /api/prediksi/proses
Content-Type: application/json
```

**Response:**
```json
{
  "status": "sukses",
  "pesan": "Prediksi berhasil diproses",
  "data": {
    "prediksi": {
      "prediksi_id": 1,
      "periode_prediksi": "2025-02-01",
      "pred_periode1": 120.5,
      "pred_periode2": 135.2,
      "slope": 7.35,
      "intercept": 85.1,
      "nilai_mae": 5.23,
      "nilai_rmse": 6.41,
      "nilai_mape": 4.87
    },
    "rekomendasi": {
      "rekomendasi_id": 1,
      "est_kebutuhan_baglog": 338,
      "est_baglog_aktif": 200,
      "baglog_baru": 138
    },
    "bahan_baku": [
      { "nama_bahan_baku": "Serbuk Kayu", "jumlah": 165.6 },
      { "nama_bahan_baku": "Bekatul",     "jumlah": 27.6 },
      { "nama_bahan_baku": "Kapur",       "jumlah": 2.76 },
      { "nama_bahan_baku": "Air",         "jumlah": 69.0 }
    ]
  }
}
```

---

## Deployment (Render / Railway)

```bash
gunicorn run:app --bind 0.0.0.0:$PORT
```

Pastikan variabel environment sudah dikonfigurasi di dashboard platform deployment.
