#!/bin/bash
./match-manager.sh offline ../test-data/cw/c.xml ../test-data/cw/c.xml -prop=../conf/$1.properties
./match-manager.sh offline ../test-data/cw/w.xml ../test-data/cw/w.xml -prop=../conf/$1.properties

