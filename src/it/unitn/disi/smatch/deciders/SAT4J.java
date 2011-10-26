package it.unitn.disi.smatch.deciders;

import it.unitn.disi.common.components.Configurable;
import org.apache.log4j.Logger;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * SAT4J-based Solver.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SAT4J extends Configurable implements ISATSolver {

    private static final Logger log = Logger.getLogger(SAT4J.class);

    private Reader reader;

    public SAT4J() {
        ISolver solver = SolverFactory.newLight();
        solver.setTimeout(3600); // 1 hour timeout
        reader = new DimacsReader(solver);
    }

    public boolean isSatisfiable(String input) throws SATSolverException {
        boolean result;
        try {
            IProblem problem = reader.parseInstance(new ByteArrayInputStream(input.getBytes()));
            result = problem.isSatisfiable();
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
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SATSolverException(errMessage, e);
        }
        return result;

    }
}
