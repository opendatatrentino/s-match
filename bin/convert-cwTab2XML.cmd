@echo off
setlocal
call setCP.cmd

java -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.MatchManager convert ..\test-data\cw\c.txt ..\test-data\cw\c.xml -prop=..\conf\SMatchTab2XML.properties
java -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.MatchManager convert ..\test-data\cw\w.txt ..\test-data\cw\w.xml -prop=..\conf\SMatchTab2XML.properties

endlocal