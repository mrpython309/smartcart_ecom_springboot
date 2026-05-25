@echo off
echo ========================================================
echo   SmartCart - Push to GitHub ^& Deploy to Render
echo ========================================================
echo.

echo [1/4] Staging changes...
git add render.yaml
git add smartcart-backend/src/main/resources/application.properties
echo.

echo [2/4] Committing changes...
git commit -m "Add render.yaml blueprint and optimize for Render free tier"
echo.

echo [3/4] Pushing to GitHub (main branch)...
git push origin main
echo.

echo ========================================================
echo   DONE! Now go to Render to complete deployment:
echo.
echo   1. Go to https://dashboard.render.com
echo   2. Click "New +" then "Blueprint"
echo   3. Connect your GitHub repo: mrpython309/smartcart_ecom_springboot
echo   4. Render will auto-detect render.yaml and deploy everything!
echo ========================================================
echo.
pause
