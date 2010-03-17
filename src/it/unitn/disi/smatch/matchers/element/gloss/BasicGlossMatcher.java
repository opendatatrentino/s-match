package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISynset;
import it.unitn.disi.smatch.oracles.IWordNetMatcher;

import java.util.Vector;

/**
 * Matches glosses of word senses.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class BasicGlossMatcher {
    private static ILinguisticOracle ILO = null;
    private static IWordNetMatcher IWNM = null;

    public BasicGlossMatcher() {
        ILO = MatchManager.getLinguisticOracle();
        IWNM = MatchManager.getIWNMatcher();
    }

    //Next 4 method are used by element level matchers to calculate relations between words

    /**
     * Checks the source is more general than the target or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if the source is more general than target
     */
    public boolean isWordMoreGeneral(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
        sSenses = ILO.getSenses(source);
        tSenses = ILO.getSenses(target);
        if ((sSenses != null) && (tSenses != null))
            if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                for (int i = 0; i < sSenses.size(); i++) {
                    String sSense = (String) sSenses.get(i);
                    for (int j = 0; j < tSenses.size(); j++) {
                        String tSense = (String) tSenses.get(j);
                        if (IWNM.isSourceMoreGeneralThanTarget(sSense, tSense))
                            return true;
                    }
                }
            }
        return false;
    }

    /**
     * Checks the source is less general than the target or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if the source is less general than target
     */
    public boolean isWordLessGeneral(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
        sSenses = ILO.getSenses(source);
        tSenses = ILO.getSenses(target);
        if ((sSenses != null) && (tSenses != null))
            if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                for (int i = 0; i < sSenses.size(); i++) {
                    String sSense = sSenses.get(i);
                    for (int j = 0; j < tSenses.size(); j++) {
                        String tSense = (String) tSenses.get(j);
                        if (IWNM.isSourceLessGeneralThanTarget(sSense, tSense))
                            return true;
                    }
                }
            }
        return false;
    }

    /**
     * Checks the source and target is synonym or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if they are synonym
     */
    public boolean isWordSynonym(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
        sSenses = ILO.getSenses(source);
        tSenses = ILO.getSenses(target);

        if ((sSenses != null) && (tSenses != null))
            if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                for (int i = 0; i < sSenses.size(); i++) {
                    String sSense = sSenses.get(i);
                    for (int j = 0; j < tSenses.size(); j++) {
                        String tSense = (String) tSenses.get(j);
                        if (IWNM.isSourceSynonymTarget(sSense, tSense))
                            return true;
                    }
                }
            }
        return false;
    }

    /**
     * Checks the source and target is opposite or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if they are in opposite relation
     */
    public boolean isWordOpposite(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
        sSenses = ILO.getSenses(source);
        tSenses = ILO.getSenses(target);
        if ((sSenses != null) && (tSenses != null))
            if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                for (String sSense : sSenses) {
                    for (String tSense : tSenses) {
                        if (IWNM.isSourceOppositeToTarget(sSense, tSense))
                            return true;
                    }
                }
            }
        return false;
    }

    /**
     * Gets extended gloss i.e. the gloss of parents or children. <br>
     * The direction and depth is according to requirement.
     *
     * @param original the original gloss of input string
     * @param intSource how much depth the gloss should be taken
     * @param Rel for less than relation get child gloss and vice versa
     * @return the extended gloss
     */
    public String getExtendedGloss(ISynset original, int intSource, char Rel) {
        Vector<ISynset> children = new Vector<ISynset>();
        String result = "";
        if (Rel == MatchManager.LESS_GENERAL_THAN) {
            children = original.getChildren(intSource);//getAncestors(original, children, intSource);
        } else if (Rel == MatchManager.MORE_GENERAL_THAN) {
            children = original.getParents(intSource);//getDescendants(original, children, intSource);
        }
        for (int i = 0; i < children.size(); i++) {
            ISynset iSynset = children.get(i);
            String gloss = iSynset.getGloss();
            result = result + gloss + ".";
        }
        return result;
    }

//    private Vector<ISynset> getAncestors(ISynset node, Vector<ISynset> ve, int depth) {
//        if (depth > 0) {
//            Vector<ISynset> tmp = node.getChildren();
//            tmp.addAll(ve);
//            for (int i = 0; i < tmp.size(); i++) {
//                ISynset iSynset = tmp.get(i);
//                tmp.addAll(getAncestors(iSynset, tmp, depth - 1));
//            }
//            return tmp;
//
//        }
//        return (new Vector<ISynset>());
//    }
//
//    private Vector<ISynset> getDescendants(ISynset node, Vector<ISynset> ve, int depth) {
//        if (depth > 0) {
//            Vector<ISynset> tmp = node.getChildren();
//            tmp.addAll(ve);
//            for (int i = 0; i < tmp.size(); i++) {
//                ISynset iSynset = tmp.get(i);
//                tmp.addAll(getDescendants(iSynset, tmp, depth - 1));
//            }
//            return tmp;
//
//        }
//        return (new Vector<ISynset>());
//    }


}
