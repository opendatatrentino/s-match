package it.unitn.disi.smatch;

import it.unitn.disi.smatch.classifiers.IContextClassifier;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.Context;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.filters.IMappingFilter;
import it.unitn.disi.smatch.loaders.context.IContextLoader;
import it.unitn.disi.smatch.loaders.mapping.IMappingLoader;
import it.unitn.disi.smatch.matchers.element.IMatcherLibrary;
import it.unitn.disi.smatch.matchers.structure.tree.ITreeMatcher;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.wordnet.InMemoryWordNetBinaryArray;
import it.unitn.disi.smatch.preprocessors.DefaultContextPreprocessor;
import it.unitn.disi.smatch.preprocessors.IContextPreprocessor;
import it.unitn.disi.smatch.renderers.context.IContextRenderer;
import it.unitn.disi.smatch.renderers.mapping.IMappingRenderer;
import it.unitn.disi.smatch.utils.SMatchUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * MatchManager controls the process of matching, loads contexts and performs other
 * auxiliary work.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatchManager extends Configurable implements IMatchManager {

    static {
        SMatchUtils.configureLog4J();
    }

    private static final Logger log = Logger.getLogger(MatchManager.class);

    /**
     * Default configuration file name.
     */
    private static final String DEFAULT_CONFIG_FILE_NAME = ".." + File.separator + "conf" + File.separator + "s-match.properties";
    // config file command line key
    private static final String configFileCmdLineKey = "-config=";
    // property command line key
    private static final String propCmdLineKey = "-property=";


    // usage string
    private static final String USAGE = "Usage: MatchManager <command> <arguments> [options]\n" +
            " Commands: \n" +
            " wntoflat                                  create cached WordNet files for fast matching\n" +
            " convert <input> <output>                  read input file and write it into output file\n" +
            " offline <input> <output>                  read input file, preprocess it and write it into output file\n" +
            " online <source> <target> <output>         read source and target files, run matching and write the output file\n" +
            " filter <source> <target> <input> <output> read source and target files, input mapping, run filtering and write the output mapping\n" +
            "\n" +
            " Options: \n" +
            " -config=file.properties                   read configuration from file.properties instead of default S-Match.properties\n" +
            " -property=key=value                       override the configuration key=value from the config file";


    private static final String CONTEXT_LOADER_KEY = "ContextLoader";
    private IContextLoader contextLoader = null;

    private static final String CONTEXT_RENDERER_KEY = "ContextRenderer";
    private IContextRenderer contextRenderer = null;

    private static final String MAPPING_LOADER_KEY = "MappingLoader";
    private IMappingLoader mappingLoader = null;

    private static final String MAPPING_RENDERER_KEY = "MappingRenderer";
    private IMappingRenderer mappingRenderer = null;

    private static final String MAPPING_FILTER_KEY = "MappingFilter";
    private IMappingFilter mappingFilter = null;

    private static final String CONTEXT_PREPROCESSOR_KEY = "ContextPreprocessor";
    private IContextPreprocessor contextPreprocessor = null;

    private static final String CONTEXT_CLASSIFIER_KEY = "ContextClassifier";
    private IContextClassifier contextClassifier = null;

    private static final String MATCHER_LIBRARY_KEY = "MatcherLibrary";
    private IMatcherLibrary matcherLibrary = null;

    private static final String TREE_MATCHER_KEY = "TreeMatcher";
    private ITreeMatcher treeMatcher = null;

    private static final String SENSE_MATCHER_KEY = "SenseMatcher";
    private ISenseMatcher senseMatcher = null;

    private static final String LINGUISTIC_ORACLE_KEY = "LinguisticOracle";
    private ILinguisticOracle linguisticOracle = null;

    public static IMatchManager getInstance() throws SMatchException {
        return new MatchManager();
    }

    public MatchManager() throws SMatchException {
        super();
    }

    /**
     * Constructor class with initialization.
     *
     * @param propFileName the name of the properties file
     * @throws SMatchException SMatchException
     */
    public MatchManager(String propFileName) throws SMatchException {
        this();

        // update properties
        try {
            setProperties(propFileName);
        } catch (ConfigurableException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    public IContext createContext() {
        return Context.getInstance();
    }

    public IContext loadContext(String fileName) throws SMatchException {
        if (null == contextLoader) {
            throw new SMatchException("Context loader is not configured.");
        }

        return contextLoader.loadContext(fileName);
    }

    public void renderContext(IContext ctxSource, String fileName) throws SMatchException {
        if (null == contextRenderer) {
            throw new SMatchException("Context renderer is not configured.");
        }
        contextRenderer.render(ctxSource, fileName);
    }

    public IContextMapping<INode> loadMapping(IContext ctxSource, IContext ctxTarget, String inputFile) throws SMatchException {
        if (null == mappingLoader) {
            throw new SMatchException("Mapping loader is not configured.");
        }
        return mappingLoader.loadMapping(ctxSource, ctxTarget, inputFile);
    }

    public void renderMapping(IContextMapping<INode> mapping, String outputFile) throws SMatchException {
        if (null == mappingRenderer) {
            throw new SMatchException("Mapping renderer is not configured.");
        }
        mappingRenderer.render(mapping, outputFile);
    }

    public IContextMapping<INode> filterMapping(IContextMapping<INode> mapping) throws SMatchException {
        if (null == mappingFilter) {
            throw new SMatchException("Mapping filter is not configured.");
        }
        return mappingFilter.filter(mapping);
    }

    public IMatchMatrix elementLevelMatching(IContext sourceContext, IContext targetContext) throws SMatchException {
        if (null == matcherLibrary) {
            throw new SMatchException("Matcher library is not configured.");
        }
        return matcherLibrary.elementLevelMatching(sourceContext, targetContext);
    }

    public IContextMapping<INode> structureLevelMatching(IContext sourceContext,
                                                         IContext targetContext, IMatchMatrix ClabMatrix) throws SMatchException {
        if (null == treeMatcher) {
            throw new SMatchException("Tree matcher is not configured.");
        }
        log.info("Structure level matching...");
        IContextMapping<INode> mapping = treeMatcher.treeMatch(sourceContext, targetContext, ClabMatrix);
        log.info("Structure level matching finished");
        return mapping;
    }

    public void offline(IContext context) throws SMatchException {
        log.info("Computing concept at label formulas...");
        preprocess(context);
        log.info("Computing concept at label formulas finished");

        log.info("Computing concept at node formulas...");
        classify(context);
        log.info("Computing concept at node formulas finished");
    }

    public IContextMapping<INode> online(IContext sourceContext, IContext targetContext) throws SMatchException {
        //TODO get rid of matrices?
        // Performs element level matching which computes the relation between labels.
        IMatchMatrix cLabMatrix = elementLevelMatching(sourceContext, targetContext);
        // Performs structure level matching which computes the relation between nodes.
        IContextMapping<INode> mapping = structureLevelMatching(sourceContext, targetContext, cLabMatrix);

        List<INode> sourceNodes = sourceContext.getAllNodes();
        List<INode> targetNodes = targetContext.getAllNodes();

        return mapping;
    }

    public IContextMapping<INode> match(IContext sourceContext, IContext targetContext) throws SMatchException {
        log.info("Matching started...");
        offline(sourceContext);
        offline(targetContext);
        IContextMapping<INode> result = online(sourceContext, targetContext);
        log.info("Matching finished");
        return result;
    }

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading configuration...");
        }
        if (!newProperties.equals(properties)) {
            // global ones
            linguisticOracle = (ILinguisticOracle) configureComponent(linguisticOracle, properties, newProperties, "linguistic oracle", LINGUISTIC_ORACLE_KEY, ILinguisticOracle.class);
            senseMatcher = (ISenseMatcher) configureComponent(senseMatcher, properties, newProperties, "sense matcher", SENSE_MATCHER_KEY, ISenseMatcher.class);

            contextLoader = (IContextLoader) configureComponent(contextLoader, properties, newProperties, "context loader", CONTEXT_LOADER_KEY, IContextLoader.class);
            contextRenderer = (IContextRenderer) configureComponent(contextRenderer, properties, newProperties, "context renderer", CONTEXT_RENDERER_KEY, IContextRenderer.class);
            mappingLoader = (IMappingLoader) configureComponent(mappingLoader, properties, newProperties, "mapping loader", MAPPING_LOADER_KEY, IMappingLoader.class);
            mappingRenderer = (IMappingRenderer) configureComponent(mappingRenderer, properties, newProperties, "mapping renderer", MAPPING_RENDERER_KEY, IMappingRenderer.class);
            mappingFilter = (IMappingFilter) configureComponent(mappingFilter, properties, newProperties, "mapping filter", MAPPING_FILTER_KEY, IMappingFilter.class);
            contextPreprocessor = (IContextPreprocessor) configureComponent(contextPreprocessor, properties, newProperties, "context preprocessor", CONTEXT_PREPROCESSOR_KEY, IContextPreprocessor.class);
            contextClassifier = (IContextClassifier) configureComponent(contextClassifier, properties, newProperties, "context classifier", CONTEXT_CLASSIFIER_KEY, IContextClassifier.class);
            matcherLibrary = (IMatcherLibrary) configureComponent(matcherLibrary, properties, newProperties, "matching library", MATCHER_LIBRARY_KEY, IMatcherLibrary.class);
            treeMatcher = (ITreeMatcher) configureComponent(treeMatcher, properties, newProperties, "tree matcher", TREE_MATCHER_KEY, ITreeMatcher.class);

            properties.clear();
            properties.putAll(newProperties);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void preprocess(IContext context) throws SMatchException {
        if (null == contextPreprocessor) {
            throw new SMatchException("Context preprocessor is not configured.");
        }

        log.info("Computing concepts at label...");
        contextPreprocessor.preprocess(context);
        log.info("Computing concepts at label finished");
    }

    public void classify(IContext context) throws SMatchException {
        if (null == contextClassifier) {
            throw new SMatchException("Context classifier is not configured.");
        }
        log.info("Computing concepts at node...");
        contextClassifier.buildCNodeFormulas(context);
        log.info("Computing concepts at node finished");
    }

    /**
     * Converts WordNet dictionary to binary format for fast searching.
     *
     * @param properties configuration
     * @throws SMatchException SMatchException
     */
    private void convertWordNetToFlat(Properties properties) throws SMatchException {
        InMemoryWordNetBinaryArray.createWordNetCaches(GLOBAL_PREFIX + SENSE_MATCHER_KEY, properties);
        DefaultContextPreprocessor.createWordNetCaches(CONTEXT_PREPROCESSOR_KEY, properties);
    }

    /**
     * Provides a command line interface to match manager.
     *
     * @param args command line arguments
     * @throws IOException           IOException
     * @throws ConfigurableException ConfigurableException
     */
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

        // collect properties specified on command line
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
            try {
                MatchManager mm = new MatchManager();

                // read properties
                Properties config = new Properties();

                config.load(new FileInputStream(configFileName));

                if (log.isEnabledFor(Level.DEBUG)) {
                    for (String k : commandProperties.stringPropertyNames()) {
                        log.debug("property override: " + k + "=" + commandProperties.getProperty(k));
                    }
                }

                // override from command line
                config.putAll(commandProperties);

                mm.setProperties(config);

                if ("wntoflat".equals(args[0])) {
                    mm.convertWordNetToFlat(config);
                } else if ("convert".equals(args[0])) {
                    if (2 < args.length) {
                        String inputFile = args[1];
                        String outputFile = args[2];
                        IContext ctxSource = mm.loadContext(inputFile);
                        mm.renderContext(ctxSource, outputFile);
                    } else {
                        System.out.println("Not enough arguments for convert command.");
                    }
                } else if ("offline".equals(args[0])) {
                    if (2 < args.length) {
                        String inputFile = args[1];
                        String outputFile = args[2];
                        IContext ctxSource = mm.loadContext(inputFile);
                        mm.offline(ctxSource);
                        mm.renderContext(ctxSource, outputFile);
                    } else {
                        System.out.println("Not enough arguments for offline command.");
                    }
                } else if ("online".equals(args[0])) {
                    if (3 < args.length) {
                        String sourceFile = args[1];
                        String targetFile = args[2];
                        String outputFile = args[3];
                        IContext ctxSource = mm.loadContext(sourceFile);
                        IContext ctxTarget = mm.loadContext(targetFile);
                        IContextMapping<INode> result = mm.online(ctxSource, ctxTarget);
                        mm.renderMapping(result, outputFile);
                    } else {
                        System.out.println("Not enough arguments for online command.");
                    }
                } else if ("filter".equals(args[0])) {
                    if (4 < args.length) {
                        String sourceFile = args[1];
                        String targetFile = args[2];
                        String inputFile = args[3];
                        String outputFile = args[4];

                        IContext ctxSource = mm.loadContext(sourceFile);
                        IContext ctxTarget = mm.loadContext(targetFile);
                        IContextMapping<INode> mapInput = mm.loadMapping(ctxSource, ctxTarget, inputFile);
                        IContextMapping<INode> mapOutput = mm.filterMapping(mapInput);
                        mm.renderMapping(mapOutput, outputFile);
                    } else {
                        System.out.println("Not enough arguments for mappingFilter command.");
                    }
                } else {
                    System.out.println("Unrecognized command.");
                }
            } catch (ConfigurableException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                }
                throw e;
            }
        }
    }
}