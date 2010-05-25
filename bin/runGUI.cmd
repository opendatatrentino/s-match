@echo off
setlocal
set CP=..\build\s-match.jar;..\lib\JWNL\commons-logging.jar;..\lib\JWNL\jwnl.jar;..\lib\JWNL\tests.jar;..\lib\JWNL\utilities.jar;..\lib\orbital\orbital-core.jar;..\lib\orbital\orbital-ext.jar;..\lib\sat\minilearningbr\minilearningbr.jar;..\lib\sat\SAT4J\org.sat4j.core.jar;..\lib\xml\xercesImpl.jar;..\lib\log4j\log4j-1.2.16.jar;..\lib\gui\ICOReader-1.04.jar

start javaw -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.gui.MatchingBasicGUI

endlocal