package it.unitn.disi.smatch;

import it.unitn.disi.smatch.classifiers.IClassifier;
import it.unitn.disi.smatch.data.Context;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.IMatchingContext;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.deciders.openSAT;
import it.unitn.disi.smatch.deciders.openSATcached;
import it.unitn.disi.smatch.filters.IFilter;
import it.unitn.disi.smatch.loaders.ILoader;
import it.unitn.disi.smatch.loaders.IMappingLoader;
import it.unitn.disi.smatch.loaders.PlainMappingLoader;
import it.unitn.disi.smatch.matchers.element.IMatcherLibrary;
import it.unitn.disi.smatch.matchers.structure.tree.ITreeMatcher;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.IWordNetMatcher;
import it.unitn.disi.smatch.preprocessors.IPreprocessor;
import it.unitn.disi.smatch.renderers.context.IContextRenderer;
import it.unitn.disi.smatch.renderers.mapping.IMappingRenderer;
import it.unitn.disi.smatch.utils.GenerateWordNetCaches;
import it.unitn.disi.smatch.utils.SMatchUtils;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Class that controls the process of matching, loads contexts and performs other
 * auxiliary work. Also it contains all the global variables and properties from
 * the configuration file.
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
     * Which properties file is used for setting configuration. <br>
     * The value of the variables change through command line parameter.
     */
    public static String propFileName = "SMatch.properties";

    // Settings from the properties file
    // whether to use element level semantic matchers library or exploit the
    // only WordNet
    public static boolean useWeakSemanticsElementLevelMatchersLibrary = true;
    // html file to print the matching results
    private static String outputFile = "..\\test\\result.htm";

    //mapping file for reading mapping
    private static String mappingFile = "..\\test\\result.txt";

    // the path to JWNL library properties file
    private static String JWNLpropertiesPath = "..\\conf\\file_properties.xml";
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
    // TODO Need comment.
    private static boolean oneSensePerLabel = true;
    // default value for the class which implements IWordNetMatcher interface
    private static String strWNmatcher = null;// "it.unitn.disi.smatch.oracles.wordnet.DefaultWordNetMatcher";
    // default value for the class which implements ILinguisticOracle interface
    private static String strLinguisticOracle = null;// "it.unitn.disi.smatch.oracles.wordnet.WordNet";
    // default value for the class which implements IClassifier interface
    private static String strClassifier = null;// "it.unitn.disi.smatch.classifiers.DefaultClassifier";
    // default value for the class which implements IPreprocessor interface
    private static String strPreprocessor = null;// "it.unitn.disi.smatch.preprocessors.DefaultPreprocessor";
    // default value for the class which implements IMatcherLibrary interface
    private static String strMatcherLibrary = null;// "it.unitn.disi.smatch.matchers.element.MatcherLibrary";
    // //add New
    // default value for the class which implements ILoader interface
    private static String strLoader = null;// "it.unitn.disi.smatch.loaders.CTXMLLoader";
    // default value for the class which implements IMappingRenderer interface
    private static String strMappingRenderer = null;// "it.unitn.disi.smatch.renderers.mapping.TaxMEMappingRenderer";
    // default value for the class which implements IContextRenderer interface
    private static String strContextRenderer = null;// "it.unitn.disi.smatch.renderers.context.CTXMLContextRenderer";
    // default value for the class which implements IFilter interface
    private static String strFilter = null;// "it.unitn.disi.smatch.filters.ZeroFilter";
    // default value for the class which implements IContext interface
    private static String strContext = null;// "it.unitn.disi.smatch.data.Context";
    // default value for the class which implements ITreeMatcher interface
    private static String strTreeMatcher = null;// "it.unitn.disi.smatch.matchers.structure.tree.DefaultTreeMatcher";

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

    // Default files for matching
    public static String ctxsSourceFile;
    public static String ctxsTargetFile;
    // "Wrong usage" string
    private static final String USAGE = "it.unitn.disi.smatch <preprocessors> source_dir target_dir";
    // Whether preprocessors part should be executed
    private static boolean offline = false; // Computing concept of labels and nodes.
    private static boolean online = false; // Computing relation between concept of labels and nodes. Also compute minimal result.
    private static boolean convert = false; // Convert input file to xml formated file.
    private static boolean filterFlag = false; // filter the semantic relation for minimal result.
    private static boolean wntoflatFlag = false; // Convert WordNet to binary caches for quick search.

    // Wordnet dictionary object
    private static Dictionary wordNetDictionary = null;
    // WordNet matcher interface
    private static IWordNetMatcher WNMatcher = null;
    // Linguistic Oracle interface
    private static ILinguisticOracle linguisticOracle = null;
    // Classification engine interface
    private static IClassifier classifier = null;
    // Linguistic Oracle interface
    private static IPreprocessor preprocessor = null;
    // Matcher Library interface
    private static IMatcherLibrary matcherLibrary = null;
    // Loader interface
    private static ILoader loader = null;
    // Mapping renderer interface
    private static IMappingRenderer mappingRenderer = null;
    // Context renderer interface
    private static IContextRenderer contextRenderer = null;
    // Filter interface
    private static IFilter filter = null;
    // Tree matcher interface
    private static ITreeMatcher treeMatcher = null;

    //mapping loader
    private static IMappingLoader mappingLoader = null;

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

    /**
     * Parses the settings form properties files and sets the property key to specific variable.
     *
     * @param properties the object of the properties files
     */
    private void parseProperties(Properties properties) {
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
        if (properties.containsKey("outputFile")) {
            outputFile = properties.getProperty("outputFile");
        }
        if (properties.containsKey("JWNLpropertiesPath")) {
            JWNLpropertiesPath = properties.getProperty("JWNLpropertiesPath");
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
    }

    public static String getOutputFile() {
		return outputFile;
	}

	public static void setOutputFile(String outputFile) {
		MatchManager.outputFile = outputFile;
	}

	// buffer size for input, output operations.
    public static int BUFFER_SIZE = 5000000;
    // ELSMthreshold for element level semantic matchers.
    // Edit Distance, Optimized edit distance, NGram matchers are controlled by this value.
    public static double ELSMthreshold = 0.9;
    // Number characters for linguistic preprocessing.
    public static String numberCharacters = "1234567890_ .,|\\/-";

    public MatchManager() throws SMatchException {
        this(propFileName);
    }

    /**
     * Constructor class to load properties files, initialize JWNL and creating components.
     *
     * @param propFileName the name of the properties file
     * @throws SMatchException
     */
    public MatchManager(String propFileName) throws SMatchException {
        // load properties
        loadProperties(propFileName);
        // initialize JWNL (this must be done before JWNL library can be used)
        initJWNL();
        // create components
        createComponents();
    }

    public static IMatchManager getInstance() throws SMatchException {
        return new MatchManager(propFileName);
    }

    /**
     * Configures the components such as classifier, loader etc for the program.
     */
    private static void createComponents() {
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

    public static void setSatSolver(String satSolverClass) {
        MatchManager.satSolverClass = satSolverClass;
    }

    public static void setWNMatcher(String WNMatcher) {
        strWNmatcher = WNMatcher;
        MatchManager.WNMatcher = (IWordNetMatcher) getClassForName(strWNmatcher);
    }

    public static void setLinguisticOracle(String linguisticOracle) {
        strLinguisticOracle = linguisticOracle;
        MatchManager.linguisticOracle = (ILinguisticOracle) getClassForName(strLinguisticOracle);
    }

    public static void setClassifier(String classifier) {
        strClassifier = classifier;
        MatchManager.classifier = (IClassifier) getClassForName(strClassifier);
    }

    public static void setPreprocessor(String preprocessor) {
        strPreprocessor = preprocessor;
        MatchManager.preprocessor = (IPreprocessor) getClassForName(strPreprocessor);
    }

    public static void setMatcherLibrary(String matcherLibrary) {
        strMatcherLibrary = matcherLibrary;
        MatchManager.matcherLibrary = (IMatcherLibrary) getClassForName(strMatcherLibrary);
    }

    public static void setLoader(String loader) {
        strLoader = loader;
        MatchManager.loader = (ILoader) getClassForName(strLoader);
    }

    public static void setMappingRenderer(String mappingRenderer) {
        strMappingRenderer = mappingRenderer;
        MatchManager.mappingRenderer = (IMappingRenderer) getClassForName(strMappingRenderer);
    }

    public static void setContextRenderer(String contextRenderer) {
        strContextRenderer = contextRenderer;
        MatchManager.contextRenderer = (IContextRenderer) getClassForName(strContextRenderer);
    }

    public static void setFilter(String filter) {
        strFilter = filter;
        MatchManager.filter = (IFilter) getClassForName(strFilter);
    }

    public static void setTreeMatcher(String treeMatcher) {
        strTreeMatcher = treeMatcher;
        MatchManager.treeMatcher = (ITreeMatcher) getClassForName(strTreeMatcher);
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

    public static IContext getIContext() {
        return Context.getInstance();
    }

    /**
     * Loads, parses, sets the value of properties from properties file.
     *
     * @param filename the properties file name
     * @throws SMatchException
     */
    private void loadProperties(String filename) throws SMatchException {
        log.info("Loading properties from " + filename);
        try {
            Properties properties = new Properties();
            // Loads the value of properties to properties object.
            properties.load(new FileInputStream(filename));
            // Sets the value of properties from properties files.
            parseProperties(properties);
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    public void setProperties(Properties properites) {
        parseProperties(properites);
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
     * @param str string of classes
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

    /**
     * Performs JWNL and JWNL logger initialization routines.
     * Needs to be performed once before matching process.
     *
     * @throws SMatchException
     */
    static public void initJWNL() throws SMatchException {
        try {
            FileInputStream fis = new FileInputStream(JWNLpropertiesPath);
            JWNL.initialize(fis);
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        } catch (FileNotFoundException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    public static void printMemoryUsage() {
        Runtime tw = Runtime.getRuntime();
        log.debug((tw.totalMemory() - tw.freeMemory()) / 1024 + " Kb used");
    }

    /**
     * Parses the commands from command line.
     *
     * @param args command line arguments
     */
    private void parseCommandPromtParameters(String[] args) {
        // Is everything right with input parameters
        if (args.length < 2) {
            log.info(USAGE);
            System.exit(-1);
        }
        if (args[0].indexOf("wntoflat") > -1) {
            wntoflatFlag = true;
        } else if (args[0].indexOf("filter") > -1) {
            filterFlag = true;
            ctxsSourceFile = args[1];
            ctxsTargetFile = args[2];
            if (args.length >= 4) {
                mappingFile = args[3];
            }
        } else if (args[0].indexOf("convert") > -1) {
            convert = true;
            ctxsSourceFile = args[1];
            outputFile = args[2];
        } else {
            // Whether preprocessors part should be executed
            if (args[0].indexOf("offline") > -1) {
                offline = true;
            } else if (args[0].indexOf("online") > -1) {
                online = true;
            }

            // directories of files to online
            ctxsSourceFile = args[1];
            if (2 < args.length) {
                if (!args[2].startsWith("-")) {
                    ctxsTargetFile = args[2];
                }
            }
        }
    }

    public IContext preprocess(IContext ctxSource) {
        IMatchingContext imc = ctxSource.getMatchingContext();
        log.info("Computing concepts at label...");
        //preprocessors source context
        imc.resetOldPreprocessing();
        ctxSource = getPreprocessor().preprocess(ctxSource);
        log.info("Computing concepts at label finished");

        printMemoryUsage();
        return ctxSource;
    }

    public IContext classify(IContext ctxSource) {
        log.info("Computing concepts at node...");
        ctxSource = classifier.buildCNodeFormulas(ctxSource);
        log.info("Computing concepts at node finished");

        printMemoryUsage();
        return ctxSource;
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

    public IMatchMatrix filter(Vector args) {
        log.info("Filtering...");
        IMatchMatrix CnodMatrix = filter.filter(args);
        log.info("Filtering finished");

        printMemoryUsage();
        return CnodMatrix;
    }

    public IMapping renderMapping(Vector args) {
        log.info("Rendering results to " + args.get(0));
        IMapping res = mappingRenderer.render(args);
        log.info("Rendering results finished");

        printMemoryUsage();
        return res;
    }

    public IContext offline(IContext ctxSource, String ctxsSourceFile) {
        log.info("Computing concept at label formulas...");
        IContext tmp = preprocess(ctxSource);
        log.info("Computing concept at label formulas finished");

        log.info("Computing concept at node formulas...");
        tmp = classify(tmp);
        log.info("Computing concept at node formulas finished");

        if (null != ctxsSourceFile) {
            renderContext(tmp, ctxsSourceFile);
        }
        return tmp;
    }

    public IMapping online(IContext sourceContext, IContext targetContext) throws SMatchException {
        // Performs element level matching which computes the relation between labels.
    	IMatchMatrix cLabMatrix = elementLevelMatching(sourceContext, targetContext);
    	// Performs structure level matching which computes the relation between nodes.
        IMatchMatrix cNodeMatrix = structureLevelMatching(sourceContext, targetContext, cLabMatrix);

        //temporary
        openSAT.reportStats();
        openSATcached.reportStats();

        Vector args = new Vector();
        args.insertElementAt(outputFile, 0);
        args.insertElementAt(cNodeMatrix, 1);
        args.insertElementAt(cLabMatrix, 2);
        args.insertElementAt(sourceContext, 3);
        args.insertElementAt(targetContext, 4);
        // Filters the relational matrix for minimal mapping.
        cNodeMatrix = filter(args);

        return renderMapping(args);
    }

    public IMapping match(IContext sourceContext, IContext targetContext) throws SMatchException {
        sourceContext = offline(sourceContext, null);
        targetContext = offline(targetContext, null);
        return online(sourceContext, targetContext);

    }

    public IContext loadContext(String fileName) throws SMatchException {
        log.info("Loading nodes from " + fileName);
        IContext context = loader.loadContext(fileName);
        //TODO move into sorting loader
        context.getContextData().sort();
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loaded nodes (" + fileName + "): " + context.getRoot().getDescendantCount());
        }
        return context;
    }

    /**
     * Filtering contexts for minimal mapping.
     *
     * @param sourceContext concept of source nodes
     * @param targetContext concept of target nodes
     */
    private void filterContexts(IContext ctxSource, IContext ctxTarget) {
        IMatchMatrix cNodeMatrix;
        try {
            cNodeMatrix = mappingLoader.loadMapping(ctxSource, ctxTarget, mappingFile);

            Vector argsV = new Vector();
            argsV.insertElementAt(outputFile, 0);
            argsV.insertElementAt(cNodeMatrix, 1);
            argsV.insertElementAt(null, 2);
            argsV.insertElementAt(ctxSource, 3);
            argsV.insertElementAt(ctxTarget, 4);

            cNodeMatrix = filter(argsV);
            renderMapping(argsV);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts WordNet dictionary to binary format for fast searching.
     *
     * @throws SMatchException
     */
    private void convertWordNetToFlat() throws SMatchException {
        GenerateWordNetCaches gwnc = new GenerateWordNetCaches();
        gwnc.convert();
    }

    public static void main(String[] args) throws SMatchException {
        IContext ctxSource;
        IContext ctxTarget = null;
        final String propKey = "-prop=";
        for (String arg : args) {
            if (arg.startsWith(propKey)) {
                propFileName = arg.substring(propKey.length());
                break;
            }
        }
        MatchManager mm = new MatchManager(propFileName);
        mm.parseCommandPromtParameters(args);

        if (wntoflatFlag) {
            mm.convertWordNetToFlat();
        } else {
            if (convert) {
                ctxSource = mm.loadContext(ctxsSourceFile);
            } else {
                ctxSource = mm.loadContext(ctxsSourceFile);
                if (null != ctxsTargetFile) {
                    ctxTarget = mm.loadContext(ctxsTargetFile);
                }
            }
            printMemoryUsage();
            if (convert) {
                mm.renderContext(ctxSource, outputFile);
            } else {
                if (filterFlag) {
                    mm.filterContexts(ctxSource, ctxTarget);
                } else {
                    if (offline) {
                        mm.offline(ctxSource, ctxsSourceFile);
                        if (null != ctxTarget) {
                            mm.offline(ctxTarget, ctxsTargetFile);
                        }
                        //mm.online(ctxSource, ctxTarget);
                    } else {
                        mm.online(ctxSource, ctxTarget);
                    }
                }
            }
        }
    }
}