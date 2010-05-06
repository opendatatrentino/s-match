package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.ISynset;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Implements a Linguistic Oracle and Sense Matcher using WordNet.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WordNet extends Configurable implements ILinguisticOracle, ISenseMatcher {

    private static final Logger log = Logger.getLogger(WordNet.class);

    private static final String JWNL_PROPERTIES_PATH_KEY = "JWNLPropertiesPath";
    private Dictionary dic = null;

    private Map<String, Character> sensesCache;

    public WordNet() {
        sensesCache = new HashMap<String, Character>();
    }

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            if (newProperties.containsKey(JWNL_PROPERTIES_PATH_KEY)) {
                // initialize JWNL (this must be done before JWNL library can be used)
                try {
                    final String configPath = newProperties.getProperty(JWNL_PROPERTIES_PATH_KEY);
                    log.info("Initializing JWNL from " + configPath);
                    JWNL.initialize(new FileInputStream(configPath));
                    dic = Dictionary.getInstance();
                } catch (JWNLException e) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    throw new ConfigurableException(errMessage, e);
                } catch (FileNotFoundException e) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    throw new ConfigurableException(errMessage, e);
                }
            } else {
                final String errMessage = "Cannot find configuration key " + JWNL_PROPERTIES_PATH_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }

        properties = newProperties;
    }

    public Vector<String> getSenses(String label) {
        IndexWordSet lemmas = null;
        //TODO remove when Thing is a top
        try {
            lemmas = dic.lookupAllIndexWords(label);
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            //throw new SMatchException(errMessage, e);
        }
        if (null != lemmas && 0 < lemmas.size()) {
            Vector<String> tmpSense = new Vector<String>();
            IndexWord lemma;
            Synset synset;
            String synsetId;
            try {
                //Looping on all words in indexWordSet
                for (int i = 0; i < lemmas.getIndexWordArray().length; i++) {
                    lemma = lemmas.getIndexWordArray()[i];
                    for (int j = 0; j < lemma.getSenseCount(); j++) {
                        synset = lemma.getSenses()[j];
                        //Forming the sense string
                        //TODO: cut as experimental heuristic
                        //if ((synset.getPOS().getKey().equals("n"))||(synset.getPOS().getKey().equals("a"))){
                        synsetId = synset.getPOS().getKey() + "#" + synset.getOffset();
                        tmpSense.add(synsetId);
                        //}
                    }
                }
            } catch (Exception e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                //throw new SMatchException(errMessage, e);
            }
            return tmpSense;
        } else {
            return null;
        }
    }

    public String getBaseForm(String derivation) {
        try {
            IndexWordSet tmp = Dictionary.getInstance().lookupAllIndexWords(derivation);
            IndexWord[] tmpar = tmp.getIndexWordArray();
            for (IndexWord indexWord : tmpar) {
                String word = indexWord.getLemma();
                if (word != null)
                    return word;
            }
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            //throw new SMatchException(errMessage, e);
        }
        return null;
    }

    public boolean isEqual(String str1, String str2) {
        try {
            IndexWordSet lemmas1 = dic.lookupAllIndexWords(str1);
            IndexWordSet lemmas2 = dic.lookupAllIndexWords(str2);
            if ((lemmas1 == null) || (lemmas2 == null) || (lemmas1.size() < 1) || (lemmas2.size() < 1))
                return false;
            else {
                IndexWord[] v1 = lemmas1.getIndexWordArray();
                IndexWord[] v2 = lemmas2.getIndexWordArray();
                for (IndexWord aV1 : v1) {
                    for (IndexWord aV2 : v2) {
                        if (aV1.equals(aV2))
                            return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ISynset getISynset(String source) {
        return new WordNetSynset(getSynset(source));
    }

    public char getRelation(Vector<String> sourceSenses, Vector<String> targetSenses) {
        for (String sourceSense : sourceSenses) {
            for (String targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.EQUIVALENCE)) {
                    return IMappingElement.EQUIVALENCE;
                }
            }
        }
        //  Check for less general than
        for (String sourceSense : sourceSenses) {
            for (String targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.LESS_GENERAL)) {
                    return IMappingElement.LESS_GENERAL;
                }
            }
        }
        //  Check for more general than
        for (String sourceSense : sourceSenses) {
            for (String targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.MORE_GENERAL)) {
                    return IMappingElement.MORE_GENERAL;
                }
            }
        }
        //  Check for opposite meaning
        for (String sourceSense : sourceSenses) {
            for (String targetSense : targetSenses) {
                if (getRelationFromOracle(sourceSense, targetSense, IMappingElement.DISJOINT)) {
                    return IMappingElement.DISJOINT;
                }
            }
        }
        return IMappingElement.IDK;
    }

    public char getRelationACoL(IAtomicConceptOfLabel source, IAtomicConceptOfLabel target) {
        Vector<String> sourceSenses = source.getSenses().getSenseList();
        Vector<String> targetSenses = target.getSenses().getSenseList();
        return getRelation(sourceSenses, targetSenses);
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
     */
    private boolean getRelationFromOracle(String source, String target, char rel) {
        char tmp;
        //  If we don't have cashed relation check which one exist and put it to cash
        if (sensesCache.get(source + target) == null) {
            //  Check for synonymy
            if (isSourceSynonymTarget(source, target)) {
                sensesCache.put(source + target, IMappingElement.EQUIVALENCE);
                return rel == IMappingElement.EQUIVALENCE;
            } else {
                //  Check for opposite meaning
                if (isSourceOppositeToTarget(source, target)) {
                    sensesCache.put(source + target, IMappingElement.DISJOINT);
                    return rel == IMappingElement.DISJOINT;
                } else {
                    //  Check for less general than
                    if (isSourceLessGeneralThanTarget(source, target)) {
                        sensesCache.put(source + target, IMappingElement.LESS_GENERAL);
                        return rel == IMappingElement.LESS_GENERAL;
                    } else {
                        //  Check for more general than
                        if (isSourceMoreGeneralThanTarget(source, target)) {
                            sensesCache.put(source + target, IMappingElement.MORE_GENERAL);
                            return rel == IMappingElement.MORE_GENERAL;
                        } else {
                            sensesCache.put(source + target, IMappingElement.IDK);
                            return false;
                        }
                    }
                }
            }
            //  Return relation already stored in cash
        } else {
            tmp = (sensesCache.get(source + target));
            return rel == tmp;
        }
    }

    public boolean isSourceSynonymTarget(String source, String target) {
        if ((source.indexOf("000000") == -1) && (target.indexOf("000000") == -1)) {
            if ((source.indexOf(UNKNOWN_MEANING) > -1) || (target.indexOf(UNKNOWN_MEANING) > -1)) {
                return false;
            }
            if (source.equals(target)) {
                return true;
            }
            try {
                //Get synsets
                Synset sourceSyn = getSynset(source);
                Synset targetSyn = getSynset(target);
                //is synonym
                RelationshipList list = RelationshipFinder.getInstance().findRelationships(sourceSyn, targetSyn, PointerType.SIMILAR_TO);
                if (list.size() > 0) {
                    if ((source.startsWith("a#")) || (target.startsWith("a#"))) {
                        return (((Relationship) list.get(0)).getDepth() == 0);
                    } else {
                        return true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean isSourceOppositeToTarget(String source, String target) {
        //synonymy and unrecognize words check
        if ((source.indexOf("000000") == -1) && (target.indexOf("000000") == -1)) {
            if ((source.indexOf(UNKNOWN_MEANING) > -1) || (target.indexOf(UNKNOWN_MEANING) > -1)) {
                return false;
            }
            if (source.equals(target)) {
                return false;
            }
            try {
                Synset sourceSyn = getSynset(source);
                Synset targetSyn = getSynset(target);
                //  Checks whether senses are siblings (thus they are opposite)
                if (source.startsWith("n#") && target.startsWith("n#")) {
                } else {
                    RelationshipList list = RelationshipFinder.getInstance().findRelationships(sourceSyn, targetSyn, PointerType.ANTONYM);
                    if (list.size() > 0) {
                        return true;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
    public boolean isSourceLessGeneralThanTarget(String source, String target) {
        return isSourceMoreGeneralThanTarget(target, source);
    }

    public boolean isSourceMoreGeneralThanTarget(String source, String target) {
        if ((source.indexOf("000000") == -1) && (target.indexOf("000000") == -1)) {
            if (((source.startsWith("n")) && (target.startsWith("n"))) || ((source.startsWith("n")) && (target.startsWith("n")))) {
                if (source.equals(target)) {
                    return false;
                }
                if ((source.indexOf(UNKNOWN_MEANING) > -1) || (target.indexOf(UNKNOWN_MEANING) > -1)) {
                    return false;
                }
                try {
                    Synset sourceSyn = getSynset(source);
                    Synset targetSyn = getSynset(target);
                    //  Find all more general relationships from wordnet
                    RelationshipList list = RelationshipFinder.getInstance().findRelationships(sourceSyn, targetSyn, PointerType.HYPERNYM);
                    if (!isUnidirestionalList(list)) {
                        PointerTargetTree ptt = PointerUtils.getInstance().getInheritedMemberHolonyms(targetSyn);
                        PointerTargetNodeList ptnl = PointerUtils.getInstance().getMemberHolonyms(targetSyn);
                        if (!traverseTree(ptt, ptnl, sourceSyn)) {
                            ptt = PointerUtils.getInstance().getInheritedPartHolonyms(targetSyn);
                            ptnl = PointerUtils.getInstance().getPartHolonyms(targetSyn);
                            if (!traverseTree(ptt, ptnl, sourceSyn)) {
                                ptt = PointerUtils.getInstance().getInheritedSubstanceHolonyms(targetSyn);
                                ptnl = PointerUtils.getInstance().getSubstanceHolonyms(targetSyn);
                                if (traverseTree(ptt, ptnl, sourceSyn)) {
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * traverses PointerTargetTree.
     *
     * @param syn
     * @param ptnl
     * @param source
     * @return
     */
    private static boolean traverseTree(PointerTargetTree syn, PointerTargetNodeList ptnl, Synset source) {
        java.util.List MGListsList = syn.toList();
        for (int j = 0; j < MGListsList.size(); j++) {
            PointerTargetNodeList MGList = (PointerTargetNodeList) MGListsList.get(j);
            for (Iterator synIt = MGList.iterator(); synIt.hasNext();) {
                Synset toAdd = ((PointerTargetNode) synIt.next()).getSynset();
                if (toAdd.equals(source)) {
                    return true;
                }
            }
        }
        for (Iterator synIt = ptnl.iterator(); synIt.hasNext();) {
            Synset toAdd = ((PointerTargetNode) synIt.next()).getSynset();
            if (toAdd.equals(source)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks unidirectionality of semantic relations in the list.
     *
     * @param list
     * @return
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
     * Returns a synset for a sense id.
     *
     * @param source a sense id
     * @return synset
     */
    private Synset getSynset(String source) {
        StringTokenizer stSource = new StringTokenizer(source, "#");
        try {
            POS POSSource = POS.getPOSForKey(stSource.nextToken());
            if (!stSource.hasMoreTokens()) {
                System.err.println(source);
                return null;
            }
            String sourseID = stSource.nextToken();
            if (!sourseID.startsWith("000000")) {
                long lSourseID = Long.parseLong(sourseID);
                return dic.getSynsetAt(POSSource, lSourseID);
            }
        } catch (Exception ex) {
            System.err.println(source);
            ex.printStackTrace();
        }
        return null;
    }
}