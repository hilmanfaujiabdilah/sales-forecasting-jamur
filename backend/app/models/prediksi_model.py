from sqlalchemy import select, extract
from config.database import get_session
from app.models.orm_tables import Prediksi

class PrediksiModel:

    @staticmethod
    def insert(data: dict) -> int:
        with get_session() as session:
            prediksi_baru = Prediksi(
                periode_prediksi=data["periode_prediksi"],
                pred_periode1=data["pred_periode1"],
                pred_periode2=data["pred_periode2"],
                slope=data["slope"],
                intercept=data["intercept"],
                nilai_mae=data["nilai_mae"],
                nilai_rmse=data["nilai_rmse"],
                nilai_mape=data["nilai_mape"],
            )
            session.add(prediksi_baru)
            session.flush()
            return prediksi_baru.prediksi_id

    @staticmethod
    def get_all() -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Prediksi).order_by(Prediksi.created_at.desc())
            ).scalars().all()
            return [p.to_dict() for p in hasil]

    @staticmethod
    def get_by_periode(bulan: int, tahun: int) -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Prediksi)
                .where(
                    extract("month", Prediksi.periode_prediksi) == bulan,
                    extract("year", Prediksi.periode_prediksi) == tahun,
                )
                .order_by(Prediksi.created_at.desc())
            ).scalars().all()
            return [p.to_dict() for p in hasil]

    @staticmethod
    def get_by_bulan_tahun(bulan: int, tahun: int):
        with get_session() as session:
            prediksi = session.execute(
                select(Prediksi)
                .where(
                    extract("month", Prediksi.periode_prediksi) == bulan,
                    extract("year", Prediksi.periode_prediksi) == tahun,
                )
                .limit(1)
            ).scalar_one_or_none()
            return prediksi.to_dict() if prediksi else None

    @staticmethod
    def get_latest():
        with get_session() as session:
            prediksi = session.execute(
                select(Prediksi)
                .order_by(Prediksi.created_at.asc())
                .limit(1)
            ).scalar_one_or_none()
            return prediksi.to_dict()