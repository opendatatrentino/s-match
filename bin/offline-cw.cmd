@echo off
setlocal
call setCP.cmd

java -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.MatchManager offline ..\test-data\cw\C.xml ..\test-data\cw\W.xml -prop=..\conf\%1.properties

endlocal