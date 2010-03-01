@echo off
setlocal
call setCP.cmd
set CP=%CP%;..\lib\gui\ICOReader-1.04.jar

start javaw -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.gui.MatchingBasicGUI

endlocal