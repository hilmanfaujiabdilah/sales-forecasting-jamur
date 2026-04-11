from flask import Flask
from flask_cors import CORS
from config.database import DatabaseConfig
from app.controller import kumbung_bp, penjualan_bp, prediksi_bp, produksi_bp
import app.models.orm_tables

def create_app() -> Flask:
    app = Flask(__name__)
    CORS(app)

    DatabaseConfig.init_engine()

    app.register_blueprint(kumbung_bp)
    app.register_blueprint(penjualan_bp)
    app.register_blueprint(prediksi_bp)
    app.register_blueprint(produksi_bp)

    @app.route('/api/health', methods=['GET'])
    def health_check():
        return {"status": "ok", "pesan": "Forecasting API berjalan"}, 200

    @app.teardown_appcontext
    def shutdown_session(exception=None):
        DatabaseConfig.dispose()

    return app