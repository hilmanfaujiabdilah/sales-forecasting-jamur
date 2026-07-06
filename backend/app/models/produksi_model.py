from sqlalchemy import select, extract, func
from sqlalchemy.orm import joinedload
from datetime import date, datetime
from config.database import get_session
from app.models.orm_tables import Produksi, Prediksi

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

    # @staticmethod
    # def get_baglog_aktif(batas_awal: date, batas_akhir: date) -> list:
    #     with get_session() as session:
    #         hasil = session.execute(
    #             select(Produksi)
    #             .where(
    #                 Produksi.tanggal_produksi >= batas_awal,
    #                 Produksi.tanggal_produksi <= batas_akhir,
    #                 Produksi.deleted_at.is_(None)
    #             )
    #             .order_by(Produksi.tanggal_produksi)
    #         ).scalars().all()
    #
    #         return [p.to_dict() for p in hasil]

    @staticmethod
    def get_baglog_aktif(batas_awal: date, batas_akhir: date) -> int:
        with get_session() as session:
            hasil = session.execute(
                select(func.sum(Produksi.jumlah_produksi))
                .where(
                    Produksi.tanggal_produksi >= batas_awal,
                    Produksi.tanggal_produksi <= batas_akhir,
                    Produksi.deleted_at.is_(None)
                )
            ).scalar()

            return int(hasil) if hasil else 0

    # @staticmethod
    # def get_agregasi_bulanan() -> list:
    #     with get_session() as session:
    #         hasil = session.execute(
    #             select(
    #                 func.date_trunc("month", Produksi.tanggal_produksi).label("periode"),
    #                 func.sum(Produksi.jumlah_produksi).label("total_produksi"),
    #             )
    #             .where(Produksi.deleted_at.is_(None))
    #             .group_by("periode")
    #             .order_by("periode")
    #         ).all()
    #
    #         return [
    #             {
    #                 "periode": str(r.periode),
    #                 "total_produksi": float(r.total_produksi),
    #                 "bulan": r.periode.month,
    #                 "tahun": r.periode.year,
    #             }
    #             for r in hasil
    #         ]

    @staticmethod
    def get_agregasi_bulanan() -> list:
        with get_session() as session:
            # Data produksi aktual
            hasil_produksi = session.execute(
                select(
                    func.date_trunc("month", Produksi.tanggal_produksi).label("periode"),
                    func.sum(Produksi.jumlah_produksi).label("total_produksi"),
                )
                .where(Produksi.deleted_at.is_(None))
                .group_by("periode")
                .order_by("periode")
            ).all()

            # Periode yang sudah ada di produksi
            periode_ada = {r.periode.strftime("%Y-%m") for r in hasil_produksi}

            # Periode dari prediksi yang belum ada di produksi
            hasil_prediksi = session.execute(
                select(Prediksi.periode_prediksi)
                .order_by(Prediksi.periode_prediksi)
            ).scalars().all()

            # Gabungkan
            data = [
                {
                    "periode": str(r.periode),
                    "total_produksi": float(r.total_produksi),
                    "bulan": r.periode.month,
                    "tahun": r.periode.year,
                }
                for r in hasil_produksi
            ]

            for p in hasil_prediksi:
                if p.strftime("%Y-%m") not in periode_ada:
                    data.append({
                        "periode": str(p),
                        "total_produksi": 0.0,
                        "bulan": p.month,
                        "tahun": p.year,
                    })

            # Urutkan berdasarkan periode
            data.sort(key=lambda x: x["periode"])
            return data

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