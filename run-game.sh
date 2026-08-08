#!/bin/zsh

set -e

cd "${0:A:h}"

mkdir -p out
javac -d out $(find src -name "*.java")
exec java -cp out:src com.r3m.spaceshooter.application.Main
