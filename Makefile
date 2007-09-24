.SUFFIXES: .java .class
JARS=jdbc-mysql.jar:xpp3_min-1.1.3.4.O.jar:xstream-1.2.jar
JAVA_FLAGS=-Xmx256M
JAVAC=javac
JAVAC_FLAGS=
JAVA_CLASSPATH=.:$(JARS)
JAVA_DEPENDENCIES=${wildcard com/nexopia/adblaster/*.java}
REVISION=${shell svn info | grep 'Revision' | sed -e 's/[^0-9]//g'}

.java.class:
	${JAVAC} ${JAVAC_FLAGS} -classpath ${JAVA_CLASSPATH} $<

all: ${JAVA_DEPENDENCIES:.java=.class}
	echo '#!/bin/sh' >nioserver.sh
	echo 'java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/NIOServer' >>nioserver.sh
	chmod +x nioserver.sh

clean:
	find . -name '*.class' -print0 | xargs --null rm -f
	find . -type d -wholename './adblaster-nioserver-r*' -print0 | xargs --null rm -rf
	rm -rf jar.build
	rm -f nioserver.sh

distclean: realclean

jar: rebuild
	mkdir jar.build
	echo 'Manifest-Version: 1.0' > jar.build/manifest
	echo 'Class-Path: ' ${subst :, ,${JARS}} >> jar.build/manifest
	echo "Created-By: `whoami`@`hostname --fqdn`" >> jar.build/manifest
	echo 'Main-Class: com.nexopia.adblaster.NIOServer' >> jar.build/manifest
	echo 'Package-Title: AdBlaster' >> jar.build/manifest
	echo 'Package-Version: r${REVISION}' >> jar.build/manifest
	find . -name '*.class' -print0 | xargs --null --replace cp --parents '{}' jar.build
	cd jar.build ;	jar cvfm ../adblaster-nioserver-r${REVISION}.jar manifest `find . -name '*.class'`
	rm -rf jar.build

package: jar
	mkdir adblaster-nioserver-r${REVISION}
	cp adblaster-nioserver-r${REVISION}.jar ${subst :, ,${JARS}} adblaster-nioserver-r${REVISION}
	tar cfv adblaster-nioserver-r${REVISION}.tar adblaster-nioserver-r${REVISION}
	rm -rf adblaster-nioserver-r${REVISION}

potentialcheck: all
	java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/PotentialChecker 2

pristine: realclean

proper: realclean

realclean: clean
	rm -f adblaster-nioserver-r*.jar
	rm -f adblaster-nioserver-r*.tar

rebuild: realclean all

tar: package
