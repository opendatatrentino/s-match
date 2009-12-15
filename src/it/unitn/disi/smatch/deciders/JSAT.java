package it.unitn.disi.smatch.deciders;

import org.satlive.jsat.algos.DPLL;
import org.satlive.jsat.algos.SatisfiabilityAlgorithm;
import org.satlive.jsat.gen.DIMACS;
import org.satlive.jsat.objects.EBase;
import org.satlive.jsat.objects.ExternalBase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.StringReader;

/**
 * JSAT-based solver.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class JSAT implements ISATSolver {
    /**
     * This method can be used to call the JSAT library.
     *
     * @param input The String that contains sat problem in DIMACS's format
     * @return boolean True if the formula is satisfiable, false otherwise
     */
    public boolean isSatisfiable(String input) {
        //Create a SAT solver instance
        SatisfiabilityAlgorithm sat = new DPLL();
        //Create a base instance
        //From a string in DIMACS format
        StringReader srCNF = new StringReader(input);
        BufferedReader brCNF = new BufferedReader(srCNF);
        ExternalBase base = new EBase();
        try {
            base = DIMACS.createBase(brCNF, base);
        } catch (FileNotFoundException fnfe) {
            System.out.println("File not found");
            System.exit(0);
        }
        // Check consistency
        boolean res = sat.isSatisfiable(base);
        if (res)
            return true;
        else
            return false;
    }
}
