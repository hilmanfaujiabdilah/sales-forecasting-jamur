import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, root_mean_squared_error, mean_absolute_percentage_error

class ModelRegresi:

    def __init__(self):
        self._model = LinearRegression()
        self._trained = False

    def train_model(self, data: list) -> None:
        if len(data) < 2:
            raise ValueError("Data minimal 2 periode untuk melatih model regresi")

        y = np.array([d["total_penjualan"] for d in data], dtype=float)
        x = np.arange(1, len(y) + 1).reshape(-1, 1)

        self._model.fit(x, y)
        self._trained = True

    def predict(self, x: int) -> float:
        if not self._trained:
            raise RuntimeError("Model belum dilatih, Panggil train_model() terlebih dahulu.")
        return float(self._model.predict(np.array([[x]]))[0])

    def hitung_error(self, aktual: list, pred: list) -> dict:
        y_aktual = np.array(aktual, dtype=float)
        y_pred = np.array(pred, dtype=float)

        mae = mean_absolute_error(y_aktual, y_pred)
        rmse = root_mean_squared_error(y_aktual, y_pred)
        mape = mean_absolute_percentage_error(y_aktual, y_pred)

        return {"mae": mae, "rmse": rmse, "mape": round(mape, 2)}

    def is_model_trained(self) -> bool:
        return self._trained

    @property
    def slope(self) -> float:
        if not self._trained:
            return 0.0
        return float(self._model.coef_[0])

    @property
    def intercept(self) -> float:
        if not self._trained:
            return 0.0
        return float(self._model.intercept_)