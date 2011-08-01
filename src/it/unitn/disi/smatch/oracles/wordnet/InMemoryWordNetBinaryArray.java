package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.utils.SMatchUtils;
import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Implements version of WN matcher which use a fast internal data structure.
 * <p/>
 * Needs several string configuration parameters pointing to files with cache. See default S-Match configuration
 * file for examples.
 * <p/>
 * Accepts loadArray boolean configuration parameter which allows to skip loading arrays into memory.
 * By default equals true, it is useful during generation of WordNet caches when it should be set to false.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class InMemoryWordNetBinaryArray extends Configurable implements ISenseMatcher {

    private static final Logger log = Logger.getLogger(InMemoryWordNetBinaryArray.class);

    // configuration keys for WordNet cache files
    private static final String ADJ_SYN_KEY = "adjectiveSynonymFile";
    private static final String ADJ_ANT_KEY = "adjectiveAntonymFile";
    private static final String NOUN_MG_KEY = "nounMGFile";
    private static final String NOUN_ANT_KEY = "nounAntonymFile";
    private static final String VERB_MG_KEY = "verbMGFile";
    private static final String NOMINALIZATION_KEY = "nominalizationsFile";
    private static final String ADV_ANT_KEY = "adverbsAntonymFile";

    private static final String JWNL_PROPERTIES_PATH_KEY = "JWNLPropertiesPath";

    // controls loading of arrays, used to skip loading before conversion
    private static final String LOAD_ARRAYS_KEY = "loadArrays";

    // array with WordNet keys
    private long[] adj_syn = null;
    private long[] adj_opp = null;
    private long[] noun_mg = null;
    private long[] noun_opp = null;
    private long[] adv_opp = null;
    private long[] verb_mg = null;
    private long[] nominalizations = null;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = super.setProperties(newProperties);
        if (result) {
            boolean loadArrays = true;
            if (newProperties.containsKey(LOAD_ARRAYS_KEY)) {
                loadArrays = Boolean.parseBoolean(newProperties.getProperty(LOAD_ARRAYS_KEY));
            }

            if (loadArrays) {
                log.info("Loading WordNet cache to memory...");
                adj_syn = readArray(newProperties, ADJ_SYN_KEY, "adjective synonyms");
                adj_opp = readArray(newProperties, ADJ_ANT_KEY, "adjective antonyms");
                noun_mg = readArray(newProperties, NOUN_MG_KEY, "noun hypernyms");
                noun_opp = readArray(newProperties, NOUN_ANT_KEY, "noun antonyms");
                verb_mg = readArray(newProperties, VERB_MG_KEY, "verb hypernyms");
                adv_opp = readArray(newProperties, ADV_ANT_KEY, "adverb antonyms");
                nominalizations = readArray(newProperties, NOMINALIZATION_KEY, "nominalizations");
                log.info("Loading WordNet cache to memory finished");
            }
        }
        return result;
    }

    public char getRelation(List<ISense> sourceSenses, List<ISense> targetSenses) {
        // Check for synonymy
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (isSourceSynonymTarget(sourceSense, targetSense)) {
                    return IMappingElement.EQUIVALENCE;
                }
            }
        }
        // Check for less general than
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (isSourceLessGeneralThanTarget(sourceSense, targetSense)) {
                    return IMappingElement.LESS_GENERAL;
                }
            }
        }
        // Check for more general than
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (isSourceMoreGeneralThanTarget(sourceSense, targetSense)) {
                    return IMappingElement.MORE_GENERAL;
                }
            }
        }
        // Check for opposite meaning
        for (ISense sourceSense : sourceSenses) {
            for (ISense targetSense : targetSenses) {
                if (isSourceOppositeToTarget(sourceSense, targetSense)) {
                    return IMappingElement.DISJOINT;
                }
            }
        }
        return IMappingElement.IDK;
    }

    private boolean isSourceOppositeToTargetInt(long sourceSense, long targetSense, char sourcePOS, char targetPOS) {
        long key;
        if (targetSense > sourceSense) {
            key = (targetSense << 32) + sourceSense;
        } else {
            key = (sourceSense << 32) + targetSense;
        }

        if (('n' == sourcePOS) && ('n' == targetPOS)) {
            if (java.util.Arrays.binarySearch(noun_opp, key) >= 0) {
                return true;
            }
        } else {
            if (('a' == sourcePOS) && ('a' == targetPOS)) {
                if (java.util.Arrays.binarySearch(adj_opp, key) >= 0) {
                    return true;
                }
            } else {
                if (('r' == sourcePOS) && ('r' == targetPOS)) {
                    if (java.util.Arrays.binarySearch(adv_opp, key) >= 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isSourceLessGeneralThanTargetInt(long sourceSense, long targetSense, char sourcePOS, char targetPOS) {
        long key = (sourceSense << 32) + targetSense;
        if (('n' == sourcePOS) && ('n' == targetPOS)) {
            if (java.util.Arrays.binarySearch(noun_mg, key) >= 0) {
                return true;
            }
        } else {
            if (('v' == sourcePOS) && ('v' == targetPOS)) {
                if (java.util.Arrays.binarySearch(verb_mg, key) >= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isSourceSynonymTargetInt(long sourceSense, long targetSense, char sourcePOS, char targetPOS) {
        if (sourceSense == targetSense) {
            return true;
        }

        long key;
        if (targetSense > sourceSense) {
            key = (targetSense << 32) + sourceSense;
        } else {
            key = (sourceSense << 32) + targetSense;
        }

        if (('a' == sourcePOS) && ('a' == targetPOS)) {
            if (java.util.Arrays.binarySearch(adj_syn, key) >= 0) {
                return true;
            }
        }
        if (('n' == sourcePOS) && ('v' == targetPOS)) {
            key = (targetSense << 32) + sourceSense;
            if (java.util.Arrays.binarySearch(nominalizations, key) >= 0) {
                return true;
            }
        }
        if (('v' == sourcePOS) && ('n' == targetPOS)) {
            key = (sourceSense << 32) + targetSense;
            if (java.util.Arrays.binarySearch(nominalizations, key) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static long[] readArray(Properties properties, String key, String name) throws ConfigurableException {
        long[] result;
        if (properties.containsKey(key)) {
            result = readHash(properties.getProperty(key));
            log.debug("Read " + name + ": " + result.length);
        } else {
            final String errMessage = "Cannot find configuration key " + key;
            log.error(errMessage);
            throw new ConfigurableException(errMessage);
        }
        return result;
    }

    private static long[] readHash(String fileName) throws SMatchException {
        return (long[]) SMatchUtils.readObject(fileName);
    }

    public boolean isSourceMoreGeneralThanTarget(ISense source, ISense target) {
        return isSourceLessGeneralThanTarget(target, source);
    }

    public boolean isSourceLessGeneralThanTarget(ISense source, ISense target) {
        return isSourceLessGeneralThanTargetInt(source.getId(), target.getId(), source.getPos(), target.getPos());
    }

    public boolean isSourceSynonymTarget(ISense source, ISense target) {
        return isSourceSynonymTargetInt(source.getId(), target.getId(), source.getPos(), target.getPos());
    }

    public boolean isSourceOppositeToTarget(ISense source, ISense target) {
        return isSourceOppositeToTargetInt(source.getId(), target.getId(), source.getPos(), target.getPos());
    }

    /**
     * Create caches of WordNet to speed up matching.
     *
     * @param componentKey a key to the component in the configuration
     * @param properties   configuration
     * @throws SMatchException SMatchException
     */
    public static void createWordNetCaches(String componentKey, Properties properties) throws SMatchException {
        properties = getComponentProperties(makeComponentPrefix(componentKey, InMemoryWordNetBinaryArray.class.getSimpleName()), properties);
        if (properties.containsKey(JWNL_PROPERTIES_PATH_KEY)) {
            // initialize JWNL (this must be done before JWNL library can be used)
            try {
                final String configPath = properties.getProperty(JWNL_PROPERTIES_PATH_KEY);
                log.info("Initializing JWNL from " + configPath);
                JWNL.initialize(new FileInputStream(configPath));
            } catch (JWNLException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            } catch (FileNotFoundException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        } else {
            final String errMessage = "Cannot find configuration key " + JWNL_PROPERTIES_PATH_KEY;
            log.error(errMessage);
            throw new SMatchException(errMessage);
        }

        log.info("Creating WordNet caches...");
        writeNominalizations(properties);
        writeSynonymsAdj(properties);
        writeOppAdverbs(properties);
        writeOppAdjectives(properties);
        writeOppNouns(properties);
        writeNounMG(properties);
        writeVerbMG(properties);
        log.info("Done");
    }

    private static void writeNominalizations(Properties properties) throws SMatchException {
        log.info("Creating nominalizations array...");
        HashSet<Long> keys = new HashSet<Long>();
        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.VERB);
            while (it.hasNext()) {
                count++;
                if (0 == count % 1000) {
                    log.info(count);
                }
                Synset source = (Synset) it.next();
                List<Pointer> pointers = source.getPointers(PointerType.NOMINALIZATION);
                for (Pointer pointer : pointers) {
                    long targetOffset = pointer.getTargetOffset();
                    long key = (source.getOffset() << 32) + targetOffset;
                    keys.add(key);
                }
            }
            log.info("Nominalizations: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            SMatchUtils.writeObject(keysArr, properties.getProperty(NOMINALIZATION_KEY));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    private static void writeSynonymsAdj(Properties properties) throws SMatchException {
        log.info("Creating adjective synonyms array...");
        HashSet<Long> keys = new HashSet<Long>();
        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.ADJECTIVE);
            while (it.hasNext()) {
                count++;
                if (0 == count % 1000) {
                    log.info(count);
                }
                Synset source = (Synset) it.next();
                long sourceOffset = source.getOffset();
                List<Pointer> pointers = source.getPointers(PointerType.SIMILAR_TO);
                for (Pointer ptr : pointers) {
                    long targetOffset = ptr.getTargetOffset();
                    long key;
                    if (targetOffset > sourceOffset) {
                        key = (targetOffset << 32) + sourceOffset;
                    } else {
                        key = (sourceOffset << 32) + targetOffset;
                    }
                    keys.add(key);
                }
            }
            log.info("Adjective Synonyms: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            SMatchUtils.writeObject(keysArr, properties.getProperty(ADJ_SYN_KEY));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    private static void writeOppAdverbs(Properties properties) throws SMatchException {
        log.info("Creating adverb antonyms array...");
        HashSet<Long> keys = new HashSet<Long>();

        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.ADVERB);
            while (it.hasNext()) {
                count++;
                if (0 == count % 1000) {
                    log.info(count);
                }
                Synset source = (Synset) it.next();
                long sourceOffset = source.getOffset();
                List<Pointer> pointers = source.getPointers(PointerType.ANTONYM);
                for (Pointer ptr : pointers) {
                    long targetOffset = ptr.getTargetOffset();
                    long key;
                    if (targetOffset > sourceOffset) {
                        key = (targetOffset << 32) + sourceOffset;
                    } else {
                        key = (sourceOffset << 32) + targetOffset;
                    }
                    keys.add(key);
                }
            }
            log.info("Adverbs antonyms: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            SMatchUtils.writeObject(keysArr, properties.getProperty(ADV_ANT_KEY));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    private static void writeOppAdjectives(Properties properties) throws SMatchException {
        log.info("Creating adjective antonyms array...");
        HashSet<Long> keys = new HashSet<Long>();

        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.ADJECTIVE);
            while (it.hasNext()) {
                count++;
                if (0 == count % 1000) {
                    log.info(count);
                }
                Synset current = (Synset) it.next();
                traverseTree(keys, PointerUtils.getExtendedAntonyms(current), current.getOffset());
                traverseListSym(keys, PointerUtils.getAntonyms(current), current.getOffset());
            }
            log.info("Adjective antonyms: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            SMatchUtils.writeObject(keysArr, properties.getProperty(ADJ_ANT_KEY));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    private static void writeOppNouns(Properties properties) throws SMatchException {
        log.info("Creating noun antonyms array...");
        HashSet<Long> keys = new HashSet<Long>();

        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.NOUN);
            while (it.hasNext()) {
                count++;
                if (0 == count % 10000) {
                    log.info(count);
                }
                Synset source = (Synset) it.next();

                cartPr(keys, source.getPointers(PointerType.PART_MERONYM));
                cartPr(keys, source.getPointers(PointerType.SUBSTANCE_MERONYM));
                cartPr(keys, source.getPointers(PointerType.MEMBER_MERONYM));
            }

            log.info("Noun antonyms: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            SMatchUtils.writeObject(keysArr, properties.getProperty(NOUN_ANT_KEY));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    private static void writeNounMG(Properties properties) throws SMatchException {
        log.info("Creating noun mg array...");
        HashSet<Long> keys = new HashSet<Long>();

        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.NOUN);
            while (it.hasNext()) {
                count++;
                if (0 == count % 10000) {
                    log.info(count);
                }
                Synset source = (Synset) it.next();
                long sourceOffset = source.getOffset();
                traverseTreeMG(keys, PointerUtils.getHypernymTree(source), sourceOffset);
                traverseTreeMG(keys, PointerUtils.getInheritedHolonyms(source), sourceOffset);
                traverseTreeMG(keys, PointerUtils.getInheritedMemberHolonyms(source), sourceOffset);
                traverseTreeMG(keys, PointerUtils.getInheritedPartHolonyms(source), sourceOffset);
                traverseTreeMG(keys, PointerUtils.getInheritedSubstanceHolonyms(source), sourceOffset);
                traverseListMG(keys, PointerUtils.getHolonyms(source), sourceOffset);
                traverseListMG(keys, PointerUtils.getMemberHolonyms(source), sourceOffset);
                traverseListMG(keys, PointerUtils.getPartHolonyms(source), sourceOffset);
                traverseListMG(keys, PointerUtils.getSubstanceHolonyms(source), sourceOffset);
            }
            log.info("Noun mg: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            SMatchUtils.writeObject(keysArr, properties.getProperty(NOUN_MG_KEY));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    private static void writeVerbMG(Properties properties) throws SMatchException {
        log.info("Creating verb mg array...");
        HashSet<Long> keys = new HashSet<Long>();

        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.VERB);
            while (it.hasNext()) {
                count++;
                if (0 == count % 1000) {
                    log.info(count);
                }
                Synset source = (Synset) it.next();
                long sourceOffset = source.getOffset();
                traverseTreeMG(keys, PointerUtils.getHypernymTree(source), sourceOffset);
            }
            log.info("Verb mg: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            SMatchUtils.writeObject(keysArr, properties.getProperty(VERB_MG_KEY));
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new SMatchException(errMessage, e);
        }
    }

    private static void cartPr(HashSet<Long> keys, List<Pointer> t) {
        for (int i = 0; i < t.size(); i++) {
            Pointer ps = t.get(i);
            long sourceOffset = ps.getTargetSynset().getOffset();
            for (int j = i + 1; j < t.size(); j++) {
                Pointer pt = t.get(j);
                long targetOffset = pt.getTargetSynset().getOffset();
                if (sourceOffset != targetOffset) {
                    long key;
                    if (targetOffset > sourceOffset) {
                        key = (targetOffset << 32) + sourceOffset;
                    } else {
                        key = (sourceOffset << 32) + targetOffset;
                    }
                    keys.add(key);
                }
            }
        }
    }

    private static void traverseListMG(HashSet<Long> keys, PointerTargetNodeList pointers, long sourceOffset) {
        for (Object pointer : pointers) {
            long targetOffset = ((PointerTargetNode) pointer).getSynset().getOffset();
            if (sourceOffset != targetOffset) {
                long key = (sourceOffset << 32) + targetOffset;
                keys.add(key);
            }
        }
    }

    private static void traverseListSym(HashSet<Long> keys, PointerTargetNodeList pointers, long sourceOffset) {
        for (Object ptn : pointers) {
            long targetOffset = ((PointerTargetNode) ptn).getSynset().getOffset();
            if (sourceOffset != targetOffset) {
                long key;//null;
                if (targetOffset > sourceOffset) {
                    key = (targetOffset << 32) + sourceOffset;
                } else {
                    key = (sourceOffset << 32) + targetOffset;
                }
                keys.add(key);
            }
        }
    }

    private static void traverseTreeMG(HashSet<Long> keys, PointerTargetTree syn, long sourceOffset) {
        for (Object aMGListsList : syn.toList()) {
            for (Object ptn : (PointerTargetNodeList) aMGListsList) {
                long targetOffset = ((PointerTargetNode) ptn).getSynset().getOffset();
                if (sourceOffset != targetOffset) {
                    long key = (sourceOffset << 32) + targetOffset;
                    keys.add(key);
                }
            }
        }
    }

    private static void traverseTree(HashSet<Long> keys, PointerTargetTree syn, long sourceOffset) {
        for (Object aMGListsList : syn.toList()) {
            for (Object ptn : (PointerTargetNodeList) aMGListsList) {
                long targetOffset = ((PointerTargetNode) ptn).getSynset().getOffset();
                if (sourceOffset != targetOffset) {
                    long key;//null;
                    if (targetOffset > sourceOffset) {
                        key = (targetOffset << 32) + sourceOffset;
                    } else {
                        key = (sourceOffset << 32) + targetOffset;
                    }
                    keys.add(key);
                }
            }
        }
    }
}
