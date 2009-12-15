#!/bin/bash
export CP=../classes:../build/smatch.jar:../lib/JWNL/commons-logging.jar:../lib/JWNL/jwnl.jar:../lib/JWNL/tests.jar:../lib/JWNL/utilities.jar:../lib/orbital/orbital-core.jar:../lib/orbital/orbital-ext.jar:../lib/sat/jsat.jar:../lib/sat/minilearningbr.jar:../lib/sat/sat4j-1.5new.jar:../lib/xml/xercesImpl.jar:../lib/xml/xmlParserAPIs.jar:../lib/log4j/log4j-1.2.14.jar

. ./convert-cwTab2XML.sh
. ./offline-cw.sh SMatchDefaultL
. ./online-cw.sh SMatchDefaultL
. ./online-cw.sh SMatchDefaultMinimalL
