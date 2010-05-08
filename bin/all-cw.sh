#!/bin/bash
. ./match-manager.sh convert ../test-data/cw/c.txt ../test-data/cw/c.xml -config=../conf/s-match-Tab2XML.properties
. ./match-manager.sh convert ../test-data/cw/w.txt ../test-data/cw/w.xml -config=../conf/s-match-Tab2XML.properties

. ./match-manager.sh offline ../test-data/cw/c.xml ../test-data/cw/c.xml
. ./match-manager.sh offline ../test-data/cw/w.xml ../test-data/cw/w.xml

. ./match-manager.sh online ../test-data/cw/c.xml ../test-data/cw/w.xml ../test-data/cw/result-cw.txt
. ./match-manager.sh online ../test-data/cw/c.xml ../test-data/cw/w.xml ../test-data/cw/result-minimal-cw.txt -config=../conf/s-match-minimal.properties
