@echo off
echo Starting all services...

REM Start Store Service (8081)
start "Store Service" cmd /c "cd services/store && mvnw spring-boot:run"

REM Wait 5 seconds
timeout /t 5

REM Start Exchange Service (8082)
start "Exchange Service" cmd /c "cd services/exchange && mvnw spring-boot:run"

REM Wait 5 seconds
timeout /t 5

REM Start Fidelity Service (8083)
start "Fidelity Service" cmd /c "cd services/fidelity && mvnw spring-boot:run"

REM Wait 5 seconds
timeout /t 5

REM Start E-commerce Service (8080)
start "E-commerce Service" cmd /c "cd services/ecommerce && mvnw spring-boot:run"

echo All services started!
echo Store Service: http://localhost:8081
echo Exchange Service: http://localhost:8082
echo Fidelity Service: http://localhost:8083
echo E-commerce Service: http://localhost:8080
