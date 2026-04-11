from sqlalchemy import text
from database import DatabaseConfig

def test_connection():
    try:
        engine = DatabaseConfig.get_engine()
        with engine.connect() as conn:
            result = conn.execute(text("SELECT 1"))
            print("Koneksi berhasil:", result.scalar())
    except Exception as e:
        print("Koneksi gagal:", e)

if __name__ == "__main__":
    test_connection()