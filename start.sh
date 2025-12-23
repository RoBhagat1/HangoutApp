#!/bin/sh
exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar
