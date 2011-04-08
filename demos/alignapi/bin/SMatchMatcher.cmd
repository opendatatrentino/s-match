@echo off
REM AlignAPI demo
REM Edit environment variables to suit your configuration
setlocal

REM start edit here
set S=file:///D:/Aliaksandr/Stage/Datasets/OAEI/2009/directory/directoryDataset2009/1/source.owl
set T=file:///D:/Aliaksandr/Stage/Datasets/OAEI/2009/directory/directoryDataset2009/1/target.owl
set ALIGN_HOME=D:\Aliaksandr\Stage\s-match\AlignAPI\align-4.0
REM end edit here

set ALIGN_CP=%ALIGN_HOME%\lib\align.jar;%ALIGN_HOME%\lib\alignsvc.jar;%ALIGN_HOME%\lib\ontowrap.jar;%ALIGN_HOME%\lib\procalign.jar;%ALIGN_HOME%\lib\getopt\getopt.jar;%ALIGN_HOME%\lib\iddl\iddl.jar;%ALIGN_HOME%\lib\jade\http.jar;%ALIGN_HOME%\lib\jade\iiop.jar;%ALIGN_HOME%\lib\jade\jade.jar;%ALIGN_HOME%\lib\jdbc\mysql-connector-java.jar;%ALIGN_HOME%\lib\jdbc\postgresql-jdbc4.jar;%ALIGN_HOME%\lib\jena\arq.jar;%ALIGN_HOME%\lib\jena\icu4j.jar;%ALIGN_HOME%\lib\jena\iri.jar;%ALIGN_HOME%\lib\jena\jena.jar;%ALIGN_HOME%\lib\jetty\jetty-util.jar;%ALIGN_HOME%\lib\jetty\jetty.jar;%ALIGN_HOME%\lib\jwnl\jwnl.jar;%ALIGN_HOME%\lib\log4j\commons-logging-1.1.1.jar;%ALIGN_HOME%\lib\log4j\log4j.jar;%ALIGN_HOME%\lib\log4j\slf4j-api.jar;%ALIGN_HOME%\lib\log4j\slf4j-log4j.jar;%ALIGN_HOME%\lib\ontosim\ontosim.jar;%ALIGN_HOME%\lib\owlapi10\api.jar;%ALIGN_HOME%\lib\owlapi10\impl.jar;%ALIGN_HOME%\lib\owlapi10\io.jar;%ALIGN_HOME%\lib\owlapi10\rdfapi.jar;%ALIGN_HOME%\lib\owlapi10\rdfparser.jar;%ALIGN_HOME%\lib\owlapi30\owlapi-bin.jar;%ALIGN_HOME%\lib\oyster\oyster2.jar;%ALIGN_HOME%\lib\pellet\aterm-java-1.6.jar;%ALIGN_HOME%\lib\pellet\pellet-core.jar;%ALIGN_HOME%\lib\pellet\pellet-jena.jar;%ALIGN_HOME%\lib\pellet\pellet-owlapiv3.jar;%ALIGN_HOME%\lib\servlet\servlet-api.jar;%ALIGN_HOME%\lib\xerces\resolver.jar;%ALIGN_HOME%\lib\xerces\xercesImpl.jar;%ALIGN_HOME%\lib\xsdlib\relaxngDatatype.jar;%ALIGN_HOME%\lib\xsdlib\xsdlib.jar;
set SMATCH_CP=..\build\s-match.jar;..\lib\JWNL\commons-logging-1.1.1.jar;..\lib\JWNL\jwnl.jar;..\lib\JWNL\tests.jar;..\lib\JWNL\utilities.jar;..\lib\orbital\orbital-core.jar;..\lib\orbital\orbital-ext.jar;..\lib\sat\minilearning\minilearningbr.jar;..\lib\sat\SAT4J\org.sat4j.core.jar;..\lib\xml\xercesImpl.jar;..\lib\log4j\log4j-1.2.16.jar
set CP=..\demos\alignapi\build\s-match-align-api.jar;%ALIGN_CP%;%SMATCH_CP%

REM run from S-Match bin to reuse std S-Match config and environment
pushd ..\..\..\bin
java -Xmx256M -Xms256M -Dlog4j.configuration=..\conf\log4j.properties -cp %CP% fr.inrialpes.exmo.align.util.Procalign -i it.unitn.disi.smatch.alignapi.SMatchMatcher -p ..\demos\alignapi\conf\SMatchMatcher.properties.xml %S% %T%
popd
endlocal