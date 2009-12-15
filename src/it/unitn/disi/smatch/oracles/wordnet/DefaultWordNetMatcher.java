package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.oracles.IWordNetMatcher;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Default matcher, queries WordNet via JWNL library.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DefaultWordNetMatcher implements IWordNetMatcher {

    public DefaultWordNetMatcher() {
        sensesCash = new Hashtable<String, Character>();
    }

    //  Hashtable which cashes relations between sences already retrieved from WN
    private Hashtable<String, Character> sensesCash;

    public char getRelation(Vector<String> sourceSenses, Vector<String> targetSenses) {
        String sourceSense;
        String targetSense;
        for (int i = 0; i < sourceSenses.size(); i++) {
            sourceSense = (sourceSenses.get(i));
            for (int j = 0; j < targetSenses.size(); j++) {
                targetSense = (targetSenses.get(j));
                //Use cashed oracle relations to speed up search
                if (getRelationFromOracle(sourceSense, targetSense, MatchManager.SYNOMYM)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    return MatchManager.SYNOMYM;
                }
            }
        }
        //  Check for less general than
        for (int i = 0; i < sourceSenses.size(); i++) {
            sourceSense = (sourceSenses.get(i));
            for (int j = 0; j < targetSenses.size(); j++) {
                targetSense = (String) (targetSenses.get(j));
                //Use cashed oracle relations to speed up search
                if (getRelationFromOracle(sourceSense, targetSense, MatchManager.LESS_GENERAL_THAN)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    return MatchManager.LESS_GENERAL_THAN;
                }
            }
        }
        //  Check for more general than
        for (int i = 0; i < sourceSenses.size(); i++) {
            sourceSense = (String) (sourceSenses.get(i));
            for (int j = 0; j < targetSenses.size(); j++) {
                targetSense = (targetSenses.get(j));
                //Use cashed oracle relations to speed up search
                if (getRelationFromOracle(sourceSense, targetSense, MatchManager.MORE_GENERAL_THAN)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    return MatchManager.MORE_GENERAL_THAN;
                }
            }
        }
        //  Check for opposite meaning
        for (int i = 0; i < sourceSenses.size(); i++) {
            sourceSense = ((String) sourceSenses.get(i));
            for (int j = 0; j < targetSenses.size(); j++) {
                targetSense = ((String) targetSenses.get(j));
                //Use cashed oracle relations to speed up search
                if (getRelationFromOracle(sourceSense, targetSense, MatchManager.OPPOSITE_MEANING)) {
                    MatchManager.retainValue(sourceSenses, sourceSense);
                    MatchManager.retainValue(targetSenses, targetSense);
                    return MatchManager.OPPOSITE_MEANING;
                }
            }
        }
        return MatchManager.IDK_RELATION;
    }

    public char getRelationACoL(IAtomicConceptOfLabel source, IAtomicConceptOfLabel target) {
        Vector<String> sourceSenses = source.getSenses().getSenseList();
        Vector<String> targetSenses = target.getSenses().getSenseList();
        return getRelation(sourceSenses, targetSenses);
    }

    /**
     * Method which returns whether particular type of relation between
     * two senses holds(according to oracle)
     * It uses cache to store already obtained relations in order to improve performance
     *
     * @param source
     * @param target
     * @param rel
     * @return
     */
    private boolean getRelationFromOracle(String source, String target, char rel) {
        char tmp;
        //  If we don't have cashed relation check which one exist and put it to cash
        if (sensesCash.get(source + target) == null) {
            //  Check for synonymy
            if (isSourceSynonymTarget(source, target)) {
                sensesCash.put(source + target, MatchManager.SYNOMYM);
                return rel == MatchManager.SYNOMYM;
            } else {
                //  Check for opposite meaning
                if (isSourceOppositeToTarget(source, target)) {
                    sensesCash.put(source + target, MatchManager.OPPOSITE_MEANING);
                    return rel == MatchManager.OPPOSITE_MEANING;
                } else {
                    //  Check for less general than
                    if (isSourceLessGeneralThanTarget(source, target)) {
                        sensesCash.put(source + target, MatchManager.LESS_GENERAL_THAN);
                        return rel == MatchManager.LESS_GENERAL_THAN;
                    } else {
                        //  Check for more general than
                        if (isSourceMoreGeneralThanTarget(source, target)) {
                            sensesCash.put(source + target, MatchManager.MORE_GENERAL_THAN);
                            return rel == MatchManager.MORE_GENERAL_THAN;
                        } else {
                            sensesCash.put(source + target, MatchManager.IDK_RELATION);
                            return false;
                        }
                    }
                }
            }
            //  Return relation already stored in cash
        } else {
            tmp = (sensesCash.get(source + target));
            return rel == tmp;
        }
    }

    public boolean isSourceSynonymTarget(String source, String target) {
        if ((source.indexOf("000000") == -1) && (target.indexOf("000000") == -1)) {
            if ((source.indexOf(MatchManager.UNKNOWN_MEANING) > -1) || (target.indexOf(MatchManager.UNKNOWN_MEANING) > -1)) {
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
            if ((source.indexOf(MatchManager.UNKNOWN_MEANING) > -1) || (target.indexOf(MatchManager.UNKNOWN_MEANING) > -1)) {
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
     * Checks whether source sence less general than target
     * Currently used version of Java WordNet Interface Library finds more general relationships
     * (hypernymy and holonymy) faster than less general so this method
     * just flip the parameters and call isSourceMoreGeneralThanTarget method
     *
     * @param source
     * @param target
     * @return
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
                if ((source.indexOf(MatchManager.UNKNOWN_MEANING) > -1) || (target.indexOf(MatchManager.UNKNOWN_MEANING) > -1)) {
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
     * traverses PointerTargetTree
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
     * Check unidirectionality of semantic relations in the list
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
                return MatchManager.getWordNetDictionary().getSynsetAt(POSSource, lSourseID);
            }
        } catch (Exception ex) {
            System.err.println(source);
            ex.printStackTrace();
        }
        return null;
    }


}
