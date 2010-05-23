package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.IConfigurable;

/**
 * Each SAT solver needs to implement only one method,
 * which takes as an input DIMACS string and returns true if it is satisfiable.
 *
 * DIMACS format is described in the note:
 * DIMACS Challenge - Satisfiability - Suggested Format
 * for example here: http://www.domagoj-babic.com/uploads/ResearchProjects/Spear/dimacs-cnf.pdf 
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISATSolver extends IConfigurable {

    /**
     * Checks whether input string in DIMACS format is satisfiable or not.
     *
     * @param input problem in DIMACS format
     * @return whether problem is satisfiable or not
     * @throws SATSolverException SATSolverException
     */
    public boolean isSatisfiable(String input) throws SATSolverException;
}
