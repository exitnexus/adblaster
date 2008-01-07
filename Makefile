.SUFFIXES: .java .class
JAR_DEPENDENCIES=jdbc-mysql.jar:xpp3_min-1.1.3.4.O.jar:xstream-1.2.jar
JAVA_FLAGS=-Xmx256M
JAVA_CLASSPATH=.:$(JAR_DEPENDENCIES)
JAVA_DEPENDENCIES=${wildcard com/nexopia/adblaster/*.java}
JAVAC=javac
JAVAC_FLAGS=
REVISION=${shell svn info | grep 'Revision' | sed -e 's/[^0-9]//g'}

.java.class:
	${JAVAC} ${JAVAC_FLAGS} -classpath ${JAVA_CLASSPATH} $<

all: ${JAVA_DEPENDENCIES:.java=.class}
	echo '#!/bin/sh' >nioserver.sh
	echo 'java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/NIOServer' >>nioserver.sh
	chmod +x nioserver.sh
	echo '#!/bin/sh' >logserver.sh
	echo 'java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/LogServer' >>logserver.sh
	chmod +x logserver.sh
	echo '#!/bin/sh' >adblaster.sh
	echo 'java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/AdBlaster' >>adblaster.sh
	chmod +x adblaster.sh

clean:
	find . -name '*.class' -print0 | xargs --null rm -f
	find . -type d -wholename './adblaster-nioserver-r*' -print0 | xargs --null rm -rf
	find . -type d -wholename './adblaster-logserver-r*' -print0 | xargs --null rm -rf
	find . -type d -wholename './adblaster-r*' -print0 | xargs --null rm -rf
	rm -rf *.jar.build
	rm -f adblaster.sh logserver.sh nioserver.sh

distclean: realclean

jar: jar-adblaster jar-logserver jar-nioserver

jar-adblaster: all
	mkdir $@.jar.build
	echo 'Manifest-Version: 1.0' > $@.jar.build/manifest
	echo 'Class-Path: ' ${subst :, ,${JAR_DEPENDENCIES}} >> $@.jar.build/manifest
	echo "Created-By: `whoami`@`hostname --fqdn`" >> $@.jar.build/manifest
	echo 'Main-Class: com.nexopia.adblaster.AdBlaster' >> $@.jar.build/manifest
	echo 'Package-Title: AdBlaster' >> $@.jar.build/manifest
	echo 'Package-Version: r${REVISION}' >> $@.jar.build/manifest
	find . -name '*.class' -print0 | xargs --null --replace cp --parents '{}' $@.jar.build
	cd $@.jar.build ;	jar cvfm ../adblaster-r${REVISION}.jar manifest `find . -name '*.class'`
	rm -rf $@.jar.build

jar-logserver: all
	mkdir $@.jar.build
	echo 'Manifest-Version: 1.0' > $@.jar.build/manifest
	echo 'Class-Path: ' ${subst :, ,${JAR_DEPENDENCIES}} >> $@.jar.build/manifest
	echo "Created-By: `whoami`@`hostname --fqdn`" >> $@.jar.build/manifest
	echo 'Main-Class: com.nexopia.adblaster.LogServer' >> $@.jar.build/manifest
	echo 'Package-Title: LogServer' >> $@.jar.build/manifest
	echo 'Package-Version: r${REVISION}' >> $@.jar.build/manifest
	find . -name '*.class' -print0 | xargs --null --replace cp --parents '{}' $@.jar.build
	cd $@.jar.build ;	jar cvfm ../adblaster-logserver-r${REVISION}.jar manifest `find . -name '*.class'`
	rm -rf $@.jar.build

jar-nioserver: all
	mkdir $@.jar.build
	echo 'Manifest-Version: 1.0' > $@.jar.build/manifest
	echo 'Class-Path: ' ${subst :, ,${JAR_DEPENDENCIES}} >> $@.jar.build/manifest
	echo "Created-By: `whoami`@`hostname --fqdn`" >> $@.jar.build/manifest
	echo 'Main-Class: com.nexopia.adblaster.NIOServer' >> $@.jar.build/manifest
	echo 'Package-Title: NIOServer' >> $@.jar.build/manifest
	echo 'Package-Version: r${REVISION}' >> $@.jar.build/manifest
	find . -name '*.class' -print0 | xargs --null --replace cp --parents '{}' $@.jar.build
	cd $@.jar.build ;	jar cvfm ../adblaster-nioserver-r${REVISION}.jar manifest `find . -name '*.class'`
	rm -rf $@.jar.build

package: package-adblaster package-logserver package-nioserver

package-adblaster: jar-adblaster
	mkdir adblaster-r${REVISION}
	cp adblaster-r${REVISION}.jar ${subst :, ,${JAR_DEPENDENCIES}} adblaster-r${REVISION}
	tar cfv adblaster-r${REVISION}.tar adblaster-r${REVISION}
	rm -rf adblaster-r${REVISION}

package-logserver: jar-logserver
	mkdir adblaster-logserver-r${REVISION}
	cp adblaster-logserver-r${REVISION}.jar ${subst :, ,${JAR_DEPENDENCIES}} adblaster-logserver-r${REVISION}
	tar cfv adblaster-logserver-r${REVISION}.tar adblaster-logserver-r${REVISION}
	rm -rf adblaster-logserver-r${REVISION}

package-nioserver: jar-nioserver
	mkdir adblaster-nioserver-r${REVISION}
	cp adblaster-nioserver-r${REVISION}.jar ${subst :, ,${JAR_DEPENDENCIES}} adblaster-nioserver-r${REVISION}
	tar cfv adblaster-nioserver-r${REVISION}.tar adblaster-nioserver-r${REVISION}
	rm -rf adblaster-nioserver-r${REVISION}

potentialcheck: all
	java ${JAVA_FLAGS} -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/PotentialChecker 2

pristine: realclean

proper: realclean

realclean: clean
	rm -f adblaster-nioserver-r*.[jt]ar
	rm -f adblaster-logserver-r*.[jt]ar
	rm -f adblaster-r*.[jt]ar

rebuild: realclean all

tar: package
