#!/bin/sh

. java-env.sh

# Mysical Magic Java Flags
MAGIC_JAVA_EXECUTION_FLAGS="-Xmx256M"

# Compile
javac -classpath ${CLASS_PATHS}:${EXTRA_PATHS} com/nexopia/adblaster/*.java

# Execute
java ${MAGIC_JAVA_EXECUTION_FLAGS} -classpath ${CLASS_PATHS}:${EXTRA_PATHS} com/nexopia/adblaster/PotentialChecker 2

