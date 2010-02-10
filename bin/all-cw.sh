#!/bin/bash
export CP=../build/s-match.jar:../lib/jwnl/commons-logging.jar:../lib/jwnl/jwnl.jar:../lib/jwnl/tests.jar:../lib/jwnl/utilities.jar:../lib/orbital/orbital-core.jar:../lib/orbital/orbital-ext.jar:../lib/sat/jsat.jar:../lib/sat/minilearningbr.jar:../lib/sat/sat4j-1.5new.jar:../lib/xml/xercesImpl.jar:../lib/xml/xmlParserAPIs.jar:../lib/log4j/log4j-1.2.14.jar

. ./convert-cwTab2XML.sh
. ./offline-cw.sh SMatchDefault
. ./online-cw.sh SMatchDefault
. ./online-cw.sh SMatchDefaultMinimal
