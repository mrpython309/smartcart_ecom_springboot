@echo off
echo ========================================================
echo       Starting SmartCart Backend and Frontend
echo ========================================================
echo.

echo [INFO] Starting Backend in a new window...
start "SmartCart Backend" cmd /k "cd smartcart-backend && mvnw spring-boot:run"

echo [INFO] Starting Frontend in a new window...
start "SmartCart Frontend" cmd /c "cd smartcart-frontend && npm run dev"

echo.
echo ========================================================
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5173
echo ================= [RUNNING] ============================
echo.
pause
