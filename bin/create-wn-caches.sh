#!/bin/bash
export CP=../build/s-match.jar:../lib/JWNL/commons-logging.jar:../lib/JWNL/jwnl.jar:../lib/JWNL/tests.jar:../lib/JWNL/utilities.jar:../lib/orbital/orbital-core.jar:../lib/orbital/orbital-ext.jar:../lib/sat/jsat.jar:../lib/sat/minilearningbr.jar:../lib/sat/sat4j-1.5new.jar:../lib/xml/xercesImpl.jar:../lib/xml/xmlParserAPIs.jar:../lib/log4j/log4j-1.2.14.jar

java -Xmx256M -Xms256M -Dlog4j.configuration=../conf/log4j.properties -cp $CP it.unitn.disi.smatch.MatchManager wntoflat -prop=../conf/SMatchDefaultL.properties

