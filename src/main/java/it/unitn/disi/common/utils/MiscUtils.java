package it.unitn.disi.common.utils;

import it.unitn.disi.common.DISIException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;

/**
 * Utility class.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MiscUtils {

    private static final Logger log = Logger.getLogger(MiscUtils.class);

    /**
     * Configures LOG4J using a configuration file given in a log4j.configuration system property.
     */
    public static void configureLog4J() {
        String log4jConf = System.getProperty("log4j.configuration");
        if (null != log4jConf) {
            PropertyConfigurator.configure(log4jConf);
        } else {
            System.err.println("No log4j.configuration property specified. Using defaults.");
            BasicConfigurator.configure();
        }
    }

    /**
     * Writes Java object to a file.
     *
     * @param object   the object
     * @param fileName the file where the object will be written
     * @throws DISIException DISIException
     */
    public static void writeObject(Object object, String fileName) throws DISIException {
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
            throw new DISIException(errMessage, e);
        }
    }

    /**
     * Reads Java object from a file.
     *
     * @param fileName the file where the object is stored
     * @parm isInternalFile reads from internal data file in resources folder
     * @return the object
     * @throws DISIException DISIException
     */
    public static Object readObject(String fileName, boolean isInternalFile) throws DISIException {
        Object result;
        try {
            FileInputStream fos = null;

            if (isInternalFile == true) {
                fos = new FileInputStream(ClassLoader.getSystemResource(fileName).getPath());
            } else {
                fos = new FileInputStream(fileName);
            }

            BufferedInputStream bis = new BufferedInputStream(fos);
            ObjectInputStream oos = new ObjectInputStream(bis);
            try {
                result = oos.readObject();
            } catch (IOException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new DISIException(errMessage, e);
            } catch (ClassNotFoundException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new DISIException(errMessage, e);
            }
            oos.close();
            bis.close();
            fos.close();
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new DISIException(errMessage, e);
        }
        return result;
    }
}
