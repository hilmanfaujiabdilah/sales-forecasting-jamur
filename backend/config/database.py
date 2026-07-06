import os
from contextlib import contextmanager
from dotenv import load_dotenv
from sqlalchemy import create_engine, text
from sqlalchemy.orm import DeclarativeBase, sessionmaker
import logging
from urllib.parse import quote_plus

# Load .env dari root project (bukan hanya folder saat ini)
load_dotenv(override=True)

logger = logging.getLogger(__name__)


class Base(DeclarativeBase):
    """Base declarative class untuk seluruh tabel ORM."""
    pass


class DatabaseConfig:
    _engine = None
    _SessionLocal = None

    # @classmethod
    # def _build_url(cls):
    #     host = os.getenv("DB_HOST", "localhost")
    #     port = os.getenv("DB_PORT", "5432")
    #     name = os.getenv("DB_NAME", "postgres")
    #     username = os.getenv("DB_USER", "postgres")
    #     password = os.getenv("DB_PASSWORD", "")
    #
    #     # Debug: cek nilai yang terbaca
    #     logger.debug(f"DB_HOST: {host}, DB_PORT: {port}, DB_NAME: {name}, DB_USER: {username}")
    #
    #     return f"postgresql+psycopg2://{username}:{password}@{host}:{port}/{name}"

    # @classmethod
    # def _build_url(cls):
    #     return "postgresql+psycopg2://postgres.dfgusewgwclhhkxdeimy:5M@rt.$salesForecast@aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres"

    @classmethod
    def _build_url(cls):
        host = os.getenv("DB_HOST", "")
        port = os.getenv("DB_PORT", "5432")
        name = os.getenv("DB_NAME", "")
        user = os.getenv("DB_USER", "")
        password = quote_plus(os.getenv("DB_PASSWORD", ""))  # encode karakter spesial!

        return f"postgresql+psycopg2://{user}:{password}@{host}:{port}/{name}"

    @classmethod
    def init_engine(cls):
        if cls._engine is None:
            url = cls._build_url()
            is_debug = str(os.getenv("FLASK_DEBUG", "false")).lower() == "true"

            cls._engine = create_engine(
                url,
                pool_size=5,
                max_overflow=10,
                pool_pre_ping=True,
                pool_timeout=30,          # ← tambahan: timeout koneksi
                pool_recycle=1800,        # ← tambahan: recycle koneksi tiap 30 menit
                connect_args={
                    "connect_timeout": 10  # ← tambahan: timeout saat connect
                },
                echo=is_debug,
            )

            cls._SessionLocal = sessionmaker(
                bind=cls._engine,
                autocommit=False,
                autoflush=False,
                expire_on_commit=False,
            )

            logger.info("Database engine initialized successfully.")

    @classmethod
    def get_engine(cls):
        if cls._engine is None:
            cls.init_engine()
        return cls._engine

    @classmethod
    def get_session_factory(cls):
        if cls._SessionLocal is None:
            cls.init_engine()
        return cls._SessionLocal

    @classmethod
    def create_all_tables(cls) -> None:
        Base.metadata.create_all(bind=cls.get_engine())
        logger.info("All tables created.")

    @classmethod
    def test_connection(cls) -> bool:
        """Tes koneksi ke database, return True jika berhasil."""
        try:
            with cls.get_engine().connect() as conn:
                conn.execute(text("SELECT 1"))
            logger.info("Database connection test: SUCCESS")
            return True
        except Exception as e:
            logger.error(f"Database connection test: FAILED - {e}")
            return False

    @classmethod
    def dispose(cls) -> None:
        if cls._engine:
            cls._engine.dispose()
            cls._engine = None
            cls._SessionLocal = None
            logger.info("Database engine disposed.")


@contextmanager
def get_session():
    factory = DatabaseConfig.get_session_factory()
    session = factory()
    try:
        yield session
        session.commit()
    except Exception as e:
        session.rollback()
        logger.error(f"Session rollback due to: {e}")
        raise
    finally:
        session.close()