#!/bin/bash

rm -f ../test-data/result.txt
java -Xmx256M -Xms256M -Dlog4j.configuration=../conf/log4j.properties -cp $CP it.unitn.disi.smatch.MatchManager online ../test-data/cw/c.xml ../test-data/cw/w.xml -prop=../conf/$1.properties
cp -f ../test-data/result.txt ../test-data/cw/result-$1-cw.txt
rm -f ../test-data/result.txt
