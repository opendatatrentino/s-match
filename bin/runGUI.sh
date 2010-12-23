#!/bin/bash
export CP=../build/s-match.jar:../build/s-match-gui.jar:../lib/jwnl/commons-logging.jar:../lib/jwnl/jwnl.jar:../lib/jwnl/tests.jar:../lib/jwnl/utilities.jar:../lib/orbital/orbital-core.jar:../lib/orbital/orbital-ext.jar:../lib/sat/minilearning/minilearningbr.jar:../lib/sat/SAT4J/org.sat4j.core.jar:../lib/xml/xercesImpl.jar:../lib/log4j/log4j-1.2.16.jar:../lib/jgraph/jgrapht-0.6.0.jar:../lib/hermit/HermiT.jar:../lib/owlapi/owlapi-bin.jar:../lib/skosapi/skosapi.jar:../lib/gui/ICOReader-1.04.jar:../lib/jgoodies/jgoodies-common-1.0.0.jar:../lib/jgoodies/jgoodies-forms-1.4.0b1.jar:../lib/jic/java-icon.jar:../lib/salamander/svgSalamander.jar

java -Xmx256M -Xms256M -Dlog4j.configuration=../conf/log4j.properties -cp $CP it.unitn.disi.smatch.gui.SMatchGUI
