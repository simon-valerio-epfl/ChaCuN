#!/bin/bash

echo "Compilation en cours..."

javac -version

# ChaCuN/scripts/run_tests
ROOT_DIR=../..

find "${ROOT_DIR}/src/ch/epfl/chacun" -name "*.java" > src_files.txt
find "${ROOT_DIR}/test/ch/epfl/chacun" -name "*.java" > test_files.txt

rm -rf ./out

# Assurez-vous que cette variable pointe vers le dossier lib du SDK JavaFX téléchargé
JAVAFX_LIB_PATH="${JAVAFX_SDK_PATH}/lib"

# Compilation des classes de production avec JavaFX
javac --enable-preview \
      --module-path "${JAVAFX_LIB_PATH}" \
      --add-modules javafx.controls,javafx.fxml \
      -d out/production/classes @src_files.txt

# Compilation des classes de test
javac --enable-preview \
      --module-path "${JAVAFX_LIB_PATH}" \
      --add-modules javafx.controls,javafx.fxml \
      -d out/test/classes -classpath out/production/classes:junit-platform-console-standalone-1.10.2.jar @test_files.txt

# Exécution des tests
CSV_FILE_PATH="${ROOT_DIR}/test/ch/epfl/chacun/tuiles.csv" java --enable-preview \
      --module-path "${JAVAFX_LIB_PATH}" \
      --add-modules javafx.controls,javafx.fxml \
      -jar junit-platform-console-standalone-1.10.2.jar \
      -cp out/production/classes:out/test/classes: \
      --select-package ch.epfl.chacun \
      --reports-dir reports

grep -q "failures=\"0\"" reports/TEST-junit-jupiter.xml && grep -q "errors=\"0\"" reports/TEST-junit-jupiter.xml || exit 1

echo "Compilation terminée."

exit 0
