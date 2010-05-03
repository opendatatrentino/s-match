package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.oracles.IWordNetMatcher;

import java.io.*;
import java.util.*;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * Implements version of WN matcher which use a fast internal data structure.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class InMemoryWordNetBinaryArray implements IWordNetMatcher {

    private static final Logger log = Logger.getLogger(InMemoryWordNetBinaryArray.class);

    private long relsReturned = 0;

    private long[] adj_syn = null;
    private long[] adj_opp = null;
    private long[] noun_mg = null;
    private long[] noun_opp = null;
    private long[] adv_opp = null;
    private long[] verb_mg = null;
    private long[] nominalizations = null;

    public InMemoryWordNetBinaryArray() throws SMatchException {
        log.info("Loading WordNet to memory...");
        //Reading WN into memory
        if (adj_syn == null) {
            adj_syn = readHash(MatchManager.adjectiveSynonymFile);
            log.debug("Read adjectives adj_syn: " + adj_syn.length);
        }
        if (adj_opp == null) {
            adj_opp = readHash(MatchManager.adjectiveAntonymFile);
            log.debug("Read adjectives adj_opp: " + adj_opp.length);
        }

        if (noun_mg == null) {
            noun_mg = readHash(MatchManager.nounMGFile);
            log.debug("Read nouns noun_mg: " + noun_mg.length);
        }
        if (noun_opp == null) {
            noun_opp = readHash(MatchManager.nounAntonymFile);
            log.debug("Read nouns noun_opp: " + noun_opp.length);
        }

        if (verb_mg == null) {
            verb_mg = readHash(MatchManager.verbMGFile);
            log.debug("Read verbs verb_mg: " + verb_mg.length);
        }
        if (adv_opp == null) {
            adv_opp = readHash(MatchManager.adverbsAntonymFile);
            log.debug("Read adverbs adv_opp: " + adv_opp.length);
        }
        if (nominalizations == null) {
            nominalizations = readHash(MatchManager.nominalizationsFile);
            log.debug("Read nominalizations nom: " + nominalizations.length);
        }

        log.info("Loading WordNet to memory finished");
    }

    public char getRelation(Vector<String> sourceSenses, Vector<String> targetSenses) {
        // Check for synonymy
        for (int i = 0; i < sourceSenses.size(); i++) {
            String sourceSense = sourceSenses.get(i);
            for (int j = 0; j < targetSenses.size(); j++) {
                String targetSense = targetSenses.get(j);
                if (isSourceSynonymTarget(sourceSense, targetSense)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    relsReturned++;
                    return MatchManager.SYNOMYM;
                }
            }
        }
        // Check for less general than
        for (int i = 0; i < sourceSenses.size(); i++) {
            String sourceSense = sourceSenses.get(i);
            for (int j = 0; j < targetSenses.size(); j++) {
                String targetSense = targetSenses.get(j);
                if (isSourceLessGeneralThanTarget(sourceSense, targetSense)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    relsReturned++;
                    return MatchManager.LESS_GENERAL_THAN;
                }
            }
        }
        // Check for more general than
        for (int i = 0; i < sourceSenses.size(); i++) {
            String sourceSense = sourceSenses.get(i);
            for (int j = 0; j < targetSenses.size(); j++) {
                String targetSense = targetSenses.get(j);
                if (isSourceMoreGeneralThanTarget(sourceSense, targetSense)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    relsReturned++;
                    return MatchManager.MORE_GENERAL_THAN;
                }
            }
        }
        // Check for opposite meaning
        for (int i = 0; i < sourceSenses.size(); i++) {
            String sourceSense = sourceSenses.get(i);
            for (int j = 0; j < targetSenses.size(); j++) {
                String targetSense = (targetSenses.get(j));
                if (isSourceOppositeToTarget(sourceSense, targetSense)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    relsReturned++;
                    return MatchManager.OPPOSITE_MEANING;
                }
            }
        }
        return MatchManager.IDK_RELATION;
    }

    public char getRelationACoL(IAtomicConceptOfLabel source, IAtomicConceptOfLabel target) {
        long[] sourceSenses = source.getSenses().getIntSenses();
        long[] targetSenses = target.getSenses().getIntSenses();
        char[] POSSensesSource = source.getSenses().getPOSSenses();
        char[] POSSensesTarget = target.getSenses().getPOSSenses();

        if ((1 == sourceSenses.length && 0 == sourceSenses[0]) || (1 == targetSenses.length && 0 == targetSenses[0])) {
            //either source or target are meaningless, does not make sense to match
        } else {
            // Check for synonymy
            for (int i = 0; i < sourceSenses.length; i++) {
                long sourceSense = sourceSenses[i];
                for (int j = 0; j < targetSenses.length; j++) {
                    long targetSense = targetSenses[j];
                    if (isSourceSynonymTargetInt(sourceSense, targetSense, POSSensesSource[i], POSSensesTarget[j])) {
                        relsReturned++;
                        return MatchManager.SYNOMYM;
                    }
                }
            }
            // Check for less general than
            for (int i = 0; i < sourceSenses.length; i++) {
                long sourceSense = sourceSenses[i];
                for (int j = 0; j < targetSenses.length; j++) {
                    long targetSense = targetSenses[j];
                    if (isSourceLessGeneralThanTargetInt(sourceSense, targetSense, POSSensesSource[i], POSSensesTarget[j])) {
                        relsReturned++;
                        return MatchManager.LESS_GENERAL_THAN;
                    }
                }
            }
            // Check for more general than
            for (int i = 0; i < sourceSenses.length; i++) {
                long sourceSense = sourceSenses[i];
                for (int j = 0; j < targetSenses.length; j++) {
                    long targetSense = targetSenses[j];
                    if (isSourceMoreGeneralThanTargetInt(sourceSense, targetSense, POSSensesSource[i], POSSensesTarget[j])) {
                        relsReturned++;
                        return MatchManager.MORE_GENERAL_THAN;
                    }
                }
            }
            // Check for opposite meaning
            for (int i = 0; i < sourceSenses.length; i++) {
                long sourceSense = sourceSenses[i];
                for (int j = 0; j < targetSenses.length; j++) {
                    long targetSense = targetSenses[j];
                    if (isSourceOppositeToTargetInt(sourceSense, targetSense, POSSensesSource[i], POSSensesTarget[j])) {
                        relsReturned++;
                        return MatchManager.OPPOSITE_MEANING;
                    }
                }
            }
        }

        return MatchManager.IDK_RELATION;
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

    private boolean isSourceMoreGeneralThanTargetInt(long sourceSense, long targetSense, char sourcePOS, char targetPOS) {
        return isSourceLessGeneralThanTargetInt(targetSense, sourceSense, targetPOS, sourcePOS);
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

    private static long[] readHash(String fileName) throws SMatchException {
        long[] result = null;
        try {
            FileInputStream fos = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fos);
            ObjectInputStream oos = new ObjectInputStream(bis);
            try {
                result = (long[]) oos.readObject();
            } catch (IOException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    throw new SMatchException(errMessage, e);
                }
            } catch (ClassNotFoundException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    throw new SMatchException(errMessage, e);
                }
            }
            oos.close();
            bis.close();
            fos.close();
        } catch (IOException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
        return result;
    }

    public boolean isSourceMoreGeneralThanTarget(String source, String target) {
        return isSourceLessGeneralThanTarget(target, source);
    }

    public boolean isSourceLessGeneralThanTarget(String source, String target) {
        if ((source.indexOf("000000") == -1) && (target.indexOf("000000") == -1)) {
            long l_source = Long.parseLong(source.substring(2));
            long l_target = Long.parseLong(target.substring(2));
            char c_source = source.charAt(0);
            char c_target = target.charAt(0);
            return isSourceLessGeneralThanTargetInt(l_source, l_target, c_source, c_target);
        }
        return false;
    }

    public boolean isSourceSynonymTarget(String source, String target) {
        if ((source.indexOf("000000") == -1) && (target.indexOf("000000") == -1)) {
            long l_source = Long.parseLong(source.substring(2));
            long l_target = Long.parseLong(target.substring(2));
            char c_source = source.charAt(0);
            char c_target = target.charAt(0);
            return isSourceSynonymTargetInt(l_source, l_target, c_source, c_target);
        }
        return false;
    }

    public boolean isSourceOppositeToTarget(String source, String target) {
        if ((source.indexOf("000000") == -1) && (target.indexOf("000000") == -1)) {
            long l_source = Long.parseLong(source.substring(2));
            long l_target = Long.parseLong(target.substring(2));
            char c_source = source.charAt(0);
            char c_target = target.charAt(0);
            return isSourceOppositeToTargetInt(l_source, l_target, c_source, c_target);
        }
        return false;
    }

    public void reportUsage() {
        log.info("Wordnet returned relationships: " + relsReturned);
    }

    /**
     * Create caches of WordNet to speed up matching.
     * @throws SMatchException SMatchException
     */
    public static void createWordNetCaches() throws SMatchException {
        log.info("Creating WordNet caches...");
        writeMultiwords();
        writeNominalizations();
        writeSynonymsAdj();
        writeOppAdverbs();
        writeOppAdjectives();
        writeOppNouns();
        writeNounMG();
        writeVerbMG();
        log.info("Finished.");
    }

    private static void writeMultiwords() throws SMatchException {
        log.info("Creating multiword hash...");
        Hashtable<String, Vector<Vector<String>>> multiwords = new Hashtable<String, Vector<Vector<String>>>();
        POS[] parts = new POS[]{POS.NOUN, POS.ADJECTIVE, POS.VERB, POS.ADVERB};
        for (POS pos : parts) {
            collectMultiwords(multiwords, pos);
        }
        log.info("Multiwords: " + multiwords.size());
        writeObject(multiwords, MatchManager.multiwordsFileName);
    }

    private static void collectMultiwords(Hashtable<String, Vector<Vector<String>>> multiwords, POS pos) throws SMatchException {
        try {
            int count = 0;
            Iterator i = Dictionary.getInstance().getIndexWordIterator(pos);
            while (i.hasNext()) {
                IndexWord iw = (IndexWord) i.next();
                String lemma = iw.getLemma();
                if (-1 < lemma.indexOf(' ')) {
                    count++;
                    if (0 == count % 10000) {
                        log.info(count);
                    }
                    String[] tokens = lemma.split(" ");
                    Vector<Vector<String>> mwEnds = multiwords.get(tokens[0]);
                    if (null == mwEnds) {
                        mwEnds = new Vector<Vector<String>>();
                    }
                    Vector<String> currentMWEnd = new Vector<String>(Arrays.asList(tokens));
                    currentMWEnd.remove(0);
                    mwEnds.add(currentMWEnd);
                    multiwords.put(tokens[0], mwEnds);
                }
            }
            log.info(pos.getKey() + " multiwords: " + count);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    public static void writeNominalizations() throws SMatchException {
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
                Pointer[] pointers = source.getPointers(PointerType.NOMINALIZATION);
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
            writeObject(keysArr, MatchManager.nominalizationsFile);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    public static void writeSynonymsAdj() throws SMatchException {
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
                Pointer[] pointers = source.getPointers(PointerType.SIMILAR_TO);
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
            writeObject(keysArr, MatchManager.adjectiveSynonymFile);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    public static void writeOppAdverbs() throws SMatchException {
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
                Pointer[] pointers = source.getPointers(PointerType.ANTONYM);
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
            writeObject(keysArr, MatchManager.adverbsAntonymFile);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    public static void writeOppAdjectives() throws SMatchException {
        log.info("Creating adjective antonyms array...");
        HashSet<Long> keys = new HashSet<Long>();

        PointerUtils pu = PointerUtils.getInstance();
        int count = 0;
        try {
            Iterator it = Dictionary.getInstance().getSynsetIterator(POS.ADJECTIVE);
            while (it.hasNext()) {
                count++;
                if (0 == count % 1000) {
                    log.info(count);
                }
                Synset current = (Synset) it.next();
                traverseTree(keys, pu.getExtendedAntonyms(current), current.getOffset());
                traverseListSym(keys, pu.getAntonyms(current), current.getOffset());
            }
            log.info("Adjective antonyms: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            writeObject(keysArr, MatchManager.adjectiveAntonymFile);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    public static void writeOppNouns() throws SMatchException {
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
            writeObject(keysArr, MatchManager.nounAntonymFile);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    public static void writeNounMG() throws SMatchException {
        log.info("Creating noun mg array...");
        HashSet<Long> keys = new HashSet<Long>();

        PointerUtils pu = PointerUtils.getInstance();
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
                traverseTreeMG(keys, pu.getHypernymTree(source), sourceOffset);
                traverseTreeMG(keys, pu.getInheritedHolonyms(source), sourceOffset);
                traverseTreeMG(keys, pu.getInheritedMemberHolonyms(source), sourceOffset);
                traverseTreeMG(keys, pu.getInheritedPartHolonyms(source), sourceOffset);
                traverseTreeMG(keys, pu.getInheritedSubstanceHolonyms(source), sourceOffset);
                traverseListMG(keys, pu.getHolonyms(source), sourceOffset);
                traverseListMG(keys, pu.getMemberHolonyms(source), sourceOffset);
                traverseListMG(keys, pu.getPartHolonyms(source), sourceOffset);
                traverseListMG(keys, pu.getSubstanceHolonyms(source), sourceOffset);
            }
            log.info("Noun mg: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            writeObject(keysArr, MatchManager.nounMGFile);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    public static void writeVerbMG() throws SMatchException {
        log.info("Creating verb mg array...");
        HashSet<Long> keys = new HashSet<Long>();

        PointerUtils pu = PointerUtils.getInstance();
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
                traverseTreeMG(keys, pu.getHypernymTree(source), sourceOffset);
            }
            log.info("Verb mg: " + keys.size());

            long[] keysArr = new long[keys.size()];
            int i = 0;
            for (Long key : keys) {
                keysArr[i] = key;
                i++;
            }
            Arrays.sort(keysArr);
            writeObject(keysArr, MatchManager.verbMGFile);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    private static void cartPr(HashSet<Long> keys, Pointer[] t) throws SMatchException {
        try {
            for (int i = 0; i < t.length; i++) {
                Pointer ps = t[i];
                long sourceOffset = ps.getTargetSynset().getOffset();
                for (int j = i + 1; j < t.length; j++) {
                    Pointer pt = t[j];
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
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
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

    /**
     * Writes Java object to a file.
     *
     * @param object   the object
     * @param fileName the file where the object will be written
     * @throws SMatchException SMatchException
     */
    private static void writeObject(Object object, String fileName) throws SMatchException {
        log.info("Writing " + fileName);
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        } catch (IOException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }
}
