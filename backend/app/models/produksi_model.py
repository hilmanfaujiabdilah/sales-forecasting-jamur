from sqlalchemy import select, extract, func
from sqlalchemy.orm import joinedload
from datetime import date, datetime
from config.database import get_session
from app.models.orm_tables import Produksi

class ProduksiModel:

    @staticmethod
    def insert(data: dict) -> bool:
        with get_session() as session:
            produksi_baru = Produksi(
                tanggal_produksi=data["tanggal_produksi"],
                jumlah_produksi=data["jumlah_produksi"],
                rekomendasi_id=data["rekomendasi_id"],
            )
            session.add(produksi_baru)
            return True

    @staticmethod
    def get_by_periode(bulan: int, tahun: int) -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Produksi)
                .options(joinedload(Produksi.rekomendasi))
                .where(
                    extract("month", Produksi.tanggal_produksi) == bulan,
                    extract("year", Produksi.tanggal_produksi) == tahun,
                    Produksi.deleted_at.is_(None)
                )
                .order_by(Produksi.tanggal_produksi)
            ).scalars().all()

            return [
                {
                    **p.to_dict(),
                    "est_kebutuhan_baglog": p.rekomendasi.est_kebutuhan_baglog if p.rekomendasi else None,
                    "baglog_baru":          p.rekomendasi.baglog_baru          if p.rekomendasi else None,
                }
                for p in hasil
            ]

    @staticmethod
    def get_baglog_aktif(batas_awal: date, batas_akhir: date) -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Produksi)
                .where(
                    Produksi.tanggal_produksi >= batas_awal,
                    Produksi.tanggal_produksi <= batas_akhir,
                    Produksi.deleted_at.is_(None)
                )
                .order_by(Produksi.tanggal_produksi)
            ).scalars().all()

            return [p.to_dict() for p in hasil]

    @staticmethod
    def get_agregasi_bulanan() -> list:
        from app.models.orm_tables import Rekomendasi, Prediksi
        with get_session() as session:
            hasil = session.execute(
                select(
                    Rekomendasi.rekomendasi_id,
                    Rekomendasi.baglog_baru,
                    Prediksi.periode_prediksi,
                    func.coalesce(
                        func.sum(Produksi.jumlah_produksi), 0
                    ).label("total_produksi")
                )
                .join(Prediksi, Rekomendasi.prediksi_id == Prediksi.prediksi_id)
                .outerjoin(
                    Produksi,
                    (Produksi.rekomendasi_id == Rekomendasi.rekomendasi_id) &
                    (Produksi.deleted_at.is_(None))
                )
                .group_by(
                    Rekomendasi.rekomendasi_id,
                    Rekomendasi.baglog_baru,
                    Prediksi.periode_prediksi,
                )
                .order_by(Prediksi.periode_prediksi)
            ).all()

            return [
                {
                    "periode": str(r.periode_prediksi),
                    "total_penjualan": float(r.total_produksi),
                    "rekomendasi_id": r.rekomendasi_id,
                    "baglog_baru": r.baglog_baru,
                    "bulan": r.periode_prediksi.month,
                    "tahun": r.periode_prediksi.year,
                }
                for r in hasil
            ]
    @staticmethod
    def hapus(produksi_id: int) -> bool:
        with get_session() as session:
            produksi = session.execute(
                select(Produksi).where(
                    Produksi.produksi_id == produksi_id,
                    Produksi.deleted_at.is_(None)  # pastikan belum dihapus
                )
            ).scalar_one_or_none()

            if produksi is None:
                return False  # tidak ditemukan atau sudah dihapus

            produksi.deleted_at = datetime.now()
            return True