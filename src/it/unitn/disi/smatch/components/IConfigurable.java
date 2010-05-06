package it.unitn.disi.smatch.components;

import java.util.Properties;

/**
 * Represents a component that supports a configuration.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IConfigurable {

    /**
     * Sets component configuration properties. The component might check for properties change and
     * reconfigure or reload subcomponents.
     *
     * @param newProperties a new configuration
     * @throws it.unitn.disi.smatch.SMatchException
     *          SMatchException
     */
    void setProperties(Properties newProperties) throws ConfigurableException;

    /**
     * Sets component configuration by reading it from a file.
     * @param fileName .properties file name
     * @throws ConfigurableException ConfigurableException
     */
    void setProperties(String fileName) throws ConfigurableException;

    Properties getProperties();
}
