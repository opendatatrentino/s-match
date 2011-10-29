package it.unitn.disi.nlptools;

import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;

import java.io.File;
import java.util.Properties;

/**
 * Provides processing of short labels.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NLPTools extends Configurable implements INLPTools {

    public static final String DEFAULT_CONFIG_FILE_NAME = ".." + File.separator + "conf" + File.separator + "nlptools.properties";

    public static final String PIPELINE_CLASS_KEY = "NLPPipeline";

    public NLPTools() {
        super();
    }

    /**
     * Constructor class with initialization.
     *
     * @param propFileName the name of the properties file
     * @throws ConfigurableException ConfigurableException
     */
    public NLPTools(String propFileName) throws ConfigurableException {
        this();
        setProperties(propFileName);
    }

    /**
     * Constructor class with initialization.
     *
     * @param properties the properties
     * @throws ConfigurableException ConfigurableException
     */
    public NLPTools(Properties properties) throws ConfigurableException {
        this();
        setProperties(properties);
    }

    public INLPPipeline getPipeline() throws ConfigurableException {
        INLPPipeline pipeline = null;
        pipeline = (INLPPipeline) configureComponent(pipeline, new Properties(), properties, "NLP Pipeline", PIPELINE_CLASS_KEY, INLPPipeline.class);
        return pipeline;
    }
}