package it.unitn.disi.smatch.deciders;

import org.opensat.ContradictionException;
import org.opensat.Dimacs;
import org.opensat.ISolver;
import org.opensat.ParseFormatException;
import org.opensat.minisat.SolverFactory;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.StringReader;

import it.unitn.disi.smatch.SMatchException;

/**
 * OpenSAT-based solver.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class openSAT implements ISATSolver {

    private static final Logger log = Logger.getLogger(openSAT.class);

    public static int hits = 0;
    private static final Dimacs parser = new Dimacs();

    public openSAT() {
    }

    public boolean isSatisfiable(String input) throws SMatchException {
        hits++;
        ISolver solver = SolverFactory.newMiniLearning();
        try {
            LineNumberReader lnrCNF = new LineNumberReader(new BufferedReader(new StringReader(input)));
            parser.parseInstance(lnrCNF, solver);
            return solver.solve();
        } catch (ParseFormatException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SMatchException(errMessage, e);
        } catch (ContradictionException e) {
            return false;
        } catch (Exception e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SMatchException(errMessage, e);
        }
    }

    public static void reportStats() {
        if (hits > 0) {
            log.debug("openSAT hits: " + hits);
        }
    }
}
