package it.unitn.disi.smatch;

import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.common.utils.MiscUtils;
import it.unitn.disi.smatch.classifiers.IContextClassifier;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingFactory;
import it.unitn.disi.smatch.data.trees.Context;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.filters.IMappingFilter;
import it.unitn.disi.smatch.loaders.context.IBaseContextLoader;
import it.unitn.disi.smatch.loaders.context.IContextLoader;
import it.unitn.disi.smatch.loaders.mapping.IMappingLoader;
import it.unitn.disi.smatch.matchers.element.IMatcherLibrary;
import it.unitn.disi.smatch.matchers.structure.tree.ITreeMatcher;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.wordnet.InMemoryWordNetBinaryArray;
import it.unitn.disi.smatch.oracles.wordnet.WordNet;
import it.unitn.disi.smatch.preprocessors.IContextPreprocessor;
import it.unitn.disi.smatch.renderers.context.IBaseContextRenderer;
import it.unitn.disi.smatch.renderers.context.IContextRenderer;
import it.unitn.disi.smatch.renderers.mapping.IMappingRenderer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * MatchManager controls the process of matching, loads contexts and performs other auxiliary work.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MatchManager extends Configurable implements IMatchManager {

    static {
        MiscUtils.configureLog4J();
    }

    private static final Logger log = Logger.getLogger(MatchManager.class);

    /**
     * Default configuration file name.
     */
    public static final String DEFAULT_CONFIG_FILE_NAME = ".." + File.separator + "conf" + File.separator + "s-match.properties";
    // config file command line key
    public static final String configFileCmdLineKey = "-config=";
    // property command line key
    public static final String propCmdLineKey = "-property=";


    // usage string
    private static final String USAGE = "Usage: MatchManager <command> <arguments> [options]\n" +
            " Commands: \n" +
            " wntoflat                                   create cached WordNet files for fast matching\n" +
            " convert <input> <output>                   read input file and write it into output file\n" +
            " convert <source> <target> <input> <output> read source, target and input mapping, and write the output mapping\n" +
            " offline <input> <output>                   read input file, preprocess it and write it into output file\n" +
            " online <source> <target> <output>          read source and target files, run matching and write the output file\n" +
            " filter <source> <target> <input> <output>  read source and target files, input mapping, run filtering and write the output mapping\n" +
            "\n" +
            " Options: \n" +
            " -config=file.properties                    read configuration from file.properties instead of default S-Match.properties\n" +
            " -property=key=value                        override the configuration key=value from the config file";


    // component configuration keys and component instance variables
    private static final String CONTEXT_LOADER_KEY = "ContextLoader";
    private IBaseContextLoader contextLoader = null;

    private static final String CONTEXT_RENDERER_KEY = "ContextRenderer";
    private IBaseContextRenderer contextRenderer = null;

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

    private static final String MAPPING_FACTORY_KEY = "MappingFactory";
    private IMappingFactory mappingFactory = null;

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

    /**
     * Constructor class with initialization.
     *
     * @param properties the properties
     * @throws SMatchException SMatchException
     */
    public MatchManager(Properties properties) throws SMatchException {
        this();

        // update properties
        try {
            setProperties(properties);
        } catch (ConfigurableException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    public IContext createContext() {
        return new Context();
    }

    public IMappingFactory getMappingFactory() {
        return mappingFactory;
    }

    public IBaseContext loadContext(String fileName) throws SMatchException {
        if (null == contextLoader) {
            throw new SMatchException("Context loader is not configured.");
        }

        log.info("Loading context from: " + fileName);
        final IBaseContext result = contextLoader.loadContext(fileName);
        log.info("Loading context finished");
        return result;
    }

    public IBaseContextLoader getContextLoader() {
        return contextLoader;
    }

    @SuppressWarnings("unchecked")
    public void renderContext(IBaseContext ctxSource, String fileName) throws SMatchException {
        if (null == contextRenderer) {
            throw new SMatchException("Context renderer is not configured.");
        }
        log.info("Rendering context to: " + fileName);
        contextRenderer.render(ctxSource, fileName);
        log.info("Rendering context finished");
    }

    public IBaseContextRenderer getContextRenderer() {
        return contextRenderer;
    }

    public IContextMapping<INode> loadMapping(IContext ctxSource, IContext ctxTarget, String inputFile) throws SMatchException {
        if (null == mappingLoader) {
            throw new SMatchException("Mapping loader is not configured.");
        }
        log.info("Loading mapping from: " + inputFile);
        final IContextMapping<INode> result = mappingLoader.loadMapping(ctxSource, ctxTarget, inputFile);
        log.info("Mapping loading finished");
        return result;
    }

    public IMappingLoader getMappingLoader() {
        return mappingLoader;
    }

    public void renderMapping(IContextMapping<INode> mapping, String outputFile) throws SMatchException {
        if (null == mappingRenderer) {
            throw new SMatchException("Mapping renderer is not configured.");
        }
        log.info("Rendering mapping to: " + outputFile);
        mappingRenderer.render(mapping, outputFile);
        log.info("Mapping rendering finished");
    }

    public IMappingRenderer getMappingRenderer() {
        return mappingRenderer;
    }

    public IContextMapping<INode> filterMapping(IContextMapping<INode> mapping) throws SMatchException {
        if (null == mappingFilter) {
            throw new SMatchException("Mapping filter is not configured.");
        }
        log.info("Filtering...");
        final IContextMapping<INode> result = mappingFilter.filter(mapping);
        log.info("Filtering finished");
        return result;
    }

    public IContextMapping<IAtomicConceptOfLabel> elementLevelMatching(IContext sourceContext, IContext targetContext) throws SMatchException {
        if (null == matcherLibrary) {
            throw new SMatchException("Matcher library is not configured.");
        }

        if (!sourceContext.getRoot().getNodeData().isSubtreePreprocessed()) {
            throw new SMatchException("Source context is not preprocessed.");
        }

        if (!targetContext.getRoot().getNodeData().isSubtreePreprocessed()) {
            throw new SMatchException("Target context is not preprocessed.");
        }

        log.info("Element level matching...");
        final IContextMapping<IAtomicConceptOfLabel> acolMapping = matcherLibrary.elementLevelMatching(sourceContext, targetContext);
        log.info("Element level matching finished");
        return acolMapping;
    }

    public IContextMapping<INode> structureLevelMatching(IContext sourceContext,
                                                         IContext targetContext, IContextMapping<IAtomicConceptOfLabel> acolMapping) throws SMatchException {
        if (null == treeMatcher) {
            throw new SMatchException("Tree matcher is not configured.");
        }
        log.info("Structure level matching...");
        IContextMapping<INode> mapping = treeMatcher.treeMatch(sourceContext, targetContext, acolMapping);
        log.info("Structure level matching finished");
        log.info("Returning links: " + mapping.size());
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
        // Performs element level matching which computes the relation between labels.
        IContextMapping<IAtomicConceptOfLabel> acolMapping = elementLevelMatching(sourceContext, targetContext);
        // Performs structure level matching which computes the relation between nodes.
        return structureLevelMatching(sourceContext, targetContext, acolMapping);
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
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading configuration...");
        }
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);
        boolean result = super.setProperties(newProperties);
        if (result) {
            // global ones
            linguisticOracle = (ILinguisticOracle) configureComponent(linguisticOracle, oldProperties, newProperties, "linguistic oracle", LINGUISTIC_ORACLE_KEY, ILinguisticOracle.class);
            senseMatcher = (ISenseMatcher) configureComponent(senseMatcher, oldProperties, newProperties, "sense matcher", SENSE_MATCHER_KEY, ISenseMatcher.class);
            mappingFactory = (IMappingFactory) configureComponent(mappingFactory, oldProperties, newProperties, "mapping factory", MAPPING_FACTORY_KEY, IMappingFactory.class);

            contextLoader = (IContextLoader) configureComponent(contextLoader, oldProperties, newProperties, "context loader", CONTEXT_LOADER_KEY, IContextLoader.class);
            contextRenderer = (IContextRenderer) configureComponent(contextRenderer, oldProperties, newProperties, "context renderer", CONTEXT_RENDERER_KEY, IContextRenderer.class);
            mappingLoader = (IMappingLoader) configureComponent(mappingLoader, oldProperties, newProperties, "mapping loader", MAPPING_LOADER_KEY, IMappingLoader.class);
            mappingRenderer = (IMappingRenderer) configureComponent(mappingRenderer, oldProperties, newProperties, "mapping renderer", MAPPING_RENDERER_KEY, IMappingRenderer.class);
            mappingFilter = (IMappingFilter) configureComponent(mappingFilter, oldProperties, newProperties, "mapping filter", MAPPING_FILTER_KEY, IMappingFilter.class);
            contextPreprocessor = (IContextPreprocessor) configureComponent(contextPreprocessor, oldProperties, newProperties, "context preprocessor", CONTEXT_PREPROCESSOR_KEY, IContextPreprocessor.class);
            contextClassifier = (IContextClassifier) configureComponent(contextClassifier, oldProperties, newProperties, "context classifier", CONTEXT_CLASSIFIER_KEY, IContextClassifier.class);
            matcherLibrary = (IMatcherLibrary) configureComponent(matcherLibrary, oldProperties, newProperties, "matching library", MATCHER_LIBRARY_KEY, IMatcherLibrary.class);
            treeMatcher = (ITreeMatcher) configureComponent(treeMatcher, oldProperties, newProperties, "tree matcher", TREE_MATCHER_KEY, ITreeMatcher.class);
        }
        return result;
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

    public IContextPreprocessor getContextPreprocessor() {
        return contextPreprocessor;
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
        WordNet.createWordNetCaches(CONTEXT_PREPROCESSOR_KEY, properties);
    }

    /**
     * Provides a command line interface to the match manager.
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
            MatchManager mm = new MatchManager();

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
                    if (3 == args.length) {
                        String inputFile = args[1];
                        String outputFile = args[2];
                        IBaseContext ctxSource = mm.loadContext(inputFile);
                        mm.renderContext(ctxSource, outputFile);
                    } else if (5 == args.length) {
                        String sourceFile = args[1];
                        String targetFile = args[2];
                        String inputFile = args[3];
                        String outputFile = args[4];

                        if (mm.getContextLoader() instanceof IContextLoader) {
                            IContext ctxSource = (IContext) mm.loadContext(sourceFile);
                            IContext ctxTarget = (IContext) mm.loadContext(targetFile);
                            IContextMapping<INode> map = mm.loadMapping(ctxSource, ctxTarget, inputFile);
                            mm.renderMapping(map, outputFile);
                        } else {
                            System.out.println("To convert a mapping, use context loaders supporting IContextLoader.");
                        }
                    }
                } else {
                    System.out.println("Not enough arguments for convert command.");
                }
            } else if ("offline".equals(args[0])) {
                if (2 < args.length) {
                    String inputFile = args[1];
                    String outputFile = args[2];
                    if (mm.getContextLoader() instanceof IContextLoader && mm.getContextRenderer() instanceof IContextRenderer) {
                        IContext ctxSource = (IContext) mm.loadContext(inputFile);
                        mm.offline(ctxSource);
                        mm.renderContext(ctxSource, outputFile);
                    } else {
                        System.out.println("To preprocess a mapping, use context loaders and renderers support IContextLoader and IContextRenderer.");
                    }
                } else {
                    System.out.println("Not enough arguments for offline command.");
                }
            } else if ("online".equals(args[0])) {
                if (3 < args.length) {
                    String sourceFile = args[1];
                    String targetFile = args[2];
                    String outputFile = args[3];
                    if (mm.getContextLoader() instanceof IContextLoader) {
                        IContext ctxSource = (IContext) mm.loadContext(sourceFile);
                        IContext ctxTarget = (IContext) mm.loadContext(targetFile);
                        IContextMapping<INode> result = mm.online(ctxSource, ctxTarget);
                        mm.renderMapping(result, outputFile);
                    } else {
                        System.out.println("To match contexts, use context loaders supporting IContextLoader.");
                    }
                } else {
                    System.out.println("Not enough arguments for online command.");
                }
            } else if ("filter".equals(args[0])) {
                if (4 < args.length) {
                    String sourceFile = args[1];
                    String targetFile = args[2];
                    String inputFile = args[3];
                    String outputFile = args[4];

                    if (mm.getContextLoader() instanceof IContextLoader) {
                        IContext ctxSource = (IContext) mm.loadContext(sourceFile);
                        IContext ctxTarget = (IContext) mm.loadContext(targetFile);
                        IContextMapping<INode> mapInput = mm.loadMapping(ctxSource, ctxTarget, inputFile);
                        IContextMapping<INode> mapOutput = mm.filterMapping(mapInput);
                        mm.renderMapping(mapOutput, outputFile);
                    } else {
                        System.out.println("To filter a mapping, use context loaders supporting IContextLoader.");
                    }
                } else {
                    System.out.println("Not enough arguments for mappingFilter command.");
                }
            } else {
                System.out.println("Unrecognized command.");
            }
        }
    }
}