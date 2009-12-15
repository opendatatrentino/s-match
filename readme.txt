
		This is the readme.txt file for S-Match

1. About S-Match

S-Match is a semantic matching algorithm developed at 
the University of Trento under the direction of Fausto Giunchiglia.

To learn more about S-Match, consult papers describing various
aspects of the algorithm and its implementation.

2. Current Release

This release is a research software, intended mainly for use
by researchers to conduct experiments.

3. Running

To run S-Match:

0. Make sure your system have WordNet installed. 
   If WordNet is installed in other location than the default one, 
   check file_properties.xml (Windows) or file_propertiesL.xml 
   (Linux) and update dictionary_path to reflect actual WordNet 
   location.

1. Generate WordNet caches by running bin/create-wn-caches.cmd 
   (Windows) or create-wn-caches.sh (Linux)

2. Run the matching of provided sample trees by running 
   bin/all-cw.cmd (Windows) or all-cw.sh (Linux). This will execute 
   the following steps:
     a) convert provided trees from text files to internal XML format
     b) enrich the trees with logical formulas ("offline" or 1 and 
        2 steps of the algorithm)
     c) match the trees ("online" or 3 and 4 steps of the algorithm)
     d) match the trees with the minimal matching algorithm

The script will run normal matching (c) and minimal matching (d) 
algorithms, rendering results into test-data\cw folder.
