@echo off
setlocal
call setCP.cmd

if exist ..\test-data\result.txt del /q ..\test-data\result.txt
java -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.MatchManager online ..\test-data\cw\C.xml ..\test-data\cw\W.xml -prop=..\conf\%1.properties
move /Y ..\test-data\result.txt ..\test-data\cw\result-%1-cw.txt

endlocal