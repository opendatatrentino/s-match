package it.unitn.disi.smatch.utils;

import it.unitn.disi.smatch.MatchManager;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Utility class.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SMatchUtils {

    /**
     * Loads the hashtable with multiwords
     * the multiwords are stored in the following format
     * Key-the first word in the multiwords
     * Value-Vector of Vectors, which contain the other words in the all the multiwords
     * starting from the word in Key.
     *
     * @param fileName file name
     * @return multiwords hastable
     */
    public static Hashtable<String, Vector<Vector<String>>> readHash(String fileName) {
        Hashtable<String, Vector<Vector<String>>> result = new Hashtable<String, Vector<Vector<String>>>();
        try {
            FileInputStream fos = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fos, MatchManager.BUFFER_SIZE);
            ObjectInputStream oos = new ObjectInputStream(bis);
            try {
                result = (Hashtable<String, Vector<Vector<String>>>) oos.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            oos.close();
            bis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void configureLog4J() {
        String log4jConf = System.getProperty("log4j.configuration");
        if (null != log4jConf) {
            PropertyConfigurator.configure(log4jConf);
        } else {
            System.err.println("No log4j.configuration property specified.");
        }
    }
}
