"""
orm_tables.py
=============
Definisi seluruh tabel database sebagai SQLAlchemy ORM class.

Setiap class merepresentasikan satu tabel dan menjadi sumber kebenaran
tunggal (single source of truth) untuk skema — konsisten dengan DDL
pada migrations/create_database.sql dan class diagram UML.

Urutan deklarasi disesuaikan dengan urutan FK:
  kumbung → prediksi → penjualan
                     → rekomendasi → bahan_baku
                                   → produksi
  jenis_bahan_baku → bahan_baku
"""

from datetime import datetime
from typing import Optional
from sqlalchemy import (
    CheckConstraint,
    Date,
    DateTime,
    Float,
    ForeignKey,
    Integer,
    String,
    UniqueConstraint,
    func
)
from sqlalchemy.orm import Mapped, mapped_column, relationship
from config.database import Base

class Kumbung(Base):
    """Ruang tanam (kumbung) tempat produksi jamur."""

    __tablename__ = "kumbung"

    kumbung_id:   Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    nama_kumbung: Mapped[str] = mapped_column(String(100), nullable=False)

    # Relasi ke penjualan (one-to-many)
    penjualan: Mapped[list["Penjualan"]] = relationship(
        "Penjualan", back_populates="kumbung", lazy="select"
    )

    def to_dict(self) -> dict:
        return {"kumbung_id": self.kumbung_id, "nama_kumbung": self.nama_kumbung}

class Prediksi(Base):
    """Hasil prediksi regresi linier dua periode ke depan."""

    __tablename__ = "prediksi"
    __table_args__ = (
        CheckConstraint("nilai_mae  >= 0", name="chk_prediksi_mae"),
        CheckConstraint("nilai_rmse >= 0", name="chk_prediksi_rmse"),
        CheckConstraint("nilai_mape >= 0", name="chk_prediksi_mape"),
    )

    prediksi_id:      Mapped[int]      = mapped_column(Integer, primary_key=True, autoincrement=True)
    periode_prediksi: Mapped[datetime] = mapped_column(Date, nullable=False)
    pred_periode1:    Mapped[float]    = mapped_column(Float, nullable=False)
    pred_periode2:    Mapped[float]    = mapped_column(Float, nullable=False)
    slope:            Mapped[float]    = mapped_column(Float, nullable=False)
    intercept:        Mapped[float]    = mapped_column(Float, nullable=False)
    nilai_mae:        Mapped[float]    = mapped_column(Float, nullable=False)
    nilai_rmse:       Mapped[float]    = mapped_column(Float, nullable=False)
    nilai_mape:       Mapped[float]    = mapped_column(Float, nullable=False)
    created_at:       Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at:       Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now(), onupdate=func.now())

    # Relasi
    penjualan:    Mapped[list["Penjualan"]]  = relationship("Penjualan",  back_populates="prediksi", lazy="select")
    rekomendasi:  Mapped["Rekomendasi"]      = relationship("Rekomendasi", back_populates="prediksi", uselist=False, lazy="select")

    def to_dict(self) -> dict:
        return {
            "prediksi_id":      self.prediksi_id,
            "periode_prediksi": str(self.periode_prediksi),
            "pred_periode1":    self.pred_periode1,
            "pred_periode2":    self.pred_periode2,
            "slope":            self.slope,
            "intercept":        self.intercept,
            "nilai_mae":        self.nilai_mae,
            "nilai_rmse":       self.nilai_rmse,
            "nilai_mape":       self.nilai_mape,
            "created_at":       str(self.created_at),
        }

class Penjualan(Base):
    """Catatan penjualan jamur harian per kumbung."""

    __tablename__ = "penjualan"

    penjualan_id:      Mapped[int]      = mapped_column(Integer, primary_key=True, autoincrement=True)
    tanggal_penjualan: Mapped[datetime] = mapped_column(Date, nullable=False)
    jumlah_penjualan:  Mapped[float]    = mapped_column(Float, nullable=False)
    kumbung_id:        Mapped[int]      = mapped_column(Integer, ForeignKey("kumbung.kumbung_id",  onupdate="CASCADE", ondelete="RESTRICT"), nullable=False)
    prediksi_id:       Mapped[int]      = mapped_column(Integer, ForeignKey("prediksi.prediksi_id", onupdate="CASCADE", ondelete="RESTRICT"), nullable=False)
    created_at:        Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at:        Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now(), onupdate=func.now())
    deleted_at: Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True, default=None)

    # Relasi
    kumbung:  Mapped["Kumbung"]  = relationship("Kumbung",  back_populates="penjualan", lazy="select")
    prediksi: Mapped["Prediksi"] = relationship("Prediksi", back_populates="penjualan", lazy="select")

    def to_dict(self) -> dict:
        return {
            "penjualan_id":      self.penjualan_id,
            "tanggal_penjualan": str(self.tanggal_penjualan),
            "jumlah_penjualan":  self.jumlah_penjualan,
            "kumbung_id":        self.kumbung_id,
            "prediksi_id":       self.prediksi_id,
            "created_at":        str(self.created_at),
        }

