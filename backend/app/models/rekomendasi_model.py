from sqlalchemy import select
from config.database import get_session
from app.models.orm_tables import Rekomendasi, BahanBaku, JenisBahanBaku

class RekomendasiModel:

    @staticmethod
    def insert(data: dict) -> int:
        with get_session() as session:
            rek_baru = Rekomendasi(
                est_kebutuhan_baglog=data["est_kebutuhan_baglog"],
                est_baglog_aktif=data["est_baglog_aktif"],
                baglog_baru=data["baglog_baru"],
                prediksi_id=data["prediksi_id"]
            )
            session.add(rek_baru)
            session.flush()
            return rek_baru.rekomendasi_id

    @staticmethod
    def get_by_prediksi_id(prediksi_id: int):
        with get_session() as session:
            rek = session.execute(
                select(Rekomendasi).where(Rekomendasi.prediksi_id == prediksi_id)
            ).scalar_one_or_none()
            return rek.to_dict() if rek else None
        
    @staticmethod
    def get_by_id(rekomendasi_id: int) -> dict | None:
        with get_session() as session:
            hasil = session.execute(
                select(Rekomendasi).where(Rekomendasi.rekomendasi_id == rekomendasi_id)
            ).scalar_one_or_none()

            return hasil.to_dict() if hasil else None

class BahanBakuModel:

    @staticmethod
    def insert_bahan_baku(data: list) -> bool:
        with get_session() as session:
            object_baru = [
                BahanBaku(
                    jumlah_bahan_baku=item["jumlah_bahan_baku"],
                    rekomendasi_id=item["rekomendasi_id"],
                    jenis_bahan_baku_id=item["jenis_bahan_baku_id"]
                )
                for item in data
            ]
            session.add_all(object_baru)
            return True

    @staticmethod
    def get_by_rekomendasi_id(rekomendasi_id: int) -> list:
        with get_session() as session:
            hasil = session.execute(
                select(BahanBaku, JenisBahanBaku)
                .join(JenisBahanBaku, BahanBaku.jenis_bahan_baku_id == JenisBahanBaku.jenis_bahan_baku_id)
                .where(BahanBaku.rekomendasi_id == rekomendasi_id)
                .order_by(JenisBahanBaku.nama_bahan_baku)
            ).all()
            return [
                {
                    "nama_bahan_baku": row.JenisBahanBaku.nama_bahan_baku,
                    "jumlah": row.BahanBaku.jumlah_bahan_baku,
                    "satuan": row.JenisBahanBaku.satuan_bahan_baku,
                }
                for row in hasil
            ]

    @staticmethod
    def get_all_jenis() -> list:
        with get_session() as session:
            hasil = session.execute(
                select(JenisBahanBaku).order_by(JenisBahanBaku.nama_bahan_baku)
            ).scalars().all()
            return [j.to_dict() for j in hasil]