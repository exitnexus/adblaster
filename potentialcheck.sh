#!/bin/sh

# Base Class Paths
CLASS_PATHS=".:je-3.1.0.jar:xstream-1.2.jar:xpp3_min-1.1.3.4.0.jar"

# Ubuntu has a package for MySQL Connector
EXTRA_PATHS="/home/troy/mysql-connector-java-3.1.13/mysql-connector-java-3.1.13-bin.jar"

# Mysical Magic Java Flags
MAGIC_JAVA_EXECUTION_FLAGS="-Xmx256M"

# Compile
javac -classpath ${CLASS_PATHS}:${EXTRA_PATHS} com/nexopia/adblaster/*.java

# Execute
java ${MAGIC_JAVA_EXECUTION_FLAGS} -classpath ${CLASS_PATHS}:${EXTRA_PATHS} com/nexopia/adblaster/PotentialChecker 2

