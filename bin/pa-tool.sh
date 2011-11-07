#!/bin/bash
#Use PATOOLJVM to add your options to JVM (such as memory)
#Use PATOOLCP to add your jar to classpath
export CP=../build/s-match.jar:../lib/extjwnl/commons-logging-1.1.1.jar:../lib/extjwnl/extjwnl-1.6.2.jar:../lib/orbital/orbital-core.jar:../lib/orbital/orbital-ext.jar:../lib/sat/minilearning/minilearningbr.jar:../lib/sat/SAT4J/org.sat4j.core.jar:../lib/xml/xercesImpl.jar:../lib/log4j/log4j-1.2.16.jar:../lib/jgraph/jgrapht-0.6.0.jar:../lib/hermit/HermiT.jar:../lib/owlapi/owlapi-bin.jar:../lib/skosapi/skosapi.jar:../lib/opennlp/opennlp-maxent-3.0.1-incubating.jar;../lib/opennlp/opennlp-tools-1.5.1-incubating.jar:$PATOOLCP

if [ -n "$PATOOLJVM" ]; then
  java -Dlog4j.configuration=../conf/log4j.properties $PATOOLJVM -cp $CP it.unitn.disi.annotation.POSAnnotationTool $*
else
  java -Xmx256M -Xms256M -Dlog4j.configuration=../conf/log4j.properties -cp $CP it.unitn.disi.annotation.POSAnnotationTool $*
fi