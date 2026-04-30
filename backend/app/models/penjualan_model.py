from sqlalchemy import select, func, extract
from datetime import datetime
from config.database import get_session
from app.models.orm_tables import Penjualan, Kumbung

class PenjualanModel:

    @classmethod
    def insert(cls, data: dict) -> bool:
        with get_session() as session:
            penjualan_baru = Penjualan(
                tanggal_penjualan=data["tanggal_penjualan"],
                jumlah_penjualan=data["jumlah_penjualan"],
                kumbung_id=data["kumbung_id"],
                prediksi_id=data["prediksi_id"],
            )
            session.add(penjualan_baru)
            return True

    @staticmethod
    def get_all_penjualan() -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Penjualan).join(Kumbung, Penjualan.kumbung_id == Kumbung.kumbung_id)
                .where(Penjualan.deleted_at.is_(None))
                .order_by(Penjualan.tanggal_penjualan.asc())
            ).scalars().all()

            return [
                {
                    **p.to_dict(),
                    "nama_kumbung": p.kumbung.nama_kumbung
                }
                for p in hasil
            ]

    @staticmethod
    def get_all_periode() -> list:
        with get_session() as session:
            hasil = session.execute(
                select(
                    extract("year", Penjualan.tanggal_penjualan).label("tahun"),
                    extract("month", Penjualan.tanggal_penjualan).label("bulan")
                )
                .where(Penjualan.deleted_at.is_(None))
                .distinct()
                .order_by("tahun", "bulan")
            ).all()

            return [{"tahun": int(r.tahun), "bulan": int(r.bulan)} for r in hasil]

    @staticmethod
    def get_by_periode(bulan: int, tahun: int) -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Penjualan)
                .where(
                    extract("month", Penjualan.tanggal_penjualan) == bulan,
                    extract("year", Penjualan.tanggal_penjualan) == tahun,
                    Penjualan.deleted_at.is_(None)
                )
                .order_by(Penjualan.tanggal_penjualan.asc())
            ).scalars().all()

            return [p.to_dict() for p in hasil]

    @staticmethod
    def get_all_agregasi_bulanan() -> list:
        with get_session() as session:
            hasil = session.execute(
                select(
                    func.date_trunc("month", Penjualan.tanggal_penjualan).label("periode"),
                    func.sum(Penjualan.jumlah_penjualan).label("total_penjualan")
                )
                .where(Penjualan.deleted_at.is_(None))
                .group_by("periode")
                .order_by("periode")
            ).all()

            return [
                {
                    "periode": str(r.periode),
                    "total_penjualan": float(r.total_penjualan),
                }
                for r in hasil
            ]

    @staticmethod
    def hapus(penjualan_id: int) -> bool:
        with get_session() as session:
            penjualan = session.execute(
                select(Penjualan).where(
                    Penjualan.penjualan_id == penjualan_id,
                    Penjualan.deleted_at.is_(None)
                )
            ).scalar_one_or_none()

            if penjualan is None:
                return False

            penjualan.deleted_at = datetime.now()
            return True