class Rekomendasi(Base):
    """Rekomendasi jumlah baglog baru yang perlu disiapkan."""

    __tablename__ = "rekomendasi"

    rekomendasi_id:       Mapped[int]      = mapped_column(Integer, primary_key=True, autoincrement=True)
    est_kebutuhan_baglog: Mapped[int]      = mapped_column(Integer, nullable=False)
    est_baglog_aktif:     Mapped[int]      = mapped_column(Integer, nullable=False)
    baglog_baru:          Mapped[int]      = mapped_column(Integer, nullable=False)
    created_at:           Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at:           Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now(), onupdate=func.now())
    prediksi_id:          Mapped[int]      = mapped_column(Integer, ForeignKey("prediksi.prediksi_id", onupdate="CASCADE", ondelete="RESTRICT"), nullable=False, unique=True)

    # Relasi
    prediksi:    Mapped["Prediksi"]          = relationship("Prediksi",   back_populates="rekomendasi", lazy="select")
    bahan_baku:  Mapped[list["BahanBaku"]]   = relationship("BahanBaku",  back_populates="rekomendasi", cascade="all, delete-orphan", lazy="select")
    produksi:    Mapped[list["Produksi"]]    = relationship("Produksi",   back_populates="rekomendasi", lazy="select")

    def to_dict(self) -> dict:
        return {
            "rekomendasi_id":       self.rekomendasi_id,
            "est_kebutuhan_baglog": self.est_kebutuhan_baglog,
            "est_baglog_aktif":     self.est_baglog_aktif,
            "baglog_baru":          self.baglog_baru,
            "prediksi_id":          self.prediksi_id,
            "created_at":           str(self.created_at),
        }

class JenisBahanBaku(Base):
    """Master data jenis bahan baku (Serbuk Kayu, Bekatul, dll.)."""

    __tablename__ = "jenis_bahan_baku"

    jenis_bahan_baku_id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    nama_bahan_baku:     Mapped[str] = mapped_column(String(100), nullable=False, unique=True)
    satuan_bahan_baku:   Mapped[str] = mapped_column(String(10), nullable=False)

    # Relasi
    bahan_baku: Mapped[list["BahanBaku"]] = relationship("BahanBaku", back_populates="jenis", lazy="select")

    def to_dict(self) -> dict:
        return {
            "jenis_bahan_baku_id": self.jenis_bahan_baku_id,
            "nama_bahan_baku":     self.nama_bahan_baku,
            "satuan_bahan_baku":   self.satuan_bahan_baku,
        }

class BahanBaku(Base):
    """Kebutuhan bahan baku per rekomendasi produksi."""

    __tablename__ = "bahan_baku"
    __table_args__ = (
        CheckConstraint("jumlah_bahan_baku >= 0", name="chk_bahanbaku_jumlah"),
        UniqueConstraint("rekomendasi_id", "jenis_bahan_baku_id", name="uq_bahanbaku_per_rekomendasi"),
    )

    bahan_baku_id:       Mapped[int]      = mapped_column(Integer, primary_key=True, autoincrement=True)
    jumlah_bahan_baku:   Mapped[float]    = mapped_column(Float, nullable=False)
    rekomendasi_id:      Mapped[int]      = mapped_column(Integer, ForeignKey("rekomendasi.rekomendasi_id",       onupdate="CASCADE", ondelete="CASCADE"),  nullable=False)
    jenis_bahan_baku_id: Mapped[int]      = mapped_column(Integer, ForeignKey("jenis_bahan_baku.jenis_bahan_baku_id", onupdate="CASCADE", ondelete="RESTRICT"), nullable=False)
    created_at:          Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at:          Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now(), onupdate=func.now())

    # Relasi
    rekomendasi: Mapped["Rekomendasi"]   = relationship("Rekomendasi",   back_populates="bahan_baku", lazy="select")
    jenis:       Mapped["JenisBahanBaku"] = relationship("JenisBahanBaku", back_populates="bahan_baku", lazy="select")

    def to_dict(self) -> dict:
        return {
            "bahan_baku_id":     self.bahan_baku_id,
            "jumlah_bahan_baku": self.jumlah_bahan_baku,
            "rekomendasi_id":    self.rekomendasi_id,
            "jenis_bahan_baku_id": self.jenis_bahan_baku_id,
            "nama_bahan_baku":   self.jenis.nama_bahan_baku if self.jenis else None,
            "satuan_bahan_baku": self.jenis.satuan_bahan_baku if self.jenis else None,
        }

class Produksi(Base):
    """Realisasi produksi harian jamur tiram."""

    __tablename__ = "produksi"
    __table_args__ = (
        CheckConstraint("jumlah_produksi >= 0", name="chk_produksi_jumlah"),
    )

    produksi_id:      Mapped[int]      = mapped_column(Integer, primary_key=True, autoincrement=True)
    tanggal_produksi: Mapped[datetime] = mapped_column(Date, nullable=False)
    jumlah_produksi:  Mapped[float]    = mapped_column(Float, nullable=False)
    rekomendasi_id:   Mapped[int]      = mapped_column(Integer, ForeignKey("rekomendasi.rekomendasi_id", onupdate="CASCADE", ondelete="RESTRICT"), nullable=False)
    created_at:       Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at:       Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now(), onupdate=func.now())
    deleted_at:       Mapped[Optional[datetime]] = mapped_column(DateTime, nullable=True, default=None)

    # Relasi
    rekomendasi: Mapped["Rekomendasi"] = relationship("Rekomendasi", back_populates="produksi", lazy="joined")

    def to_dict(self) -> dict:
        return {
            "produksi_id":      self.produksi_id,
            "tanggal_produksi": str(self.tanggal_produksi),
            "jumlah_produksi":  self.jumlah_produksi,
            "rekomendasi_id":   self.rekomendasi_id,
            "created_at":       str(self.created_at),
        }