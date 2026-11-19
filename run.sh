#!/usr/bin/env sh
# Build and start MessKhata with only the JDK (no Maven or IDE needed).
set -e
cd "$(dirname "$0")"
rm -rf out
find src/main/java -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm -f sources.txt
exec java -cp out com.messkhata.Main
