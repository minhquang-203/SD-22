UPDATE anh_san_pham SET url = REPLACE(url, 'https://cdn.sunova.vn/', '/uploads/products/') WHERE url LIKE 'https://cdn.sunova.vn/%';
