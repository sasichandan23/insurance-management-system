@echo off
REM ============================================
REM  Insurance Management System - Backend
REM  Starts the Spring Boot API on port 8081
REM ============================================
set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.1"
set "PATH=C:\Windows\System32;C:\Windows\System32\WindowsPowerShell\v1.0;%PATH%"
cd /d "C:\Users\HomePC\Desktop\insurance-management-system\backend"
call .\mvnw.cmd spring-boot:run
