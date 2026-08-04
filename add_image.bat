@echo off
chcp 65001 >nul
echo Đang sao chép ảnh sản phẩm...
mkdir "backend\uploads\products" 2>nul
copy /Y "C:\Users\nguye\.gemini\antigravity\brain\tempmediaStorage\media_1785733492853.jpg" "backend\uploads\products\sp002.jpg"
if %errorlevel% neq 0 (
    echo Lỗi khi copy file.
) else (
    echo Sao chép thành công! Hình ảnh đã được thêm cho sản phẩm La Roche-Posay Anthelios UVMune 400 (SP002).
)
pause
