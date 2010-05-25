package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.ling.Sense;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.*;
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
 * <p/>
 * Needs  JWNLPropertiesPath string parameter which should point to a JWNL configuration file.
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
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = super.setProperties(newProperties);
        if (result) {
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
                    for (int j = 0; j < lemma.getSenseCount(); j++) {
                        Synset synset = lemma.getSenses()[j];
                        result.add(new Sense(synset.getPOS().getKey().charAt(0), synset.getOffset()));
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

    public String getBaseForm(String derivation) throws LinguisticOracleException {
        try {
            String result = derivation;
            IndexWordSet tmp = dic.lookupAllIndexWords(derivation);
            if (null != tmp) {
                for (IndexWord indexWord : tmp.getIndexWordArray()) {
                    String word = indexWord.getLemma();
                    if (null != word) {
                        result = word;
                        break;
                    }
                }
            }
            return result;
        } catch (JWNLException e) {
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
                        if (aV1.equals(aV2))
                            return true;
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

    public ISynset getISynset(ISense source) throws LinguisticOracleException {
        return new WordNetSynset(getSynset(source));
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
        try {
            Synset sourceSyn = getSynset(source);
            Synset targetSyn = getSynset(target);
            //is synonym
            RelationshipList list = RelationshipFinder.getInstance().findRelationships(sourceSyn, targetSyn, PointerType.SIMILAR_TO);
            if (list.size() > 0) {
                if (('a' == source.getPos()) || ('a' == target.getPos())) {
                    return (((Relationship) list.get(0)).getDepth() == 0);
                } else {
                    return true;
                }
            }
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SenseMatcherException(errMessage, e);
        } catch (LinguisticOracleException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SenseMatcherException(errMessage, e);
        }
        return false;
    }

    public boolean isSourceOppositeToTarget(ISense source, ISense target) throws SenseMatcherException {
        if (source.equals(target)) {
            return false;
        }
        try {
            Synset sourceSyn = getSynset(source);
            Synset targetSyn = getSynset(target);
            //  Checks whether senses are siblings (thus they are opposite)
            if ('n' == source.getPos() && 'n' == target.getPos()) {
            } else {
                RelationshipList list = RelationshipFinder.getInstance().findRelationships(sourceSyn, targetSyn, PointerType.ANTONYM);
                if (list.size() > 0) {
                    return true;
                }
            }
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SenseMatcherException(errMessage, e);
        } catch (LinguisticOracleException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SenseMatcherException(errMessage, e);
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
        if (('n' == source.getPos() && 'n' == target.getPos()) || ('v' == source.getPos() && 'v' == target.getPos())) {
            if (source.equals(target)) {
                return false;
            }
            try {
                Synset sourceSyn = getSynset(source);
                Synset targetSyn = getSynset(target);
                // find all more general relationships from WordNet
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
            } catch (JWNLException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SenseMatcherException(errMessage, e);
            } catch (LinguisticOracleException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SenseMatcherException(errMessage, e);
            }
        }
        return false;
    }

    /**
     * traverses PointerTargetTree.
     *
     * @param syn synonyms
     * @param ptnl target node list
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
     * Returns a synset for a sense.
     *
     * @param source a sense
     * @return synset
     * @throws LinguisticOracleException LinguisticOracleException
     */
    private Synset getSynset(ISense source) throws LinguisticOracleException {
        try {
            POS POSSource = POS.getPOSForKey(Character.toString(source.getPos()));
            return dic.getSynsetAt(POSSource, source.getId());
        } catch (JWNLException e) {
            final String errMessage = "Malformed synset id: " + source + ". Error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new LinguisticOracleException(errMessage, e);
        }
    }
}