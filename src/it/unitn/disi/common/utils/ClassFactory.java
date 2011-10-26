package it.unitn.disi.common.utils;

import it.unitn.disi.common.DISIException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Constructs class instances.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ClassFactory {

    private static final Logger log = Logger.getLogger(ClassFactory.class);

    /**
     * Returns object instance from the string representing its class name.
     *
     * @param className className
     * @return Object instance
     */
    public static Object getClassForName(String className) {
        Object object = null;
        try {
            Class classDefinition = Class.forName(className);
            object = classDefinition.newInstance();
        } catch (ClassNotFoundException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("ClassNotFoundException", e);
            }
        } catch (InstantiationException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("InstantiationException " + e.getMessage(), e);
            }
        } catch (IllegalAccessException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("IllegalAccessException", e);
            }
        }
        return object;
    }


    /**
     * Creates an instance of the class whose name is passed as the parameter.
     *
     * @param className  name of the class those instance is to be created
     * @param attrTypes  attrTypes
     * @param attrValues attrValues
     * @return instance of the class
     * @throws DISIException DISIException
     */
    @SuppressWarnings("unchecked")
    public static Object getClassInstance(String className,
                                          Class[] attrTypes,
                                          Object[] attrValues) throws DISIException {

        Constructor constr;
        try {
            Class cl = Class.forName(className);
            constr = cl.getConstructor(attrTypes);
        } catch (ClassNotFoundException e) {
            //yep, log and throw...
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new DISIException(errMessage, e);
        } catch (NoSuchMethodException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new DISIException(errMessage, e);
        }

        Object classInst;

        try {
            classInst = constr.newInstance(attrValues);
        } catch (InstantiationException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new DISIException(errMessage, e);
        } catch (IllegalAccessException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new DISIException(errMessage, e);
        } catch (InvocationTargetException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new DISIException(errMessage, e);
        }
        return classInst;
    }


    /**
     * Parses a string of class names separated by separator into a list of objects.
     *
     * @param str       names of classes
     * @param separator separator characters
     * @return ArrayList of class instances
     */
    public static List<Object> stringToClasses(String str, String separator) {
        ArrayList<Object> tmp = new ArrayList<Object>();
        StringTokenizer stringTokenizer = new StringTokenizer(str, separator);
        while (stringTokenizer.hasMoreTokens()) {
            Object obj = getClassForName(stringTokenizer.nextToken());
            if (obj != null) {
                tmp.add(obj);
            }
        }
        return tmp;
    }

    /**
     * Parses a string of class names separated by separator into a list of objects.
     *
     * @param str        names of classes
     * @param separator  separator characters
     * @param attrTypes  attrTypes
     * @param attrValues attrValues
     * @return ArrayList of class instances
     * @throws DISIException DISIException
     */
    public static List<Object> stringToClassInstances(String str, String separator, Class[] attrTypes,
                                                      Object[] attrValues) throws DISIException {
        ArrayList<Object> tmp = new ArrayList<Object>();
        StringTokenizer stringTokenizer = new StringTokenizer(str, separator);
        while (stringTokenizer.hasMoreTokens()) {
            Object obj = getClassInstance(stringTokenizer.nextToken(), attrTypes, attrValues);
            if (obj != null) {
                tmp.add(obj);
            }
        }
        return tmp;
    }
}