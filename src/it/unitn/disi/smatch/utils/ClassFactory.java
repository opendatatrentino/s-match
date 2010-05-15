package it.unitn.disi.smatch.utils;

import it.unitn.disi.smatch.SMatchException;
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
 * @author Aliaksandr Autayeu avtaev@gmail.com
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
     * @throws SMatchException SMatchException
     */
    public static Object getClassInstance(String className,
                                          Class[] attrTypes,
                                          Object[] attrValues) throws SMatchException {

        Constructor constr;
        try {
            Class cl = Class.forName(className);
            constr = cl.getConstructor(attrTypes);
        } catch (ClassNotFoundException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("ClassNotFoundException", e);
            }
            throw new SMatchException("ERROR: ClassNotFoundException " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("NoSuchMethodException", e);
            }
            throw new SMatchException("ERROR: NoSuchMethodException " + e.getMessage(), e);
        }

        Object classInst;

        try {
            classInst = constr.newInstance(attrValues);
        } catch (InstantiationException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("InstantiationException " + e.getMessage(), e);
            }
            throw new SMatchException("ERROR: InstantiationException " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("IllegalAccessException", e);
            }
            throw new SMatchException("ERROR: IllegalAccessException " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("InvocationTargetException", e);
            }
            if (e.getTargetException() != null) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("InvocationTargetException.getTargetException " + e.getTargetException().getStackTrace().toString(), e);
                }
                throw new SMatchException("ERROR: InvocationTargetException " + e.getMessage() + " Target:" + e.getTargetException().getMessage(), e);
            }
            throw new SMatchException("ERROR: InvocationTargetException " + e.getMessage(), e);
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
     * @throws SMatchException SMatchException
     */
    public static List<Object> stringToClassInstances(String str, String separator, Class[] attrTypes,
                                                      Object[] attrValues) throws SMatchException {
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