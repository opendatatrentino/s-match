call match-manager.cmd convert ..\test-data\cw\c.txt ..\test-data\cw\c.xml -config=..\conf\s-match-Tab2XML.properties
call match-manager.cmd convert ..\test-data\cw\w.txt ..\test-data\cw\w.xml -config=..\conf\s-match-Tab2XML.properties

call match-manager.cmd offline ..\test-data\cw\c.xml ..\test-data\cw\c.xml
call match-manager.cmd offline ..\test-data\cw\w.xml ..\test-data\cw\w.xml

call match-manager.cmd online ..\test-data\cw\c.xml ..\test-data\cw\w.xml ..\test-data\cw\result-cw.txt
call match-manager.cmd online ..\test-data\cw\c.xml ..\test-data\cw\w.xml ..\test-data\cw\result-minimal-cw.txt -config=..\conf\s-match-minimal.properties
