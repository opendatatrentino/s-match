package it.unitn.disi.smatch.utils;

import it.unitn.disi.smatch.SMatchException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;

/**
 * Utility class.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SMatchUtils {

    private static final Logger log = Logger.getLogger(SMatchUtils.class);

    /**
     * Configures LOG4J using a configuration file given in a log4j.configuration system property.
     */
    public static void configureLog4J() {
        String log4jConf = System.getProperty("log4j.configuration");
        if (null != log4jConf) {
            PropertyConfigurator.configure(log4jConf);
        } else {
            System.err.println("No log4j.configuration property specified.");
        }
    }

    /**
     * Writes Java object to a file.
     *
     * @param object   the object
     * @param fileName the file where the object will be written
     * @throws SMatchException SMatchException
     */
    public static void writeObject(Object object, String fileName) throws SMatchException {
        log.info("Writing " + fileName);
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    /**
     * Reads Java object from a file.
     *
     * @param fileName the file where the object is stored
     * @return the object
     * @throws SMatchException SMatchException
     */
    public static Object readObject(String fileName) throws SMatchException {
        Object result;
        try {
            FileInputStream fos = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fos);
            ObjectInputStream oos = new ObjectInputStream(bis);
            try {
                result = oos.readObject();
            } catch (IOException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            } catch (ClassNotFoundException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
            oos.close();
            bis.close();
            fos.close();
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
        return result;
    }
}
