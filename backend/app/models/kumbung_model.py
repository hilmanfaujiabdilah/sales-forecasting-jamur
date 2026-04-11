from sqlalchemy import select

from app.models import Kumbung
from config.database import get_session
from app.models.orm_tables import Kumbung

class KumbungModel:

    @staticmethod
    def get_all() -> list:
        with get_session() as session:
            hasil = session.execute(
                select(Kumbung).order_by(Kumbung.kumbung_id)
            ).scalars().all()
            return [k.to_dict() for k in hasil]

    @staticmethod
    def get_by_id(kumbung_id: int):
        with get_session() as session:
            kumbung = session.get(Kumbung, kumbung_id)
            return kumbung.to_dict() if kumbung else None

    @staticmethod
    def insert(nama: str) -> int:
        with get_session() as session:
            kumbung_baru = Kumbung(nama_kumbung=nama)
            session.add(kumbung_baru)
            session.flush()
            return kumbung_baru.kumbung_id

    @staticmethod
    def delete(kumbung_id: int):
        with get_session() as session:
            kumbung = session.get(Kumbung, kumbung_id)
            if kumbung is None:
                return False
            session.delete(kumbung)
            return True