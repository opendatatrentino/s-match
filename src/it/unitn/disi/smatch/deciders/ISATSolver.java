package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.SMatchException;

/**
 * Each SAT solver needs to implement only one method,
 * which takes as an input DIMACS string and returns true if it is satisfiable.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISATSolver {

    /**
     * Checks whether input string in DIMACS format is satisfiable or not.
     *
     * @param input problem in DIMACS format
     * @return whether problem is satisfiable or not
     * @throws SMatchException
     */
	// TODO Need comments about DIMACS format
    public boolean isSatisfiable(String input) throws SMatchException;
}
