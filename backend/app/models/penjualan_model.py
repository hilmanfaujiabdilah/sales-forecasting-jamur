from sqlalchemy import select, func, extract

from app.models import Kumbung
from config.database import get_session
from app.models.orm_tables import Penjualan, Kumbung

class PenjualanModel:

    @classmethod
    def insert(data: dict) -> bool:
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
                .order_by(Penjualan.jumlah_penjualan.desc())
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
                    extract("year", Penjualan.tanggal_penjualan) == tahun
                )
                .order_by(Penjualan.tanggal_penjualan.desc())
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