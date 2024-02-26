#!/bin/bash

echo "Compilation en cours..."

/usr/bin/javac -version

# ChaCuN/scripts/run_tests
ROOT_DIR=../..

# Définissez le chemin vers votre répertoire Maven local
MAVEN_REPOSITORY=./lib

# Lire le fichier XML et extraire les URLs des JARs
JAR_PATHS=$(grep -oP 'jar://\$MAVEN_REPOSITORY\$\K[^!]*' "${ROOT_DIR}/.idea/libraries/junit_jupiter.xml")

# Construire le chemin de classe en remplaçant $MAVEN_REPOSITORY$ par le chemin réel
CLASSPATH=""
for PATH in $JAR_PATHS; do
    JAR="${MAVEN_REPOSITORY}/${PATH}"
    CLASSPATH="${CLASSPATH}:${JAR}"
done

# Supprimer le premier caractère ':' du CLASSPATH
CLASSPATH=${CLASSPATH:1}

/usr/bin/find "${ROOT_DIR}/src/ch/epfl/chacun" -name "*.java" > src_files.txt
/usr/bin/find "${ROOT_DIR}/test/ch/epfl/chacun" -name "*.java" > test_files.txt

/usr/bin/rm -rf ./out

# build production class
/usr/bin/javac -d out/production/classes @src_files.txt
# build test class
/usr/bin/javac -d out/test/classes -classpath out/production/classes:"$CLASSPATH" @test_files.txt
/usr/bin/java -jar junit-platform-console-standalone-1.10.2.jar execute -cp out/production/classes:out/test/classes --select-package ch.epfl.chacun

echo "Compilation terminée."
