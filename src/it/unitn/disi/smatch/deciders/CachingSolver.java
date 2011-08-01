package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Properties;

/**
 * SAT solver which caches answers. Observed cache hit rates vary from 70% on small (dozens of nodes) matching tasks
 * to 99% on large (hundreds of nodes) tasks. Needs SATSolver configuration parameter pointing to a class implementing
 * {@link it.unitn.disi.smatch.deciders.ISATSolver} to solve SAT problems.
 *
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public class CachingSolver extends Configurable implements ISATSolver {

    private static final Logger log = Logger.getLogger(CachingSolver.class);

    private static final String SAT_SOLVER_KEY = "SATSolver";
    protected ISATSolver satSolver = null;

    private static HashMap<String, Boolean> solutionsCache = new HashMap<String, Boolean>();

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(SAT_SOLVER_KEY)) {
                satSolver = (ISATSolver) configureComponent(satSolver, oldProperties, newProperties, "SAT solver", SAT_SOLVER_KEY, ISATSolver.class);
            } else {
                final String errMessage = "Cannot find configuration key " + SAT_SOLVER_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }

    /**
     * Calls the solver and caches the answer.
     *
     * @param input The String that contains sat problem in DIMACS's format
     * @return boolean True if the formula is satisfiable, false otherwise
     * @throws SATSolverException SATSolverException
     */
    public boolean isSatisfiable(String input) throws SATSolverException {
        Boolean result = solutionsCache.get(input);
        if (null == result) {
            result = satSolver.isSatisfiable(input);
            solutionsCache.put(input, result);
        }
        return result;
    }
}