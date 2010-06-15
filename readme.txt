1. About S-Match

S-Match is a semantic matching algorithm developed at the University of Trento
under the direction of Fausto Giunchiglia.

To learn more about S-Match visit: http://s-match.org/

2. Current Release

This release is a research software, intended mainly for use by researchers to
conduct experiments.

3. Disclaimer

This release contains not very robust and not optimized code. Its main use was
and is to conduct experiments. It does contain code written as fast as possible
with sometimes little thinking about memory consumption and processing speed.

4. Getting started

S-Match is written in Java and runs in a Java Virtual Machine version 6 or later.

4.1 Command line

Run the matching of provided sample trees by running bin/all-cw.cmd (Windows) or 
all-cw.sh (Linux). This will execute the following steps:
  a) convert sample trees in test-data/cw/c.txt and test-data/cw/w.txt
     from text files to internal XML format.
  b) enrich the trees with logical formulas ("offline" steps of the algorithm)
  c) match the trees ("online" steps of the algorithm)
  d) match the trees with the minimal matching algorithm
        
The script will render the results of the
step c) into: test-data/cw/result-cw.txt 
step d) into: test-data/cw/result-minimal-cw.txt

4.2 GUI

Run bin\runGUI.cmd or bin/runGUI.sh

4.3 S-Match Wiki

Access S-Match Wiki for more information:
http://sourceforge.net/apps/trac/s-match/wiki/

5. Documentation

For S-Match documentation visit: http://s-match.org/documentation.html
