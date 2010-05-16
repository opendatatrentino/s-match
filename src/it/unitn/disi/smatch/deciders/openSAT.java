package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.Configurable;
import org.apache.log4j.Logger;
import org.opensat.Default;
import org.opensat.ISolver;
import org.opensat.ParseFormatException;
import org.opensat.TimeoutException;
import org.opensat.data.ICNF;
import org.opensat.parsers.Dimacs;

import java.io.LineNumberReader;
import java.io.StringReader;

/**
 * OpenSAT-based solver.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class OpenSAT extends Configurable implements ISATSolver {

    private static final Logger log = Logger.getLogger(OpenSAT.class);

    private final Dimacs parser = new Dimacs();
    private final ICNF formula = Default.cnf();
    private final ISolver solver = Default.solver();

    public boolean isSatisfiable(String input) throws SATSolverException {
        try {
            parser.parseInstance(new LineNumberReader(new StringReader(input)), formula);
            return solver.solve(formula);
        } catch (TimeoutException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SATSolverException(errMessage, e);
        } catch (ParseFormatException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SATSolverException(errMessage, e);
        }
    }
}