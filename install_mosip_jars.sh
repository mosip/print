#!/bin/sh
#
mvn install:install-file \
    -Dfile=needed_jars/kernel-websubclient-api-1.2.0-20200827.145351-13.jar \
    -DgroupId=io.mosip.kernel -DartifactId=kernel-websubclient-api \
    -Dversion=1.2.0-SNAPSHOT \
    -Dpackaging=jar

mvn install:install-file \
    -Dfile=needed_jars/kernel-core-1.2.0-20200828.150339-28.jar \
    -DgroupId=io.mosip.kernel \
    -DartifactId=kernel-core \
    -Dversion=1.2.0-SNAPSHOT \
    -Dpackaging=jar
