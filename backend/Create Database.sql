-- ===========================
-- MEMBUT DATABASE FORECASTING
-- ===========================


-- 1. Table Kumbung

create table kumbung(
	kumbung_id serial primary key,
	nama_kumbung varchar(100) not null
);

-- 2. Tabel Penjualan

create table penjualan(
	penjualan_id serial primary key,
	tanggal_penjualan date not null,
	jumlah_penjualan float not null,
	kumbung_id int not null,
	prediksi_id int not null,
	created_at timestamp not null default now(),
	updated_at timestamp not null default now(),
	deleted_at timestamp null,

	CONSTRAINT fk_penjualan_kumbung
		foreign key (kumbung_id)
		references kumbung(kumbung_id)
		on update cascade
		on delete restrict,

	CONSTRAINT fk_penjualan_prediksi
		foreign key (prediksi_id)
		references prediksi(prediksi_id)
		on update cascade
		on delete restrict
);

-- 3. Tabel Prediksi

create table prediksi(
	prediksi_id serial primary key,
	periode_prediksi date not null,
	pred_periode1 float not null,
	pred_periode2 float not null,
	slope float not null,
	intercept float not null,
	nilai_mae float not null check(nilai_mae >= 0),
	nilai_rmse float not null check(nilai_rmse >= 0),
	nilai_mape float not null check(nilai_mape >= 0),
	created_at timestamp not null default now(),
	updated_at timestamp not null default now()
);


-- 4. Tabel Rekomendasi

create table rekomendasi(
	rekomendasi_id serial primary key,
	est_kebutuhan_baglog int not null ,
	est_baglog_aktif int not null,
	baglog_baru int not null,
	created_at timestamp not null default now(),
	updated_at timestamp not null default now(),
	prediksi_id int not null unique,


	CONSTRAINT fk_rekomendasi_prediksi
		foreign key (prediksi_id)
		references prediksi(prediksi_id)
		on update cascade
		on delete restrict
);

-- 5. Tabel Jenis Bahan Baku

create table jenis_bahan_baku(
	jenis_bahan_baku_id serial primary key,
	nama_bahan_baku varchar(100) not null unique,
	satuan_bahan_baku varchar(10) not null
);


-- 6. Tabel Kebutuhan Bahan Baku

create table bahan_baku(
	bahan_baku_id serial primary key,
	jumlah_bahan_baku float not null check(jumlah_bahan_baku >=0 ),
	rekomendasi_id int not null,
	jenis_bahan_baku_id int not null,
	created_at timestamp not null default now(),
	updated_at timestamp not null default now(),

	constraint fk_bahanbaku_rekomendasi
		foreign key (rekomendasi_id)
		references rekomendasi(rekomendasi_id)
		on update cascade
		on delete cascade,

	constraint fk_bakanbaku_jenis
		foreign key (jenis_bahan_baku_id)
		references jenis_bahan_baku(jenis_bahan_baku_id)
		on update cascade
		on delete restrict,

	constraint uq_bahanbaku_per_rekomendasi
		unique (rekomendasi_id, jenis_bahan_baku_id)
);

-- 7. Tabel Produksi

create table produksi(
	produksi_id serial primary key,
	tanggal_produksi date not null,
	jumlah_produksi float not null check (jumlah_produksi >= 0),
	rekomendasi_id int not null,
	created_at timestamp not null default now(),
	updated_at timestamp not null default now(),
	deleted_at timestamp null,

	constraint fk_produksi_rekomendasi
		foreign key (rekomendasi_id)
		references rekomendasi(rekomendasi_id)
		on update cascade
		on delete restrict
);

-- INDEX
create index idx_penjualan_kumbung		on penjualan	(kumbung_id);
create index idx_penjualan_tanggal		on penjualan	(tanggal_penjualan);
create index idx_rekomendasi_prediksi	on rekomendasi	(prediksi_id);
create index idx_bahanbaku_rekomendasi	on bahan_baku	(rekomendasi_id);
create index idx_bahanbaku_jenis		on bahan_baku	(jenis_bahan_baku_id);
create index idx_produksi_rekomendasi	on produksi		(rekomendasi_id);
create index idx_produksi_tanggal		on produksi		(tanggal_produksi);