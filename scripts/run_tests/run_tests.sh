#!/bin/bash

echo "Compilation en cours..."

javac -version

# ChaCuN/scripts/run_tests
ROOT_DIR=../..

# Définissez le chemin vers votre répertoire Maven local
MAVEN_REPOSITORY=./lib

# Lire le fichier XML et extraire les URLs des JARs
JAR_PATHS=$(grep -oP 'jar://\$MAVEN_REPOSITORY\$\K[^!]*' "${ROOT_DIR}/.idea/libraries/junit_jupiter.xml")

# Construire le chemin de classe en remplaçant $MAVEN_REPOSITORY$ par le chemin réel
CLASSPATH=""
for JARPATH in $JAR_PATHS; do
    JAR="${MAVEN_REPOSITORY}${JARPATH}"
    CLASSPATH="${CLASSPATH}:${JAR}"
done

# Supprimer le premier caractère ':' du CLASSPATH
CLASSPATH=${CLASSPATH:1}

echo $CLASSPATH

find "${ROOT_DIR}/src/ch/epfl/chacun" -name "*.java" > src_files.txt
find "${ROOT_DIR}/test/ch/epfl/chacun" -name "*.java" > test_files.txt

rm -rf ./out

# build production class
javac -d out/production/classes @src_files.txt
# build test class
javac -d out/test/classes -classpath out/production/classes:"$CLASSPATH" @test_files.txt
java -jar junit-platform-console-standalone-1.10.2.jar execute -cp out/production/classes:out/test/classes: --select-package ch.epfl.chacun --reports-dir reports

grep -q "failures=\"0\"" reports/TEST-junit-jupiter.xml || exit 1

echo "Compilation terminée."
