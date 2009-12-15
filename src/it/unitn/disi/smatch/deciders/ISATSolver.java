package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.SMatchException;

/**
 * Each SAT solver need to implement only one method
 * which takes as an input DIMACS string and returns true if it is satisfiable.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISATSolver {

    /**
     * Checks whether input string in DIMACS format is satisfiable.
     *
     * @param input problem in DIMACS format
     * @return whether problem is satisfiable
     */
    public boolean isSatisfiable(String input) throws SMatchException;
}
