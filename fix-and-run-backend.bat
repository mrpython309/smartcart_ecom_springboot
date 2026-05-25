@echo off
setlocal
echo ========================================================
echo       SmartCart Backend Robust Startup and Repair
echo ========================================================
echo.

set WRAPPER_DIR=smartcart-backend\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set JAR_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

if not exist "smartcart-backend" (
    echo [ERROR] Could not find the 'smartcart-backend' folder.
    echo [INFO] Please make sure you are running this script from: c:\Users\shaik\Desktop\Smartcart
    pause
    exit /b 1
)

:: 1. Ensure directory exists
if not exist "%WRAPPER_DIR%" (
    echo [INFO] Creating wrapper directory...
    mkdir "%WRAPPER_DIR%"
)

:: 2. Download JAR if missing
if not exist "%WRAPPER_JAR%" (
    echo [INFO] Maven Wrapper JAR is missing. Attempting to download...
    
    :: Try certutil first (Native Windows)
    echo [INFO] Trying certutil...
    certutil -urlcache -f "%JAR_URL%" "%WRAPPER_JAR%" >nul 2>&1
    
    :: If certutil fails, try powershell
    if not exist "%WRAPPER_JAR%" (
        echo [INFO] certutil failed. Trying PowerShell...
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%JAR_URL%', '%WRAPPER_JAR%')" >nul 2>&1
    )
)

:: 3. Check if we have Maven on the PATH as a backup
mvn -version >nul 2>&1
set HAS_MVN=%ERRORLEVEL%

if not exist "%WRAPPER_JAR%" (
    if %HAS_MVN% NEQ 0 (
        echo [ERROR] Could not download Maven Wrapper and 'mvn' is not installed on your system.
        echo [INFO] Please install Maven or check your internet connection.
        pause
        exit /b 1
    ) else (
        echo [INFO] Maven Wrapper failed but local 'mvn' was found. Using local Maven...
    )
) else (
    echo [SUCCESS] Maven Wrapper is ready.
)
echo.

:: 4. Run the Backend
echo [INFO] Starting SmartCart Backend...
echo --------------------------------------------------------
cd smartcart-backend
if exist ".mvn\wrapper\maven-wrapper.jar" (
    call mvnw.cmd spring-boot:run
) else (
    call mvn spring-boot:run
)

echo.
pause
