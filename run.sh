#!/usr/bin/env sh
set -eu

mkdir -p build/classes build/test-classes
find src/main/java -name '*.java' -print > build/main-sources.txt
javac -d build/classes @build/main-sources.txt

case "${1:-preview}" in
  preview) java -cp build/classes education.logistics.digest.LogisticsDigestApplication ;;
  register) java -cp build/classes education.logistics.digest.LogisticsDigestApplication register ;;
  test)
    find src/test/java -name '*.java' -print > build/test-sources.txt
    javac -cp build/classes -d build/test-classes @build/test-sources.txt
    java -cp build/classes:build/test-classes education.logistics.digest.service.LogisticsDigestServiceTest
    ;;
  *) echo "Usage: ./run.sh [preview|register|test]" >&2; exit 2 ;;
esac
