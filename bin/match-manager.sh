#!/bin/bash
export CP=../build/s-match.jar:../lib/jwnl/commons-logging.jar:../lib/jwnl/jwnl.jar:../lib/jwnl/tests.jar:../lib/jwnl/utilities.jar:../lib/orbital/orbital-core.jar:../lib/orbital/orbital-ext.jar:../lib/sat/minilearningbr/minilearningbr.jar:../lib/sat/SAT4J/org.sat4j.core.jar:../lib/xml/xercesImpl.jar:../lib/log4j/log4j-1.2.16.jar

java -Xmx256M -Xms256M -Dlog4j.configuration=../conf/log4j.properties -cp $CP it.unitn.disi.smatch.MatchManager $*
