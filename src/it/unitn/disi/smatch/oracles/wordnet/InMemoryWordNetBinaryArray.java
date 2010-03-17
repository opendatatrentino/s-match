package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.oracles.IWordNetMatcher;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

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
}
