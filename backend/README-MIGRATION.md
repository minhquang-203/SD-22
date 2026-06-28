# Quy tắc migration database (Flyway) — SUNOVA

## Cách hoạt động

- Khi chạy Spring Boot, **Flyway** tự áp các file SQL trong `src/main/resources/db/migration/`.
- Hibernate vẫn dùng `spring.jpa.hibernate.ddl-auto=validate` — **không** tự sửa schema.
- Flyway **không tạo database**. Tạo database `SUNOVA` rỗng **một lần** trên SQL Server trước khi chạy app.

```sql
CREATE DATABASE SUNOVA;
```

## Máy mới (DB rỗng)

1. Tạo database `SUNOVA` (rỗng).
2. `pull` code + chạy app.
3. Flyway chạy `V1__init.sql` → dựng full schema + seed.
4. Bảng `flyway_schema_history` xuất hiện trong DB.

## Máy đã có schema sẵn (team đang dev)

1. `pull` code + chạy app.
2. Flyway **baseline** tại version 1 (`baseline-on-migrate=true`) → **không** chạy lại `V1__init.sql`, **không** mất dữ liệu.
3. Chỉ các file **V2+** (mới hơn) mới được áp.

## Quy tắc khi đổi schema

1. **Mỗi lần đổi schema = tạo file migration MỚI.**  
   Tuyệt đối **không sửa** file migration đã commit (Flyway kiểm checksum → lỗi khi start app).

2. **Đặt tên theo mốc thời gian** để tránh trùng version giữa 4 người:

   ```
   V20260628_1430__them_bang_x.sql
   ```

   - `V` + `yyyyMMdd_HHmm` + `__` + mô tả ngắn (snake_case, tiếng Việt không dấu).

3. **Seed / dữ liệu mẫu** cần chia sẻ cho cả team cũng để trong migration (file mới), không chạy tay riêng lẻ.

4. Mỗi file SQL Server dùng `GO` giữa các batch như script hiện tại — Flyway 10 + `flyway-sqlserver` hỗ trợ sẵn.

## Ví dụ thêm cột

Tạo file `src/main/resources/db/migration/V20260629_0900__them_cot_noi_bat.sql`:

```sql
IF COL_LENGTH('san_pham', 'noi_bat') IS NULL
BEGIN
    ALTER TABLE san_pham ADD noi_bat BIT NOT NULL CONSTRAINT DF_san_pham_noi_bat DEFAULT 0;
END
GO
```

Commit + push → đồng đội chỉ cần `pull` và chạy lại app.

## File hiện có

| File | Mô tả |
|------|--------|
| `V1__init.sql` | Schema đầy đủ + seed ban đầu (đã baseline cho DB cũ) |
