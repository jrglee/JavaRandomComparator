#!/bin/sh

JAR_FILE=target/RandomComparisson-1.0.jar

if [ ! -f $JAR_FILE ]; then
 echo "Compiling..."
 mvn package
 echo "Compilation done"
fi

echo
echo "Executing..."
echo

$JAVA_HOME/bin/java -jar $JAR_FILE