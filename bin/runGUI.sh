#!/bin/bash

java -Xmx256M -Xms256M -Dlog4j.configuration=../conf/log4j.properties -cp $CP it.unitn.disi.smatch.gui.MatchingBasicGUI