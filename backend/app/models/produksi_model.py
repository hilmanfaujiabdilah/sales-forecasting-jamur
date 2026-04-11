from sqlalchemy import select, extract
from datetime import date
from config.database import get_session
from app.models.orm_tables import Produksi, Rekomendasi

class ProduksiModel:

    @staticmethod
    def insert(data: dict) -> bool:
        with get_session() as session:
            produksi_baru = Produksi(
                tanggal_produksi=data["tanggal_penjualan"],
                jumlah_produksi=data["jumlah_produksi"],
                rekomendasi=data["rekomendasi"],
            )
            session.add(produksi_baru)
            return True

    @staticmethod
    def get_by_periode(bulan: int, tahun: int) -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Produksi)
                .join(Rekomendasi, Produksi.produksi_id == Rekomendasi.rekomendasi_id)
                .where(
                    extract("month", Produksi.tanggal_produksi) == bulan,
                    extract("year", Produksi.tanggal_produksi) == tahun,
                )
                .order_by(Produksi.tanggal_produksi)
            ).scalars().all()

            return [
                {
                    **p.to_dict(),
                    "est_kebutuhan_baglog": p.rekomendasi.est_kebutuhan_baglog,
                    "baglog_baru": p.rekomendasi.baglog_baru,
                }
                for p in hasil
            ]

    @staticmethod
    def get_baglog_aktif(batas_awal: date, batas_akhir: date) -> list:
        """
        Rentang dihitung dari controller:
            batas_awal      = periode_pred2 - 3 bulan
            batas_akrhir    = periode_pred2 - 1 bulan

        Contoh (prediksi dilakukan 30 Oktober):
            periode_pred2 = Desember
            batas_awal    = September
            batas_akhir   = November
            → mengambil produksi bulan September, Oktober, November
        """
        with get_session() as session:
            hasil = session.execute(
                select(Produksi)
                .where(
                    Produksi.tanggal_produksi >= batas_awal,
                    Produksi.tanggal_produksi <= batas_akhir
                )
                .order_by(Produksi.tanggal_produksi)
            ).scalars().all()

            return [p.to_dict() for p in hasil]