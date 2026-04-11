from flask import jsonify

def response_sukses(data=None, pesan="Berhasil", kode=200):
    payload = {"status": "sukses", "pesan": pesan}
    if data is not None:
        payload["data"] = data
    return jsonify(payload), kode

def response_error(pesan="Terjadi kesalahan", kode=400, detail=None):
    payload = {"status": "error", "pesan": pesan}
    if detail is not None:
        payload["data"] = detail
    return jsonify(payload), kode