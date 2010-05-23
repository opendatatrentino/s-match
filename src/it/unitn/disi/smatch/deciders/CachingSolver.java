package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Properties;

/**
 * SAT solver which caches answers. Observed cache hit rates vary from 70% on small (dozens of nodes) matching tasks
 * to 99% on large (hundreds of nodes) tasks.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CachingSolver extends Configurable implements ISATSolver {

    private static final String SAT_SOLVER_KEY = "SATSolver";
    protected ISATSolver satSolver = null;

    private static HashMap<String, Boolean> solutionsCache = new HashMap<String, Boolean>();

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            satSolver = (ISATSolver) configureComponent(satSolver, properties, newProperties, "SAT solver", SAT_SOLVER_KEY, ISATSolver.class);

            properties.clear();
            properties.putAll(newProperties);
        }
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