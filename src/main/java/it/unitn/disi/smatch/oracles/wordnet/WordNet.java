package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.common.DISIException;
import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.common.utils.MiscUtils;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import it.unitn.disi.smatch.oracles.SenseMatcherException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Implements a Linguistic Oracle and Sense Matcher using WordNet.
 * <p/>
 * Needs  JWNLPropertiesPath string parameter which should point to a JWNL configuration file.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class WordNet extends Configurable implements ILinguisticOracle, ISenseMatcher {

    private static final Logger log = Logger.getLogger(WordNet.class);

    private static final String JWNL_PROPERTIES_PATH_KEY = "JWNLPropertiesPath";
    private static final String USE_INTERNAL_FILES = "UseInternalFiles";
    private static final String DEFAULT_FILE_DICTIONARY_PATH = "../data/";
    
    private Dictionary dic = null;

    // controls loading of arrays, used to skip loading before conversion
    private static final String LOAD_ARRAYS_KEY = "loadArrays";

    // contains all the multiwords in WordNet
    private static final String MULTIWORDS_FILE_KEY = "multiwordsFileName";
    private HashMap<String, ArrayList<ArrayList<String>>> multiwords = null;

    private static final Pattern offset = Pattern.compile("\\d+");

    private Map<String, Character> sensesCache;

    public WordNet() {
        sensesCache = new HashMap<String, Character>();
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = super.setProperties(newProperties);
        if (result) {
            boolean loadArrays = true;
            if (newProperties.containsKey(LOAD_ARRAYS_KEY)) {
                loadArrays = Boolean.parseBoolean(newProperties.getProperty(LOAD_ARRAYS_KEY));
            }
            
            boolean useInternalFiles = true;
            
            if (newProperties.containsKey(USE_INTERNAL_FILES)) {
                useInternalFiles = Boolean.parseBoolean(newProperties.getProperty(USE_INTERNAL_FILES));
            }

            if (newProperties.containsKey(JWNL_PROPERTIES_PATH_KEY)) {
                // initialize JWNL (this must be done before JWNL library can be used)
                try {
                    final String configPath = newProperties.getProperty(JWNL_PROPERTIES_PATH_KEY);
                    log.info("Initializing JWNL from " + configPath);

                    if (useInternalFiles) {
                        log.info("Using internal files.");

                        InputStream propertiesStream = Thread.currentThread().getContextClassLoader().getResource(configPath).openStream();
                        String dictionaryPath = Thread.currentThread().getContextClassLoader().getResource("data").toString();
                        dictionaryPath = dictionaryPath.replace("file:/", "");
                        dictionaryPath = "/" + dictionaryPath + "/";
                        log.info("dictionaryPath: " + dictionaryPath);
                        String propertiesFromStream = getTextFromStream(propertiesStream);
                        propertiesFromStream = propertiesFromStream.replace(DEFAULT_FILE_DICTIONARY_PATH, dictionaryPath);
                        InputStream is = new ByteArrayInputStream(propertiesFromStream.getBytes());
                        JWNL.initialize(is);
                    } else {
                        JWNL.initialize(new FileInputStream(configPath));
                    }
                    dic = Dictionary.getInstance();
                } catch (JWNLException e) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    throw new ConfigurableException(errMessage, e);
                } catch (FileNotFoundException e) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    throw new ConfigurableException(errMessage, e);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(WordNet.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                final String errMessage = "Cannot find configuration key " + JWNL_PROPERTIES_PATH_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            if (newProperties.containsKey(MULTIWORDS_FILE_KEY)) {
                if (loadArrays) {
                    String multiwordFileName = newProperties.getProperty(MULTIWORDS_FILE_KEY);
                    log.info("Loading multiwords: " + multiwordFileName);
                    multiwords = readHash(multiwordFileName, useInternalFiles);
                    log.info("loaded multiwords: " + multiwords.size());
                } else {
                    multiwords = new HashMap<String, ArrayList<ArrayList<String>>>();
                }
            } else {
                final String errMessage = "Cannot find configuration key " + MULTIWORDS_FILE_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

        }
        return result;
    }

    public List<ISense> getSenses(String label) throws LinguisticOracleException {
        List<ISense> result = new ArrayList<ISense>();
        try {
            IndexWordSet lemmas = dic.lookupAllIndexWords(label);
            if (null != lemmas && 0 < lemmas.size()) {
                //Looping on all words in indexWordSet
                for (int i = 0; i < lemmas.getIndexWordArray().length; i++) {
                    IndexWord lemma = lemmas.getIndexWordArray()[i];
                    for (int j = 0; j < lemma.getSenses().size(); j++) {
                        Synset synset = lemma.getSenses().get(j);
                        result.add(new WordNetSense(synset));
                    }
                }
            }
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new LinguisticOracleException(errMessage, e);
        }
        return result;
    }

    public List<String> getBaseForms(String derivation) throws LinguisticOracleException {
        try {
            List<String> result = new ArrayList<String>();
            IndexWordSet tmp = dic.lookupAllIndexWords(derivation);
            if (null != tmp) {
                IndexWord[] indexWordArray = tmp.getIndexWordArray();
                for (IndexWord indexWord : indexWordArray) {
                    String lemma = indexWord.getLemma();
                    if (null != lemma && !result.contains(lemma)) {
                        result.add(lemma);
                    }
                }
            } else {
                if (null != dic.getMorphologicalProcessor()) {
                    for (POS pos : POS.values()) {
                        List<String> posLemmas = dic.getMorphologicalProcessor().lookupAllBaseForms(pos, derivation);
                        for (String lemma : posLemmas) {
                            if (!result.contains(lemma)) {
                                result.add(lemma);
                            }
                        }
                    }
                }
            }
            if (0 == result.size()) {
                result.add(derivation);
            }
            return result;
        } catch (JWNLException e) {
            //TODO fix "log and throw" everywhere
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new LinguisticOracleException(errMessage, e);
        }
    }

    public boolean isEqual(String str1, String str2) throws LinguisticOracleException {
        try {
            IndexWordSet lemmas1 = dic.lookupAllIndexWords(str1);
            IndexWordSet lemmas2 = dic.lookupAllIndexWords(str2);
            if ((lemmas1 == null) || (lemmas2 == null) || (lemmas1.size() < 1) || (lemmas2.size() < 1)) {
                return false;
            } else {
                IndexWord[] v1 = lemmas1.getIndexWordArray();
                IndexWord[] v2 = lemmas2.getIndexWordArray();
                for (IndexWord aV1 : v1) {
                    for (IndexWord aV2 : v2) {
                        if (aV1.equals(aV2)) {
                            return true;
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new LinguisticOracleException(errMessage, e);
        }
        return false;
    }

    public char getRelation(List<ISense> sourceSenses, List<ISense> targetSenses) throws SenseMatcherException {
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.EQUIVALENCE)) {
                    return IMappingElement.EQUIVALENCE;
                }
            }
        }
        //  Check for less general than
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.LESS_GENERAL)) {
                    return IMappingElement.LESS_GENERAL;
                }
            }
        }
        //  Check for more general than
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.MORE_GENERAL)) {
                    return IMappingElement.MORE_GENERAL;
                }
            }
        }
        //  Check for opposite meaning
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.DISJOINT)) {
                    return IMappingElement.DISJOINT;
                }
            }
        }
        return IMappingElement.IDK;
    }

    /**
     * Method which returns whether particular type of relation between
     * two senses holds(according to oracle).
     * It uses cache to store already obtained relations in order to improve performance.
     *
     * @param source the string of source
     * @param target the string of target
     * @param rel    the relation between source and target
     * @return whether particular type of relation holds between two senses according to oracle
     * @throws SenseMatcherException SenseMatcherException
     */
    private boolean getRelationFromOracle(ISense source, ISense target, char rel) throws SenseMatcherException {
        final String sensePairKey = source.toString() + target.toString();
        Character cachedRelation = sensesCache.get(sensePairKey);
        // if we don't have cached relation check which one exist and put it to cash
        if (null == cachedRelation) {
            // check for synonymy
            if (isSourceSynonymTarget(source, target)) {
                sensesCache.put(sensePairKey, IMappingElement.EQUIVALENCE);
                return rel == IMappingElement.EQUIVALENCE;
            } else {
                // check for opposite meaning
                if (isSourceOppositeToTarget(source, target)) {
                    sensesCache.put(sensePairKey, IMappingElement.DISJOINT);
                    return rel == IMappingElement.DISJOINT;
                } else {
                    // check for less general than
                    if (isSourceLessGeneralThanTarget(source, target)) {
                        sensesCache.put(sensePairKey, IMappingElement.LESS_GENERAL);
                        return rel == IMappingElement.LESS_GENERAL;
                    } else {
                        // check for more general than
                        if (isSourceMoreGeneralThanTarget(source, target)) {
                            sensesCache.put(sensePairKey, IMappingElement.MORE_GENERAL);
                            return rel == IMappingElement.MORE_GENERAL;
                        } else {
                            sensesCache.put(sensePairKey, IMappingElement.IDK);
                            return false;
                        }
                    }
                }
            }
        } else {
            return rel == cachedRelation;
        }
    }

    public boolean isSourceSynonymTarget(ISense source, ISense target) throws SenseMatcherException {
        if (source.equals(target)) {
            return true;
        }
        if ((source instanceof WordNetSense) && (target instanceof WordNetSense)) {
            try {
                WordNetSense sourceSyn = (WordNetSense) source;
                WordNetSense targetSyn = (WordNetSense) target;
                //is synonym
                RelationshipList list = RelationshipFinder.findRelationships(sourceSyn.getSynset(), targetSyn.getSynset(), PointerType.SIMILAR_TO);
                if (list.size() > 0) {
                    return !((POS.ADJECTIVE == sourceSyn.getPOS()) || (POS.ADJECTIVE == targetSyn.getPOS())) || (list.get(0).getDepth() == 0);
                }
            } catch (CloneNotSupportedException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SenseMatcherException(errMessage, e);
            }
        }
        return false;
    }

    public boolean isSourceOppositeToTarget(ISense source, ISense target) throws SenseMatcherException {
        if (source.equals(target)) {
            return false;
        }
        if ((source instanceof WordNetSense) && (target instanceof WordNetSense)) {
            try {
                WordNetSense sourceSyn = (WordNetSense) source;
                WordNetSense targetSyn = (WordNetSense) target;
                //  Checks whether senses are siblings (thus they are opposite)
                if (POS.NOUN == sourceSyn.getPOS() && POS.NOUN == targetSyn.getPOS()) {
                } else {
                    RelationshipList list = RelationshipFinder.findRelationships(sourceSyn.getSynset(), targetSyn.getSynset(), PointerType.ANTONYM);
                    if (list.size() > 0) {
                        return true;
                    }
                }
            } catch (CloneNotSupportedException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SenseMatcherException(errMessage, e);
            }
        }
        return false;
    }

    /**
     * Checks whether source sense less general than target.
     * Currently used version of Java WordNet Interface Library finds more general relationships
     * (hypernymy and holonymy) faster than less general so this method
     * just flip the parameters and call isSourceMoreGeneralThanTarget method.
     *
     * @param source the string of source
     * @param target the string of target
     * @return true if source is less general than target
     */
    public boolean isSourceLessGeneralThanTarget(ISense source, ISense target) throws SenseMatcherException {
        return isSourceMoreGeneralThanTarget(target, source);
    }

    public boolean isSourceMoreGeneralThanTarget(ISense source, ISense target) throws SenseMatcherException {
        if ((source instanceof WordNetSense) && (target instanceof WordNetSense)) {
            WordNetSense sourceSyn = (WordNetSense) source;
            WordNetSense targetSyn = (WordNetSense) target;

            if ((POS.NOUN == sourceSyn.getPOS() && POS.NOUN == targetSyn.getPOS()) || (POS.VERB == sourceSyn.getPOS() && POS.VERB == targetSyn.getPOS())) {
                if (source.equals(target)) {
                    return false;
                }
                try {
                    // find all more general relationships from WordNet
                    RelationshipList list = RelationshipFinder.findRelationships(sourceSyn.getSynset(), targetSyn.getSynset(), PointerType.HYPERNYM);
                    if (!isUnidirestionalList(list)) {
                        PointerTargetTree ptt = PointerUtils.getInheritedMemberHolonyms(targetSyn.getSynset());
                        PointerTargetNodeList ptnl = PointerUtils.getMemberHolonyms(targetSyn.getSynset());
                        if (!traverseTree(ptt, ptnl, sourceSyn.getSynset())) {
                            ptt = PointerUtils.getInheritedPartHolonyms(targetSyn.getSynset());
                            ptnl = PointerUtils.getPartHolonyms(targetSyn.getSynset());
                            if (!traverseTree(ptt, ptnl, sourceSyn.getSynset())) {
                                ptt = PointerUtils.getInheritedSubstanceHolonyms(targetSyn.getSynset());
                                ptnl = PointerUtils.getSubstanceHolonyms(targetSyn.getSynset());
                                if (traverseTree(ptt, ptnl, sourceSyn.getSynset())) {
                                    return true;
                                }
                            } else {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    } else {
                        return true;
                    }
                } catch (CloneNotSupportedException e) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    throw new SenseMatcherException(errMessage, e);
                }
            }
        }
        return false;
    }

    public ISense createSense(String id) throws LinguisticOracleException {
        if (id.length() < 3 || 1 != id.indexOf('#') || !"navr".contains(id.substring(0, 1)) || !offset.matcher(id.substring(2)).matches()) {
            throw new LinguisticOracleException("Malformed sense id: " + id);
        }
        try {
            Synset synset = dic.getSynsetAt(POS.getPOSForKey(id.substring(0, 1)), Long.parseLong(id.substring(2)));
            return new WordNetSense(synset);
        } catch (JWNLException e) {
            throw new LinguisticOracleException(e.getMessage(), e);
        }
    }

    public ArrayList<ArrayList<String>> getMultiwords(String beginning) throws LinguisticOracleException {
        return multiwords.get(beginning);
    }

    /**
     * traverses PointerTargetTree.
     *
     * @param syn    synonyms
     * @param ptnl   target node list
     * @param source source synset
     * @return if source was found
     */
    private static boolean traverseTree(PointerTargetTree syn, PointerTargetNodeList ptnl, Synset source) {
        java.util.List MGListsList = syn.toList();
        for (Object aMGListsList : MGListsList) {
            PointerTargetNodeList MGList = (PointerTargetNodeList) aMGListsList;
            for (Object aMGList : MGList) {
                Synset toAdd = ((PointerTargetNode) aMGList).getSynset();
                if (toAdd.equals(source)) {
                    return true;
                }
            }
        }
        for (Object aPtnl : ptnl) {
            Synset toAdd = ((PointerTargetNode) aPtnl).getSynset();
            if (toAdd.equals(source)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks unidirectionality of semantic relations in the list.
     *
     * @param list a list with relations
     * @return true if relations in the list are unidirectional
     */
    private boolean isUnidirestionalList(RelationshipList list) {
        if (list.size() > 0) {
            try {
                if (((AsymmetricRelationship) list.get(0)).getCommonParentIndex() == 0) {
                    return true;
                }
            } catch (java.lang.IndexOutOfBoundsException ex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the hashmap with multiwords. The multiwords are stored in the following format:
     * Key - the first word in the multiwords
     * Value - List of Lists, which contain the other words in the all the multiwords starting with key.
     *
     * @param fileName the file name from which the hashmap will be read
     * @parm isInternalFile reads from internal data file in resources folder
     * @return multiwords hashmap
     * @throws it.unitn.disi.smatch.SMatchException SMatchException
     */
    @SuppressWarnings("unchecked")
    private static HashMap<String, ArrayList<ArrayList<String>>> readHash(String fileName, boolean isInternalFile) throws SMatchException {
        try {
            return (HashMap<String, ArrayList<ArrayList<String>>>) MiscUtils.readObject(fileName, isInternalFile);
        } catch (DISIException e) {
            throw new SMatchException(e.getMessage(), e);
        }
    }

    /**
     * Create caches of WordNet to speed up matching.
     *
     * @param componentKey a key to the component in the configuration
     * @param properties   configuration
     * @throws SMatchException SMatchException
     */
    public static void createWordNetCaches(String componentKey, Properties properties) throws SMatchException {
        properties = getComponentProperties(makeComponentPrefix(componentKey, WordNet.class.getSimpleName()), properties);
        if (properties.containsKey(JWNL_PROPERTIES_PATH_KEY)) {
            // initialize JWNL (this must be done before JWNL library can be used)
            try {
                final String configPath = properties.getProperty(JWNL_PROPERTIES_PATH_KEY);
                log.info("Initializing JWNL from " + configPath);

                boolean useInternalFiles = true;

                if (properties.containsKey(USE_INTERNAL_FILES)) {
                    useInternalFiles = Boolean.parseBoolean(properties.getProperty(USE_INTERNAL_FILES));
                }

                if (useInternalFiles == true) {
                    log.info("Using internal files.");

                    InputStream propertiesStream = Thread.currentThread().getContextClassLoader().getResource(configPath).openStream();
                    String dictionaryPath = Thread.currentThread().getContextClassLoader().getResource("data").toString();
                    dictionaryPath = dictionaryPath.replace("file:/", "");
                    dictionaryPath = "/" + dictionaryPath + "/";
                    log.info("dictionaryPath: " + dictionaryPath);
                    String propertiesFromStream = getTextFromStream(propertiesStream);
                    propertiesFromStream = propertiesFromStream.replace(DEFAULT_FILE_DICTIONARY_PATH, dictionaryPath);
                    InputStream is = new ByteArrayInputStream(propertiesFromStream.getBytes());
                    JWNL.initialize(is);
                } else {
                    JWNL.initialize(new FileInputStream(configPath));
                }
                log.info("Creating WordNet caches...");
                writeMultiwords(properties);
                log.info("Done");
            } catch (JWNLException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            } catch (FileNotFoundException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(WordNet.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            final String errMessage = "Cannot find configuration key " + JWNL_PROPERTIES_PATH_KEY;
            log.error(errMessage);
            throw new SMatchException(errMessage);
        }
    }

    private static void writeMultiwords(Properties properties) throws SMatchException {
        log.info("Creating multiword hash...");
        HashMap<String, ArrayList<ArrayList<String>>> multiwords = new HashMap<String, ArrayList<ArrayList<String>>>();
        POS[] parts = new POS[]{POS.NOUN, POS.ADJECTIVE, POS.VERB, POS.ADVERB};
        for (POS pos : parts) {
            collectMultiwords(multiwords, pos);
        }
        log.info("Multiwords: " + multiwords.size());
        try {
            MiscUtils.writeObject(multiwords, properties.getProperty(MULTIWORDS_FILE_KEY));
        } catch (DISIException e) {
            throw new SMatchException(e.getMessage(), e);
        }
    }

    private static void collectMultiwords(HashMap<String, ArrayList<ArrayList<String>>> multiwords, POS pos) throws SMatchException {
        try {
            int count = 0;
            Iterator i = net.sf.extjwnl.dictionary.Dictionary.getInstance().getIndexWordIterator(pos);
            while (i.hasNext()) {
                IndexWord iw = (IndexWord) i.next();
                String lemma = iw.getLemma();
                if (-1 < lemma.indexOf(' ')) {
                    count++;
                    if (0 == count % 10000) {
                        log.info(count);
                    }
                    String[] tokens = lemma.split(" ");
                    ArrayList<ArrayList<String>> mwEnds = multiwords.get(tokens[0]);
                    if (null == mwEnds) {
                        mwEnds = new ArrayList<ArrayList<String>>();
                    }
                    ArrayList<String> currentMWEnd = new ArrayList<String>(Arrays.asList(tokens));
                    currentMWEnd.remove(0);
                    mwEnds.add(currentMWEnd);
                    multiwords.put(tokens[0], mwEnds);
                }
            }
            log.info(pos.getKey() + " multiwords: " + count);
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }
    
    private static String getTextFromStream(InputStream inputStream) throws IOException {
        BufferedReader fileCheck;
        fileCheck = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder fileText = new StringBuilder();
        String line;
        while (null != (line = fileCheck.readLine())) {
            fileText.append(line).append("\n");
        }
        try {
            fileCheck.close();
        } catch (IOException e) {
            // doesn't matter.
        }
        return fileText.toString();
    }
}