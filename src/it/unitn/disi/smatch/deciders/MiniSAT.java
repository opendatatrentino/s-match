package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.Configurable;
import org.apache.log4j.Logger;
import org.opensat.ContradictionException;
import org.opensat.Dimacs;
import org.opensat.ISolver;
import org.opensat.ParseFormatException;
import org.opensat.minisat.SolverFactory;

import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.StringReader;

public class MiniSAT extends Configurable implements ISATSolver {

    private static final Logger log = Logger.getLogger(MiniSAT.class);

    private static final Dimacs parser = new Dimacs();

    public boolean isSatisfiable(String input) throws SATSolverException {
        ISolver solver = SolverFactory.newMiniLearning();
        try {
            LineNumberReader lnrCNF = new LineNumberReader(new BufferedReader(new StringReader(input)));
            parser.parseInstance(lnrCNF, solver);
            return solver.solve();
        } catch (ParseFormatException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SATSolverException(errMessage, e);
        } catch (ContradictionException e) {
            return false;
        } catch (Exception e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            log.error(input);
            throw new SATSolverException(errMessage, e);
        }
    }
}
