from sqlalchemy import select
from sqlalchemy.ext.horizontal_shard import set_shard_id

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
                prediksi=data["prediksi_id"]
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
            session.add(object_baru)
            return True

    @staticmethod
    def get_by_rekomendasi_id(rekomendasi_id: int) -> list:
        with get_session() as session:
            hasil = session.execute(
                select(BahanBaku)
                .join(JenisBahanBaku, BahanBaku.jenis_bahan_baku_id == JenisBahanBaku.jenis_bahan_baku_id)
                .where(BahanBaku.rekomendasi_id == rekomendasi_id)
                .order_by(JenisBahanBaku.nama_bahan_baku)
            ).scalars().all()
            return [bb.to_dict() for bb in hasil]

    @staticmethod
    def get_all_jenis() -> list:
        with get_session() as session:
            hasil = session.execute(
                select(JenisBahanBaku).order_by(JenisBahanBaku.nama_bahan_baku)
            ).scalars().all()
            return [j.to_dict() for j in hasil]