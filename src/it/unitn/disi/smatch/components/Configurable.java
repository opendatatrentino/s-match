package it.unitn.disi.smatch.components;

import it.unitn.disi.smatch.utils.ClassFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configurable component base class.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class Configurable implements IConfigurable {

    private static final Logger log = Logger.getLogger(Configurable.class);

    protected static final String GLOBAL_PREFIX = "Global.";

    // for components prefixed with Global. 
    protected static final Map<String, IConfigurable> globalComponents = new HashMap<String, IConfigurable>();

    protected Properties properties;

    public Configurable() {
        properties = new Properties();
    }

    public Configurable(Properties properties) {
        this.properties = properties;
    }

    public void setProperties(Properties newProperties) throws ConfigurableException {
        properties.clear();
        properties.putAll(newProperties);
    }

    public void setProperties(String fileName) throws ConfigurableException {
        setProperties(loadProperties(fileName));
    }


    public Properties getProperties() {
        return properties;
    }

    /**
     * Returns only properties that start with componentPrefix, removing this prefix.
     *
     * @param componentPrefix a prefix to search
     * @param properties      properties
     * @return properties that start with componentPrefix
     */
    protected static Properties getComponentProperties(String componentPrefix, Properties properties) {
        Properties result = new Properties();
        if (null != componentPrefix) {
            int componentPrefixLength = componentPrefix.length();
            for (String propertyName : properties.stringPropertyNames()) {
                if (propertyName.startsWith(componentPrefix)) {
                    result.put(propertyName.substring(componentPrefixLength), properties.getProperty(propertyName));
                }
            }
        }
        return result;
    }

    /**
     * Creates a prefix for component to search for its properties in properties.
     *
     * @param tokenName a component configuration key
     * @param className a component class name
     * @return prefix
     */
    protected static String makeComponentPrefix(String tokenName, String className) {
        String simpleClassName = className;
        if (null != className) {
            int lastDotIdx = className.lastIndexOf(".");
            if (lastDotIdx > -1) {
                simpleClassName = className.substring(lastDotIdx + 1, className.length());
            }
        }
        return tokenName + "." + simpleClassName + ".";
    }

    protected IConfigurable configureComponent(IConfigurable component, Properties oldProperties, Properties newProperties, String componentName, String componentKey, Class componentInterface) throws ConfigurableException {
        IConfigurable result = null;
        boolean addToGlobal = false;

        // check global property
        final String globalComponentKey = GLOBAL_PREFIX + componentKey;
        if (newProperties.containsKey(globalComponentKey)) {
            // component becomes or stays global
            addToGlobal = true;
            componentKey = globalComponentKey;
        } else {
            // component becomes local
            globalComponents.remove(globalComponentKey);
        }

        String oldClassName = oldProperties.getProperty(componentKey);
        if ("".equals(oldClassName)) {
            oldClassName = null;
        }
        String newClassName = newProperties.getProperty(componentKey);
        if ("".equals(newClassName)) {
            newClassName = null;
        }
        Properties oldComponentProperties = getComponentProperties(makeComponentPrefix(componentKey, oldClassName), oldProperties);
        Properties newComponentProperties = getComponentProperties(makeComponentPrefix(componentKey, newClassName), newProperties);

        boolean reload = !oldComponentProperties.equals(newComponentProperties);
        boolean create = false;
        if (null != oldClassName) {
            if (oldClassName.equals(newClassName)) {
                result = component;
            } else {
                if (null != newClassName) {
                    create = true;
                }
            }
        } else {
            if (null != newClassName) {
                create = true;
            } else {
                if (log.isEnabledFor(Level.DEBUG)) {
                    log.debug("No " + componentName);
                }
            }
        }

        if (create) {
            synchronized (Configurable.class) {
                if (newClassName.startsWith(GLOBAL_PREFIX)) {
                    if (log.isEnabledFor(Level.DEBUG)) {
                        log.debug("Looking up global " + componentName + ": " + newClassName + "...");
                    }
                    result = globalComponents.get(newClassName);
                    if (null == result) {
                        final String errMessage = "Cannot find global " + componentName + ": " + newClassName + "...";
                        if (log.isEnabledFor(Level.ERROR)) {
                            log.error(errMessage);
                        }
                        throw new ConfigurableException(errMessage);
                    }
                } else {
                    if (log.isEnabledFor(Level.DEBUG)) {
                        log.debug("Creating " + componentName + ": " + newClassName + "...");
                    }
                    Object o = ClassFactory.getClassForName(newClassName);

                    if (componentInterface.isInstance(o)) {
                        result = (IConfigurable) o;
                    } else {
                        final String errMessage = "Specified for " + componentName + " " + newClassName + " does not support " + componentInterface.getSimpleName() + " interface";
                        log.error(errMessage);
                        throw new ConfigurableException(errMessage);
                    }
                }
            }
        }

        if (reload && null != result) {
            result.setProperties(newComponentProperties);
        }


        if (addToGlobal) {
            if (null != result) {
                globalComponents.put(globalComponentKey, result);
            } else {
                globalComponents.remove(globalComponentKey);
            }
        }

        return result;
    }

    /**
     * Loads the properties from the properties file.
     *
     * @param filename the properties file name
     * @return Properties instance
     * @throws ConfigurableException ConfigurableException
     */
    private Properties loadProperties(String filename) throws ConfigurableException {
        log.info("Loading properties from " + filename);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ConfigurableException(errMessage, e);
        }

        return properties;
    }

}
