package it.unitn.disi.smatch;

import it.unitn.disi.smatch.classifiers.IClassifier;
import it.unitn.disi.smatch.data.Context;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.IMatchingContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.Mapping;
import it.unitn.disi.smatch.data.mappings.MappingElement;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.filters.IFilter;
import it.unitn.disi.smatch.loaders.ILoader;
import it.unitn.disi.smatch.loaders.IMappingLoader;
import it.unitn.disi.smatch.loaders.PlainMappingLoader;
import it.unitn.disi.smatch.matchers.element.IMatcherLibrary;
import it.unitn.disi.smatch.matchers.structure.tree.ITreeMatcher;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.IWordNetMatcher;
import it.unitn.disi.smatch.oracles.wordnet.InMemoryWordNetBinaryArray;
import it.unitn.disi.smatch.preprocessors.IPreprocessor;
import it.unitn.disi.smatch.renderers.context.IContextRenderer;
import it.unitn.disi.smatch.renderers.mapping.IMappingRenderer;
import it.unitn.disi.smatch.utils.SMatchUtils;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * MatchManager controls the process of matching, loads contexts and performs other
 * auxiliary work.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatchManager implements IMatchManager {

    static {
        SMatchUtils.configureLog4J();
    }

    private static final Logger log = Logger.getLogger(MatchManager.class);

    /**
     * Default configuration file name.
     */
    private static final String DEFAULT_CONFIG_FILE_NAME = ".." + File.separator + "conf" + File.separator + "S-Match.properties";
    // config file command line key
    private static final String propFileCmdLineKey = "-prop=";

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
            " -prop=file.properties                     read configuration from file.properties instead of default S-Match.properties";


    // Settings from the properties file
    // whether to use element level semantic matchers library or exploit the
    // only WordNet
    public static boolean useWeakSemanticsElementLevelMatchersLibrary = true;

    // which Sat solver to use
    public static String satSolverClass = null;// "it.unitn.disi.smatch.deciders.openSAT";
    // contains the classes of string matchers (Implementations of
    // IStringBasedElementLevelSemanticMatcher interface)
    public static Vector stringMatchers = new Vector();
    // contains the classes of sense and gloss based matchers (Implementations
    // of ISenseGlossBasedElementLevelSemanticMatcher interface)
    public static Vector senseGlossMatchers = new Vector();
    // whether to use fast reasoning techniques for conjunctive and atomic
    // labels cases
    public static boolean useConjunctiveLabelsOptimization = false;
    // whether to use disjointness test optimization technique
    public static boolean useOppositeAxiomsOptimization = false;
    // leave one sense per label (WSD) or leave many (WSF)
    private static boolean oneSensePerLabel = true;
    // default value for the class which implements IWordNetMatcher interface
    private static String strWNmatcher = null;// "it.unitn.disi.smatch.oracles.wordnet.DefaultWordNetMatcher";
    // default value for the class which implements ILinguisticOracle interface
    private static String strLinguisticOracle = null;// "it.unitn.disi.smatch.oracles.wordnet.WordNet";
    // default value for the class which implements IClassifier interface
    private String strClassifier = null;// "it.unitn.disi.smatch.classifiers.DefaultClassifier";
    // default value for the class which implements IPreprocessor interface
    private String strPreprocessor = null;// "it.unitn.disi.smatch.preprocessors.DefaultPreprocessor";
    // default value for the class which implements IMatcherLibrary interface
    private String strMatcherLibrary = null;// "it.unitn.disi.smatch.matchers.element.MatcherLibrary";
    // default value for the class which implements ILoader interface
    private String strLoader = null;// "it.unitn.disi.smatch.loaders.CTXMLLoader";
    // default value for the class which implements IMappingRenderer interface
    private String strMappingRenderer = null;// "it.unitn.disi.smatch.renderers.mapping.TaxMEMappingRenderer";
    // default value for the class which implements IContextRenderer interface
    private String strContextRenderer = null;// "it.unitn.disi.smatch.renderers.context.CTXMLContextRenderer";
    // default value for the class which implements IFilter interface
    private String strFilter = null;// "it.unitn.disi.smatch.filters.ZeroFilter";
    // default value for the class which implements IContext interface
    private String strContext = null;// "it.unitn.disi.smatch.data.Context";
    // default value for the class which implements ITreeMatcher interface
    private String strTreeMatcher = null;// "it.unitn.disi.smatch.matchers.structure.tree.DefaultTreeMatcher";

    // the words which are treated as logical and (&)
    public static String andWords = " and + & ^ of , for . ";
    // the words which are treated as logical or (|)
    public static String orWords = " or | ";
    // the words which are treated as logical not (~)
    public static String notWords = " except ";
    // the words which are cut off from the area of discourse
    public static String meaninglessWords = "of on to from by in at is are have has the a as - ~ ? ! @ # $ % * = . [ ] { } ( ) genre alfabet region topic q w e r t y u i IWNM p a s d f g h j k l z x c v b n m ";
    // the multiwords file name
    public static String multiwordsFileName = "..\\data\\multiwords.hash";

    // Wordnet dictionary object
    private static Dictionary wordNetDictionary = null;
    // WordNet matcher interface
    private static IWordNetMatcher WNMatcher = null;
    // Linguistic Oracle interface
    private static ILinguisticOracle linguisticOracle = null;
    // Classification engine interface
    private IClassifier classifier = null;
    // Linguistic Oracle interface
    private IPreprocessor preprocessor = null;
    // Matcher Library interface
    private IMatcherLibrary matcherLibrary = null;
    // Loader interface
    private ILoader loader = null;
    // Mapping renderer interface
    private IMappingRenderer mappingRenderer = null;
    // Context renderer interface
    private IContextRenderer contextRenderer = null;
    // Filter interface
    private IFilter filter = null;
    // Tree matcher interface
    private ITreeMatcher treeMatcher = null;

    //mapping loader
    private IMappingLoader mappingLoader = null;

    // Relations abbreviations
    public static final char SYNOMYM = '=';
    public static final char WEAK_EQUIVALENCE = 'E';
    public static final char LESS_GENERAL_THAN = '<';
    public static final char MORE_GENERAL_THAN = '>';
    public static final char OPPOSITE_MEANING = '!';

    //for entail matchers, for minimal links paper
    public static final char ENTAILED_LESS_GENERAL_THAN = 'L';
    public static final char ENTAILED_MORE_GENERAL_THAN = 'M';
    public static final char ENTAILED_OPPOSITE_MEANING = 'X';

    public static final char IDK_RELATION = '?';
    public static final String UNKNOWN_MEANING = "n#000000";
    public static final char ERASED_LG = 'L';
    public static final char ERASED_MG = 'M';
    // files of WN cache
    public static String adjectiveSynonymFile = "adj_syn.arr";
    public static String adjectiveAntonymFile = "adj_opp.arr";
    public static String nounMGFile = "noun3_mg.arr";
    public static String nounAntonymFile = "noun_opp_new.arr";
    public static String verbMGFile = "verb_mg.arr";
    public static String nominalizationsFile = "nominalizations.arr";
    public static String adverbsAntonymFile = "adv_opp.arr";

    // buffer size for input, output operations.
    public static int BUFFER_SIZE = 5000000;
    // ELSMthreshold for element level semantic matchers.
    // Edit Distance, Optimized edit distance, NGram matchers are controlled by this value.
    public static double ELSMthreshold = 0.9;
    // Number characters for linguistic preprocessing.
    public static String numberCharacters = "1234567890_ .,|\\/-";

    public MatchManager() throws SMatchException {
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
        setProperties(loadProperties(propFileName));
    }

    public static IMatchManager getInstance() throws SMatchException {
        return new MatchManager();
    }

    /**
     * Creates the components such as classifier, loader and others.
     */
    private void createComponents() {
//        // create an Oracle
//        if (wordNetDictionary == null) {
//            wordNetDictionary = Dictionary.getInstance();
//        }
//        // get WN matcher and Linguistic oracle interfaces
//        if ((strWNmatcher != null) && (strWNmatcher.trim().length() > 0)) {
//            WNMatcher = (IWordNetMatcher) getClassForName(strWNmatcher);
//        }
//        if ((strLinguisticOracle != null)
//                && (strLinguisticOracle.trim().length() > 0)) {
//            linguisticOracle = (ILinguisticOracle) getClassForName(strLinguisticOracle);
//        }
        if ((strClassifier != null) && (strClassifier.trim().length() > 0)) {
            classifier = (IClassifier) getClassForName(strClassifier);
        }
//        if ((strPreprocessor != null) && (strPreprocessor.trim().length() > 0)) {
//            preprocessor = (IPreprocessor) getClassForName(strPreprocessor);
//        }
//        if ((strMatcherLibrary != null)
//                && (strMatcherLibrary.trim().length() > 0)) {
//            matcherLibrary = (IMatcherLibrary) getClassForName(strMatcherLibrary);
//        }
        if ((strLoader != null) && (strLoader.trim().length() > 0)) {
            loader = (ILoader) getClassForName(strLoader);
        }
        if ((strMappingRenderer != null)
                && (strMappingRenderer.trim().length() > 0)) {
            mappingRenderer = (IMappingRenderer) getClassForName(strMappingRenderer);
        }
        if ((strContextRenderer != null)
                && (strContextRenderer.trim().length() > 0)) {
            contextRenderer = (IContextRenderer) getClassForName(strContextRenderer);
        }
        if ((strFilter != null) && (strFilter.trim().length() > 0)) {
            filter = (IFilter) getClassForName(strFilter);
        }
        if ((strTreeMatcher != null) && (strTreeMatcher.trim().length() > 0)) {
            treeMatcher = (ITreeMatcher) getClassForName(strTreeMatcher);
        }

        mappingLoader = new PlainMappingLoader();
    }

    public void setSatSolver(String satSolverClass) {
        this.satSolverClass = satSolverClass;
    }

    public void setWNMatcher(String WNMatcher) {
        strWNmatcher = WNMatcher;
        this.WNMatcher = (IWordNetMatcher) getClassForName(strWNmatcher);
    }

    public void setLinguisticOracle(String linguisticOracle) {
        strLinguisticOracle = linguisticOracle;
        this.linguisticOracle = (ILinguisticOracle) getClassForName(strLinguisticOracle);
    }

    public void setClassifier(String classifier) {
        strClassifier = classifier;
        this.classifier = (IClassifier) getClassForName(strClassifier);
    }

    public void setPreprocessor(String preprocessor) {
        strPreprocessor = preprocessor;
        this.preprocessor = (IPreprocessor) getClassForName(strPreprocessor);
    }

    public void setMatcherLibrary(String matcherLibrary) {
        strMatcherLibrary = matcherLibrary;
        this.matcherLibrary = (IMatcherLibrary) getClassForName(strMatcherLibrary);
    }

    public void setLoader(String loader) {
        strLoader = loader;
        this.loader = (ILoader) getClassForName(strLoader);
    }

    public void setMappingRenderer(String mappingRenderer) {
        strMappingRenderer = mappingRenderer;
        this.mappingRenderer = (IMappingRenderer) getClassForName(strMappingRenderer);
    }

    public void setContextRenderer(String contextRenderer) {
        strContextRenderer = contextRenderer;
        this.contextRenderer = (IContextRenderer) getClassForName(strContextRenderer);
    }

    public void setFilter(String filter) {
        strFilter = filter;
        this.filter = (IFilter) getClassForName(strFilter);
    }

    public void setTreeMatcher(String treeMatcher) {
        strTreeMatcher = treeMatcher;
        this.treeMatcher = (ITreeMatcher) getClassForName(strTreeMatcher);
    }

    public static Dictionary getWordNetDictionary() {
        // create an Oracle
        if (null == wordNetDictionary) {
            wordNetDictionary = Dictionary.getInstance();
        }

        return wordNetDictionary;
    }

    public static IWordNetMatcher getIWNMatcher() {
        // get WN matcher and Linguistic oracle interfaces
        if ((null == WNMatcher) && (null != strWNmatcher) && (strWNmatcher.trim().length() > 0)) {
            WNMatcher = (IWordNetMatcher) getClassForName(strWNmatcher);
        }

        return WNMatcher;
    }

    public static ILinguisticOracle getLinguisticOracle() {
        if ((null == linguisticOracle) && (null != strLinguisticOracle) && (strLinguisticOracle.trim().length() > 0)) {
            linguisticOracle = (ILinguisticOracle) getClassForName(strLinguisticOracle);
        }

        return linguisticOracle;
    }


    public IPreprocessor getPreprocessor() {
        if ((null == preprocessor) && (null != strPreprocessor) && (strPreprocessor.trim().length() > 0)) {
            preprocessor = (IPreprocessor) getClassForName(strPreprocessor);
        }

        return preprocessor;
    }


    public IMatcherLibrary getMatcherLibrary() {
        if ((null == matcherLibrary) && (strMatcherLibrary != null) && (strMatcherLibrary.trim().length() > 0)) {
            matcherLibrary = (IMatcherLibrary) getClassForName(strMatcherLibrary);
        }

        return matcherLibrary;
    }

    public IContext createContext() {
        return Context.getInstance();
    }

    /**
     * Loads the properties from the properties file.
     *
     * @param filename the properties file name
     * @throws SMatchException SMatchException
     * @return Properties instance
     */
    private Properties loadProperties(String filename) throws SMatchException {
        log.info("Loading properties from " + filename);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }

        return properties;
    }

    public void setProperties(Properties properties) throws SMatchException {
        //TODO support for properties update: components recreation on properties modification  

        String JWNLPropertiesPath = ".." + File.separator + "conf" + File.separator + "file_properties.xml";

        if (properties.containsKey("MatchMatrixClassName")) {
            MatrixFactory.MATRIX_CLASS_NAME = properties.getProperty("MatchMatrixClassName");
        }
        if (properties.containsKey("useWeakSemanticsElementLevelMatchersLibrary")) {
            useWeakSemanticsElementLevelMatchersLibrary = properties.getProperty(
                    "useWeakSemanticsElementLevelMatchersLibrary").equals("true");
        }
        if (properties.containsKey("useConjunctiveLabelsOptimization")) {
            useConjunctiveLabelsOptimization = properties.getProperty("useConjunctiveLabelsOptimization").equals("true");
        }
        if (properties.containsKey("useOppositeAxiomsOptimization")) {
            useOppositeAxiomsOptimization = properties.getProperty("useOppositeAxiomsOptimization").equals("true");
        }
        if (properties.containsKey("oneSensePerLabel")) {
            oneSensePerLabel = properties.getProperty("oneSensePerLabel").equals("true");
        }
        if (properties.containsKey("stringMatchers")) {
            String strStringMatchers = properties.getProperty("stringMatchers");
            stringMatchers = fromStringToVectorOfClasses(strStringMatchers, ";");
        }
        if (properties.containsKey("senseGlossMatchers")) {
            String strSenseGlossMatchers = properties.getProperty("senseGlossMatchers");
            senseGlossMatchers = fromStringToVectorOfClasses(strSenseGlossMatchers, ";");
        }
        if (properties.containsKey("satSolverClass")) {
            satSolverClass = properties.getProperty("satSolverClass");
        }
        if (properties.containsKey("JWNLpropertiesPath")) {
            JWNLPropertiesPath = properties.getProperty("JWNLpropertiesPath");
        }
        if (properties.containsKey("andWords")) {
            andWords = " " + properties.getProperty("andWords") + " ";
        }
        if (properties.containsKey("orWords")) {
            orWords = " " + properties.getProperty("orWords") + " ";
        }
        if (properties.containsKey("notWords")) {
            notWords = " " + properties.getProperty("notWords") + " ";
        }
        if (properties.containsKey("meaninglessWords")) {
            meaninglessWords = " " + properties.getProperty("meaninglessWords") + " ";
        }
        if (properties.containsKey("multiwordsFileName")) {
            multiwordsFileName = properties.getProperty("multiwordsFileName");
        }
        if (properties.containsKey("LinguisticOracleClass")) {
            strLinguisticOracle = properties.getProperty("LinguisticOracleClass");
        }
        if (properties.containsKey("adjectiveSynonymFile")) {
            adjectiveSynonymFile = properties.getProperty("adjectiveSynonymFile");
        }
        if (properties.containsKey("adjectiveAntonymFile")) {
            adjectiveAntonymFile = properties.getProperty("adjectiveAntonymFile");
        }
        if (properties.containsKey("nounMGFile")) {
            nounMGFile = properties.getProperty("nounMGFile");
        }
        if (properties.containsKey("nounAntonymFile")) {
            nounAntonymFile = properties.getProperty("nounAntonymFile");
        }
        if (properties.containsKey("verbMGFile")) {
            verbMGFile = properties.getProperty("verbMGFile");
        }
        if (properties.containsKey("nominalizationsFile")) {
            nominalizationsFile = properties.getProperty("nominalizationsFile");
        }
        if (properties.containsKey("adverbsAntonymFile")) {
            adverbsAntonymFile = properties.getProperty("adverbsAntonymFile");
        }
        if (properties.containsKey("BUFFER_SIZE")) {
            BUFFER_SIZE = Integer.parseInt(properties.getProperty("BUFFER_SIZE"));
        }
        if (properties.containsKey("ELSMThreshold")) {
            ELSMthreshold = Double.parseDouble(properties.getProperty("ELSMThreshold"));
        }
        if (properties.containsKey("numberCharacters")) {
            numberCharacters = properties.getProperty("numberCharacters");
        }
        if (properties.containsKey("WNmatcher")) {
            strWNmatcher = properties.getProperty("WNmatcher");
        }
        if (properties.containsKey("LinguisticOracle")) {
            strLinguisticOracle = properties.getProperty("LinguisticOracle");
        }
        if (properties.containsKey("Classifier")) {
            strClassifier = properties.getProperty("Classifier");
        }
        if (properties.containsKey("Preprocessor")) {
            strPreprocessor = properties.getProperty("Preprocessor");
        }
        if (properties.containsKey("MatcherLibrary")) {
            strMatcherLibrary = properties.getProperty("MatcherLibrary");
        }
        if (properties.containsKey("Loader")) {
            strLoader = properties.getProperty("Loader");
        }
        if (properties.containsKey("MappingRenderer")) {
            strMappingRenderer = properties.getProperty("MappingRenderer");
        }
        if (properties.containsKey("ContextRenderer")) {
            strContextRenderer = properties.getProperty("ContextRenderer");
        }
        if (properties.containsKey("Filter")) {
            strFilter = properties.getProperty("Filter");
        }
        if (properties.containsKey("TreeMatcher")) {
            strTreeMatcher = properties.getProperty("TreeMatcher");
        }
        if (properties.containsKey("ContextClass")) {
            strContext = properties.getProperty("ContextClass");
        }

        // initialize JWNL (this must be done before JWNL library can be used)
        try {
            JWNL.initialize(new FileInputStream(JWNLPropertiesPath));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        } catch (FileNotFoundException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }

        createComponents();
    }

    /**
     * Creates object for specific class.
     *
     * @param className the class name to create the object
     * @return object of corresponding class name
     */
    public static Object getClassForName(String className) {
        Object object = null;
        try {
            Class classDefinition = Class.forName(className);
            object = classDefinition.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * Creates list of object of classes.
     *
     * @param str       string of classes
     * @param separator string to separate the name of class
     * @return vector of objects of classes
     */
    private static Vector<Object> fromStringToVectorOfClasses(String str,
                                                              String separator) {
        Vector<Object> tmp = new Vector<Object>();
        for (StringTokenizer stringTokenizer = new StringTokenizer(str,
                separator); stringTokenizer.hasMoreTokens();) {
            tmp.add(getClassForName(stringTokenizer.nextToken()));
        }
        return tmp;
    }

    /**
     * Retains value in vector
     *
     * @param v     vector
     * @param value value
     */
    // TODO Need comments about the parameters.
    public static void retainValue(Vector<String> v, String value) {
        if (oneSensePerLabel) {
            Vector<String> toSave = new Vector<String>();
            toSave.add(value);
            v.retainAll(toSave);
        }
    }

    public static void printMemoryUsage() {
        Runtime tw = Runtime.getRuntime();
        log.debug((tw.totalMemory() - tw.freeMemory()) / 1024 + " Kb used");
    }

    public void preprocess(IContext context) {
        IMatchingContext imc = context.getMatchingContext();
        log.info("Computing concepts at label...");
        //preprocessors source context
        imc.resetOldPreprocessing();
        getPreprocessor().preprocess(context);
        log.info("Computing concepts at label finished");

        printMemoryUsage();
    }

    public void classify(IContext context) {
        log.info("Computing concepts at node...");
        classifier.buildCNodeFormulas(context);
        log.info("Computing concepts at node finished");

        printMemoryUsage();
    }

    public void renderContext(IContext ctxSource, String fileName) {
        contextRenderer.render(ctxSource, fileName);
    }

    public IMatchMatrix elementLevelMatching(IContext sourceContext,
                                             IContext targetContext) throws SMatchException {
        log.info("Element level matching...");
        IMatchMatrix ClabMatrix = getMatcherLibrary().elementLevelMatching(sourceContext, targetContext);
        log.info("Element level matching finished");

        printMemoryUsage();
        return ClabMatrix;
    }

    public IMatchMatrix structureLevelMatching(IContext sourceContext,
                                               IContext targetContext, IMatchMatrix ClabMatrix) throws SMatchException {
        log.info("Structure level matching...");
        IMatchMatrix CnodMatrix = treeMatcher.treeMatch(sourceContext, targetContext, ClabMatrix);
        log.info("Structure level matching finished");

        printMemoryUsage();
        return CnodMatrix;
    }

    public void renderMapping(IMapping mapping, String outputFile) throws SMatchException {
        mappingRenderer.render(mapping, outputFile);
    }

    public IMapping loadMapping(IContext ctxSource, IContext ctxTarget, String inputFile) throws SMatchException {
        return mappingLoader.loadMapping(ctxSource, ctxTarget, inputFile);
    }

    public void offline(IContext context) {
        log.info("Computing concept at label formulas...");
        preprocess(context);
        log.info("Computing concept at label formulas finished");

        log.info("Computing concept at node formulas...");
        classify(context);
        log.info("Computing concept at node formulas finished");
    }

    public IMapping online(IContext sourceContext, IContext targetContext) throws SMatchException {
        //TODO get rid of matrices?
        // Performs element level matching which computes the relation between labels.
        IMatchMatrix cLabMatrix = elementLevelMatching(sourceContext, targetContext);
        // Performs structure level matching which computes the relation between nodes.
        IMatchMatrix cNodeMatrix = structureLevelMatching(sourceContext, targetContext, cLabMatrix);

        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();

        IMapping mapping = new Mapping(sourceContext, targetContext);
        for (int i = 0; i < sourceNodes.size(); i++) {
            INode sourceNode = sourceNodes.get(i);
            for (int j = 0; j < targetNodes.size(); j++) {
                INode targetNode = targetNodes.get(j);
                char relation = cNodeMatrix.getElement(i , j);
                if (MatchManager.IDK_RELATION != relation) {
                    mapping.add(new MappingElement(sourceNode, targetNode, relation));
                }
            }
        }

        return mapping;
    }

    public IMapping match(IContext sourceContext, IContext targetContext) throws SMatchException {
        log.info("Matching started...");
        offline(sourceContext);
        offline(targetContext);
        IMapping result = online(sourceContext, targetContext);
        log.info("Matching finished");
        return result;
    }

    public IContext loadContext(String fileName) throws SMatchException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading nodes from " + fileName);
        }
        IContext context = loader.loadContext(fileName);
        //TODO move into sorting loader or renderer
        context.getContextData().sort();
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loaded nodes (" + fileName + "): " + context.getRoot().getDescendantCount());
        }
        return context;
    }

    public IMapping filterMapping(IMapping sourceMapping) {
        log.info("Filtering...");
        IMapping result = filter.filter(sourceMapping);
        log.info("Filtering finished");

        return result;
    }

    /**
     * Converts WordNet dictionary to binary format for fast searching.
     *
     * @throws SMatchException SMatchException
     */
    private void convertWordNetToFlat() throws SMatchException {
        InMemoryWordNetBinaryArray.createWordNetCaches();
    }

    /**
     * Provides a command line interface to match manager.
     *
     * @param args command line arguments
     * @throws SMatchException SMatchException
     */
    public static void main(String[] args) throws SMatchException {
        // initialize property file
        String propFile = DEFAULT_CONFIG_FILE_NAME;
        ArrayList<String> cleanArgs = new ArrayList<String>();
        for (String arg : args) {
            if (arg.startsWith(propFileCmdLineKey)) {
                propFile = arg.substring(propFileCmdLineKey.length());
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

            // read properties
            Properties config = new Properties();
            try {
                config.load(new FileInputStream(propFile));
                mm.setProperties(config);

                if ("wntoflat".equals(args[0])) {
                    mm.convertWordNetToFlat();
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
                        IMapping result = mm.online(ctxSource, ctxTarget);
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
                        IMapping mapInput = mm.loadMapping(ctxSource, ctxTarget, inputFile);
                        IMapping mapOutput = mm.filterMapping(mapInput);
                        mm.renderMapping(mapOutput, outputFile);
                    } else {
                        System.out.println("Not enough arguments for filter command.");
                    }
                } else {
                    System.out.println("Unrecognized command.");
                }
            } catch (IOException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("IOException: " + e.getMessage(), e);
                }
            }
        }
    }
}