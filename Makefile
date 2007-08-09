.SUFFIXES: .java .class
JARS=jdbc-mysql.jar:xpp3_min-1.1.3.4.0.jar:xstream-1.2.jar
JAVAC=javac
JFLAGS=
JAVA_CLASSPATH=.:$(JARS)
JAVA_DEPENDENCIES=${wildcard com/nexopia/adblaster/*.java}
REVISION=${strip ${subst Rev,,${subst :,,${subst $$,,$$Rev$$}}}}

.java.class:
	${JAVAC} ${JFLAGS} -classpath ${JAVA_CLASSPATH} $<

all: ${JAVA_DEPENDENCIES:.java=.class}
	echo '#!/bin/sh' >run.sh
	echo 'java -Xmx1024M -classpath ${JAVA_CLASSPATH} com/nexopia/adblaster/NIOServer' >>run.sh
	chmod +x run.sh

clean:
	find . -name '*.class' -print0 | xargs --null rm -f
	rm -rf jar.build
	rm -f run.sh

jar: rebuild
	mkdir jar.build
	find . -name '*.class' -print0 | xargs --null --replace cp --parents '{}' jar.build
	for JAR_FILE in ${subst :, ,${JARS}}; do \
		cd jar.build ; jar xvf ../$${JAR_FILE} ; \
	done
	cd jar.build ;	jar cvf ../adblaster-r${REVISION}.jar `find . -name '*.class'`
	rm -rf jar.build

rebuild: clean all
