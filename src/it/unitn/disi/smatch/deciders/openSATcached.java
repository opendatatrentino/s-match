package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.Configurable;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;

/**
 * OpenSAT-based caching implementation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class openSATcached extends Configurable implements ISATSolver {

    private static final Logger log = Logger.getLogger(openSATcached.class);

    private static final openSAT opensat = new openSAT();
    private static HashMap<String, Boolean> solutionsCache = new HashMap<String, Boolean>();
    private static int cacheHits = 0;
    private static int hits = 0;

    /**
     * This method can be used to call the OpenSAT .
     *
     * @param input The String that contains sat problem in DIMACS's format
     * @return boolean True if the formula is satisfiable, false otherwise
     * @throws SATSolverException SATSolverException
     */
    public boolean isSatisfiable(String input) throws SATSolverException {
        hits++;
        Boolean result = solutionsCache.get(input);
        if (null == result) {
            result = opensat.isSatisfiable(input);
            solutionsCache.put(input, result);
        } else {
            cacheHits++;
        }
        return result;
    }

    public static void reportStats() {
        if (hits > 0) {
            log.info("openSATcached:       hits: " + hits);
            log.info("openSATcached: cache hits: " + cacheHits);
            log.info("openSATcached: cache size: " + solutionsCache.size());
            log.info("openSATcached: cache hit rate: " + (int) (100 * (cacheHits / (double) hits)) + "%");
        }
    }
}