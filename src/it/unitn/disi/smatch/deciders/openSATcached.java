package it.unitn.disi.smatch.deciders;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;

import it.unitn.disi.smatch.SMatchException;

/**
 * OpenSAT-based caching implementation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class openSATcached implements ISATSolver {

    private static final Logger log = Logger.getLogger(openSATcached.class);

    private static final openSAT opensat = new openSAT();

    private static HashMap<String, Boolean> solutions = new HashMap<String, Boolean>();
    private static int cacheHits = 0;
    private static int hits = 0;

    private static boolean saveCache = false;
    private static final String cacheFile = "sat.cache";

    public openSATcached() {
        if (saveCache) {
            solutions = readCache(cacheFile);
        }
    }

    /**
     * This method can be used to call the OpenSAT .
     *
     * @param input The String that contains sat problem in DIMACS's format
     * @return boolean True if the formula is satisfiable, false otherwise
     */
    public boolean isSatisfiable(String input) throws SMatchException {
        hits++;
        Boolean result = solutions.get(input);
        if (null == result) {
            result = opensat.isSatisfiable(input);
            solutions.put(input, result);
        } else {
            cacheHits++;
        }
        return result;
    }

    public static void reportStats() {
        if (hits > 0) {
            log.info("openSATcached:       hits: " + hits);
            log.info("openSATcached: cache hits: " + cacheHits);
            log.info("openSATcached: cache size: " + solutions.size());
            log.info("openSATcached: cache hit rate: " + (int) (100 * (cacheHits / (double) hits)) + "%");
        }
        if (saveCache) {
            writeCache(solutions, cacheFile);
        }
    }


    public static void writeCache(HashMap<String, Boolean> h, String fileName) {
        if (saveCache && null != solutions && solutions.size() > 0) {
            log.info("Writing SAT cache to " + fileName);
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(h);
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static HashMap<String, Boolean> readCache(String fileName) {
        HashMap<String, Boolean> result = null;
        try {
            File cacheFile = new File(fileName);
            if (cacheFile.exists()) {
                log.info("Reading SAT cache from " + fileName);
                FileInputStream fos = new FileInputStream(fileName);
                BufferedInputStream bis = new BufferedInputStream(fos);
                ObjectInputStream oos = new ObjectInputStream(bis);
                try {
                    result = (HashMap<String, Boolean>) oos.readObject();
                    log.info("SAT cache size: " + result.size());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                oos.close();
                bis.close();
                fos.close();
            } else {
                result = new HashMap<String, Boolean>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }
}
