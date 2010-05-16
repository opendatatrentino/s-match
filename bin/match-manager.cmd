@echo off
setlocal
set CP=..\build\s-match.jar;..\lib\JWNL\commons-logging.jar;..\lib\JWNL\jwnl.jar;..\lib\JWNL\tests.jar;..\lib\JWNL\utilities.jar;..\lib\orbital\orbital-core.jar;..\lib\orbital\orbital-ext.jar;..\lib\sat\OpenSAT\colt.jar;..\lib\sat\OpenSAT\commons-cli-1.0.jar;..\lib\sat\OpenSAT\opensat.jar;..\lib\sat\SAT4J\org.sat4j.core.jar;..\lib\xml\xercesImpl.jar;..\lib\xml\xmlParserAPIs.jar;..\lib\log4j\log4j-1.2.16.jar

java -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.MatchManager %*

endlocal