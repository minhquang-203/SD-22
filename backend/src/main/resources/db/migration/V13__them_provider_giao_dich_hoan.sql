-- Luu ma giao dich nha cung cap (vd: vnp_TransactionNo) va ngay thanh toan (vnp_PayDate)
-- de goi API hoan tien sau nay.
ALTER TABLE thanh_toan_hoa_don ADD provider_transaction_no VARCHAR(100) NULL;
ALTER TABLE thanh_toan_hoa_don ADD provider_pay_date VARCHAR(20) NULL;

-- Luu raw response nha cung cap khi hoan tien (doi soat).
ALTER TABLE hoan_tien ADD phan_hoi_ncc NVARCHAR(500) NULL;
