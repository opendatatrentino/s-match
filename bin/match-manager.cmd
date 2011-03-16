@echo off
REM Use S-MATCH-JVM to add your options to JVM (such as memory)
REM Use S-MATCH-CP to add your jar to classpath
setlocal
set CP=..\build\s-match.jar;..\lib\extjwnl\commons-logging.jar;..\lib\extjwnl\extjwnl-20110117.jar;..\lib\orbital\orbital-core.jar;..\lib\orbital\orbital-ext.jar;..\lib\sat\minilearning\minilearningbr.jar;..\lib\sat\SAT4J\org.sat4j.core.jar;..\lib\xml\xercesImpl.jar;..\lib\log4j\log4j-1.2.16.jar;..\lib\jgraph\jgrapht-0.6.0.jar;..\lib\hermit\HermiT.jar;..\lib\owlapi\owlapi-bin.jar;..\lib\skosapi\skosapi.jar;%S-MATCH-CP%

if x%S-MATCH-JVM% EQU x (
  java -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.MatchManager %*
) else (
  java %S-MATCH-JVM% -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% it.unitn.disi.smatch.MatchManager %*
)

endlocal