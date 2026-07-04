# Mở tunnel public tới backend local (port 8080) để VNPay callback khi test QR trên điện thoại.
# Yêu cầu: cài ngrok — https://ngrok.com/download
#
# Sau khi chạy, copy URL https (vd. https://abc123.ngrok-free.app) vào:
#   payment.vnpay.return-url=https://abc123.ngrok-free.app/api/payments/vnpay/callback
# trong application-local.properties, rồi restart backend và tạo LẠI mã QR.

$ErrorActionPreference = "Stop"

if (-not (Get-Command ngrok -ErrorAction SilentlyContinue)) {
    Write-Host "Chua cai ngrok. Tai tai: https://ngrok.com/download" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Dang mo tunnel -> http://localhost:8080" -ForegroundColor Cyan
Write-Host "Copy URL https tu bang ngrok, gan vao payment.vnpay.return-url, restart backend." -ForegroundColor Yellow
Write-Host "Tao LAI ma QR tren POS sau khi doi URL." -ForegroundColor Yellow
Write-Host ""

ngrok http 8080
