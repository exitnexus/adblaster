.SUFFIXES: .java .class
JARS=jdbc-mysql.jar:xpp3_min-1.1.3.4.O.jar:xstream-1.2.jar
JAVA_FLAGS=-Xmx256M
JAVAC=javac
JAVAC_FLAGS=
JAVA_CLASSPATH=.:$(JARS)
JAVA_DEPENDENCIES=${wildcard com/nexopia/adblaster/*.java}
REVISION=${strip ${subst Rev,,${subst :,,${subst $$,,$$Rev$$}}}}

.java.class:
	${JAVAC} ${JAVAC_FLAGS} -classpath ${JAVA_CLASSPATH} $<

all: ${JAVA_DEPENDENCIES:.java=.class}
	echo '#!/bin/sh' >run.sh
	echo 'java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/NIOServer' >>run.sh
	chmod +x run.sh

clean:
	find . -name '*.class' -print0 | xargs --null rm -f
	rm -rf jar.build
	rm -f run.sh

jar: rebuild
	mkdir jar.build
	echo 'Manifest-Version: 1.0' > jar.build/manifest
	echo 'Class-Path: ' ${subst :, ,${JARS}} >> jar.build/manifest
	echo "Created-By: `whoami`@`hostname --fqdn`" >> jar.build/manifest
	echo 'Main-Class: com.nexopia.adblaster.NIOServer' >> jar.build/manifest
	echo 'Package-Title: AdBlaster' >> jar.build/manifest
	echo 'Package-Version: r${REVISION}' >> jar.build/manifest
	find . -name '*.class' -print0 | xargs --null --replace cp --parents '{}' jar.build
	cd jar.build ;	jar cvfm ../adblaster-r${REVISION}.jar manifest `find . -name '*.class'`
	rm -rf jar.build

package: jar
	tar cfv adblaster-r${REVISION}.tar adblaster-r${REVISION}.jar ${subst :, ,${JARS}}

potentialcheck: all
	java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/PotentialChecker 2

pristine: realclean

proper: realclean

realclean: clean
	rm -f adblaster-r*.jar
	rm -f adblaster-r*.tar

rebuild: realclean all

tar: package
