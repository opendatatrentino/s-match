package it.unitn.disi.smatch.utils;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Converts WordNet to binary structures optimized for fast searches
 * and used by InMemoryWordNetBinaryArray and preprocessors.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class GenerateWordNetCaches {

    private static final Logger log = Logger.getLogger(GenerateWordNetCaches.class);

    /**
     * Convert WordNet dictionary to binary structures and save them to caches.
     *
     * @throws SMatchException
     */
    public void convert() throws SMatchException {
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

    private void writeMultiwords() throws SMatchException {
        log.info("Creating multiword hash...");
        Hashtable<String, Vector<Vector<String>>> multiwords = new Hashtable<String, Vector<Vector<String>>>();
        POS[] parts = new POS[]{POS.NOUN, POS.ADJECTIVE, POS.VERB, POS.ADVERB};
        for (POS pos : parts) {
            collectMultiwords(multiwords, pos);
        }
        log.info("Multiwords: " + multiwords.size());
        writeObject(multiwords, MatchManager.multiwordsFileName);
    }

    private void collectMultiwords(Hashtable<String, Vector<Vector<String>>> multiwords, POS pos) throws SMatchException {
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

    public void writeNominalizations() throws SMatchException {
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

    public void writeSynonymsAdj() throws SMatchException {
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

    public void writeOppAdverbs() throws SMatchException {
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

    public void writeOppAdjectives() throws SMatchException {
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

    public void writeOppNouns() throws SMatchException {
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

    public void writeNounMG() throws SMatchException {
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

    public void writeVerbMG() throws SMatchException {
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

    private void cartPr(HashSet<Long> keys, Pointer[] t) throws SMatchException {
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

    public void traverseListMG(HashSet<Long> keys, PointerTargetNodeList pointers, long sourceOffset) {
        for (Object pointer : pointers) {
            long targetOffset = ((PointerTargetNode) pointer).getSynset().getOffset();
            if (sourceOffset != targetOffset) {
                long key = (sourceOffset << 32) + targetOffset;
                keys.add(key);
            }
        }
    }

    public void traverseListSym(HashSet<Long> keys, PointerTargetNodeList pointers, long sourceOffset) {
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

    public void traverseTreeMG(HashSet<Long> keys, PointerTargetTree syn, long sourceOffset) {
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

    public void traverseTree(HashSet<Long> keys, PointerTargetTree syn, long sourceOffset) {
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
     * Writes specific WordNet cache such as multiwords to corresponding file.
     *
     * @param object the cache object
     * @param fileName the file where the object will be written
     * @throws SMatchException
     */
    private void writeObject(Object object, String fileName) throws SMatchException {
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
