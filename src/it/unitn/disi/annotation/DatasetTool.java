package it.unitn.disi.annotation;

import it.unitn.disi.annotation.pipelines.IBaseContextPipeline;
import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.common.utils.MiscUtils;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.loaders.context.IBaseContextLoader;
import it.unitn.disi.smatch.renderers.context.IBaseContextRenderer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Processes a dataset by applying to it a series of operations in a pipeline fashion.
 * <p/>
 * Usage: DatasetTool input output -config=... -property=...
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DatasetTool extends Configurable {

    static {
        MiscUtils.configureLog4J();
    }

    private static final Logger log = Logger.getLogger(DatasetTool.class);

    public static final String DEFAULT_CONFIG_FILE_NAME = ".." + File.separator + "conf" + File.separator + "d-tool.properties";

    // usage string
    private static final String USAGE = "Usage: DatasetTool <input> [output] [options]\n" +
            " Options: \n" +
            " -config=file.properties                    read configuration from file.properties\n" +
            " -property=key=value                        supply the configuration key=value (overriding those in the config file)";

    // config file command line key
    public static final String configFileCmdLineKey = "-config=";
    // property command line key
    public static final String propCmdLineKey = "-property=";

    // component configuration keys and component instance variables
    private static final String CONTEXT_LOADER_KEY = "ContextLoader";
    private IBaseContextLoader contextLoader = null;

    private static final String CONTEXT_RENDERER_KEY = "ContextRenderer";
    private IBaseContextRenderer contextRenderer = null;

    private static final String PIPELINE_KEY = "Pipeline";
    private IBaseContextPipeline pipeline;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading configuration...");
        }
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);
        boolean result = super.setProperties(newProperties);
        if (result) {
            contextLoader = (IBaseContextLoader) configureComponent(contextLoader, oldProperties, newProperties, "context loader", CONTEXT_LOADER_KEY, IBaseContextLoader.class);
            contextRenderer = (IBaseContextRenderer) configureComponent(contextRenderer, oldProperties, newProperties, "context renderer", CONTEXT_RENDERER_KEY, IBaseContextRenderer.class);
            if (newProperties.containsKey(PIPELINE_KEY)) {
                pipeline = (IBaseContextPipeline) configureComponent(pipeline, oldProperties, newProperties, "pipeline", PIPELINE_KEY, IBaseContextPipeline.class);
            } else {
                pipeline = null;
            }
        }
        return result;
    }

    public IBaseContext loadContext(String fileName) throws ConfigurableException {
        if (null == contextLoader) {
            throw new ConfigurableException("Context loader is not configured.");
        }

        log.info("Loading context from: " + fileName);
        final IBaseContext result = contextLoader.loadContext(fileName);
        log.info("Loading context finished");
        return result;
    }

    @SuppressWarnings("unchecked")
    public void renderContext(IBaseContext context, String fileName) throws SMatchException {
        if (null == contextRenderer) {
            throw new SMatchException("Context renderer is not configured.");
        }
        log.info("Rendering context to: " + fileName);
        contextRenderer.render(context, fileName);
        log.info("Rendering context finished");
    }

    public void process(IBaseContext context) {
        try {
            if (null != pipeline) {
                log.info("Processing context...");
                pipeline.process(context);
            }
        } catch (PipelineComponentException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void afterProcessing() {
        if (null != pipeline) {
            try {
                pipeline.afterProcessing();
            } catch (PipelineComponentException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public void beforeProcessing() {
        if (null != pipeline) {
            try {
                pipeline.beforeProcessing();
            } catch (PipelineComponentException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, ConfigurableException {
        // initialize property file
        String configFileName = DEFAULT_CONFIG_FILE_NAME;
        ArrayList<String> cleanArgs = new ArrayList<String>();
        for (String arg : args) {
            if (arg.startsWith(configFileCmdLineKey)) {
                configFileName = arg.substring(configFileCmdLineKey.length());
            } else {
                cleanArgs.add(arg);
            }
        }

        args = cleanArgs.toArray(new String[cleanArgs.size()]);
        cleanArgs.clear();

        // collect properties specified on the command line
        Properties commandProperties = new Properties();
        for (String arg : args) {
            if (arg.startsWith(propCmdLineKey)) {
                String[] props = arg.substring(propCmdLineKey.length()).split("=");
                if (0 < props.length) {
                    String key = props[0];
                    String value = "";
                    if (1 < props.length) {
                        value = props[1];
                    }
                    commandProperties.put(key, value);
                }
            } else {
                cleanArgs.add(arg);
            }
        }

        args = cleanArgs.toArray(new String[cleanArgs.size()]);

        // check input parameters
        if (args.length < 1) {
            System.out.println(USAGE);
        } else {
            DatasetTool dt = new DatasetTool();

            Properties config = new Properties();
            if (new File(configFileName).exists()) {
                config.load(new FileInputStream(configFileName));
            }

            if (log.isEnabledFor(Level.DEBUG)) {
                for (String k : commandProperties.stringPropertyNames()) {
                    log.debug("property override: " + k + "=" + commandProperties.getProperty(k));
                }
            }

            // override from command line
            config.putAll(commandProperties);

            dt.setProperties(config);

            if (!config.isEmpty()) {
                if (1 == args.length) {
                    String[] inputFiles = args[0].split(";");
                    dt.beforeProcessing();
                    for (String inputFile : inputFiles) {
                        IBaseContext context = dt.loadContext(inputFile);
                        dt.process(context);
                    }
                    dt.afterProcessing();
                } else if (2 == args.length) {
                    String[] inputFiles = args[0].split(";");
                    String[] outputFiles = args[1].split(";");

                    if (inputFiles.length == outputFiles.length) {
                        dt.beforeProcessing();
                        for (int i = 0; i < inputFiles.length; i++) {
                            IBaseContext context = dt.loadContext(inputFiles[i]);
                            dt.process(context);
                            dt.renderContext(context, outputFiles[i]);
                        }
                        dt.afterProcessing();
                    } else {
                        System.out.println("Input and output arguments count mismatch.");
                    }
                }
            } else {
                System.out.println("Not enough arguments.");
            }
        }
    }
}