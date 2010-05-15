package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.Configurable;
import org.apache.log4j.Logger;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;
//import org.sat4j.tools.RemiUtils;

import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.StringReader;
/**
 * // TODO Need comments.
 */

public class SAT4J extends Configurable implements ISATSolver {

    private static final Logger log = Logger.getLogger(SAT4J.class);

    public boolean isSatisfiable(String input) throws SATSolverException {
        ISolver solver = new ModelIterator(SolverFactory.newMiniLearning());
        DimacsReader reader = new DimacsReader(solver);
        boolean result;
        try {
            LineNumberReader lnr = new LineNumberReader(new BufferedReader(new StringReader(input)));
            reader.parseInstance(lnr);
            result = solver.isSatisfiable();
        } catch (ParseFormatException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SATSolverException(errMessage, e);
        } catch (ContradictionException e) {
            result = false;
        } catch (TimeoutException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SATSolverException(errMessage, e);
        }
        return result;

    }
}
