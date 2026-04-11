import os
from contextlib import contextmanager
from dotenv import load_dotenv
from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

load_dotenv()

class Base(DeclarativeBase):
    """Base declarative class untuk seluruh tabel ORM."""
    pass

class DatabaseConfig:
    _engine = None
    _SessionLocal = None

    @classmethod
    def _build_url(cls):
        host = os.getenv("DB_HOST", "localhost")
        port = os.getenv("DB_PORT", "5432")
        name = os.getenv("DB_NAME", "sales_forecasting_jamur")
        username = os.getenv("DB_USER", "postgres")
        password = os.getenv("DB_PASSWORD", "")
        return f"postgresql+psycopg2://{username}:{password}@{host}:{port}/{name}"

    @classmethod
    def init_engine(cls):
        if cls._engine is None:
            cls._engine = create_engine(
                cls._build_url(),
                pool_size=5,
                max_overflow=10,
                pool_pre_ping=True,
                echo=os.getenv("FLASK_DEBUG", False).lower() == "true",
            )

            cls._SessionLocal = sessionmaker(
                bind=cls._engine,
                autocommit=False,
                autoflush=False,
                expire_on_commit=False,
            )

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

    @classmethod
    def dispose(cls) -> None:
        if cls._engine:
            cls._engine.dispose()
            cls._engine = None
            cls._SessionLocal = None

@contextmanager
def get_session():
    factory = DatabaseConfig.get_session_factory()
    session = factory()
    try:
        yield session
        session.commit()
    except Exception:
        session.rollback()
        raise
    finally:
        session.close()