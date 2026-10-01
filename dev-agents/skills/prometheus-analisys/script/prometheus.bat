@echo off
:: Configuración de nombres de archivo
SET FINAL_REPORT=reporte_actual.txt
SET BACKUP_1=reporte_anterior_1.txt
SET BACKUP_2=reporte_anterior_2.txt
SET URL=http://localhost:8080/actuator/prometheus

echo [+] Rotando el historial de reportes (guardando los 2 ultimos)...

:: Si existe el respaldo 1, lo mueve al respaldo 2 (borrando el viejo 2)
if exist %BACKUP_1% (
    if exist %BACKUP_2% del %BACKUP_2%
    ren %BACKUP_1% %BACKUP_2%
)

:: Si existe el reporte actual, lo mueve al respaldo 1
if exist %FINAL_REPORT% (
    ren %FINAL_REPORT% %BACKUP_1%
)

:: Generar el nuevo reporte limpio
echo =================================================== > %FINAL_REPORT%
echo        REPORTE DE MÉTRICAS WEBSOCKET & BACKEND       >> %FINAL_REPORT%
echo        Fecha: %DATE%  Hora: %TIME%               >> %FINAL_REPORT%
echo =================================================== >> %FINAL_REPORT%
echo. >> %FINAL_REPORT%

echo [+] Extrayendo timers de duración de mensajes...
echo [TIMERS DE MENSAJE] >> %FINAL_REPORT%
curl -s %URL% | findstr "ws_message_duration" >> %FINAL_REPORT%
echo. >> %FINAL_REPORT%

echo [+] Extrayendo timers por fases de procesamiento...
echo [TIMERS POR FASE] >> %FINAL_REPORT%
curl -s %URL% | findstr "ws_message_phase_duration" >> %FINAL_REPORT%
echo. >> %FINAL_REPORT%

echo [+] Extrayendo métricas de la caché de audio...
echo [CACHÉ DE AUDIO] >> %FINAL_REPORT%
curl -s %URL% | findstr "ws_audio_cache" >> %FINAL_REPORT%
echo. >> %FINAL_REPORT%

echo [+] Extrayendo métricas de síntesis de voz (TTS)...
echo [SÍNTESIS TTS] >> %FINAL_REPORT%
curl -s %URL% | findstr "ws_tts_synthesis" >> %FINAL_REPORT%
echo. >> %FINAL_REPORT%

echo [+] Extrayendo estados globales (Gauges)...
echo [ESTADOS GLOBALES - GAUGES] >> %FINAL_REPORT%
curl -s %URL% | findstr "ws_sessions_open ws_games_active ws_games_locks ws_world_states" >> %FINAL_REPORT%
echo. >> %FINAL_REPORT%

echo [+] Extrayendo sentencias SQL por mensaje...
echo [MÉTRICAS SQL] >> %FINAL_REPORT%
curl -s %URL% | findstr "ws_sql_statements" >> %FINAL_REPORT%
echo. >> %FINAL_REPORT%

echo =================================================== >> %FINAL_REPORT%
echo FIN DEL REPORTE >> %FINAL_REPORT%

echo.
echo [OK] Extracción completada con éxito.
echo Reporte actual:   %cd%\%FINAL_REPORT%
if exist %BACKUP_1% echo Historial 1 (previo): %cd%\%BACKUP_1%
if exist %BACKUP_2% echo Historial 2 (antiguo): %cd%\%BACKUP_2%
echo.
pause
