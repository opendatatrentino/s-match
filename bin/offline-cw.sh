#!/bin/bash

java -Xmx256M -Xms256M -Dlog4j.configuration=../conf/log4j.properties -cp $CP it.unitn.disi.smatch.MatchManager offline ../test-data/cw/c.xml ../test-data/cw/w.xml -prop=../conf/$1.properties

