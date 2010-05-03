#!/bin/bash
./match-manager.sh convert ../test-data/cw/c.txt ../test-data/cw/c.xml -prop=../conf/SMatchTab2XML.properties
./match-manager.sh convert ../test-data/cw/w.txt ../test-data/cw/w.xml -prop=../conf/SMatchTab2XML.properties
