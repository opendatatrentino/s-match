package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.StringTokenizer;

/**
 * Implements WNExtendedSemanticGlossComparison matcher.
 * see Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNExtendedSemanticGlossComparison extends BasicGlossMatcher implements ISenseGlossBasedElementLevelSemanticMatcher {

	/**
     * Computes the relation for extended semantic gloss matcher.
     *
     * @param source1 the gloss of source
     * @param target1 the gloss of target
     * @return more general, less general or IDK relation
     */
    public char match(ISynset source1, ISynset target1) {
        String sSynset = source1.getGloss();
        String tSynset = target1.getGloss();

        // get gloss of Immediate ancestor of target node
        String tLGExtendedGloss = getExtendedGloss(target1, 1, MatchManager.LESS_GENERAL_THAN);
        // get relation frequently occur between gloss of source and extended gloss of target
        char LGRel = getDominantRelation(sSynset, tLGExtendedGloss);
        // get final relation
        char LGFinal = getRelationFromRels(MatchManager.LESS_GENERAL_THAN, LGRel);
        // get gloss of Immediate descendant of target node
        String tMGExtendedGloss = getExtendedGloss(target1, 1, MatchManager.MORE_GENERAL_THAN);
        char MGRel = getDominantRelation(sSynset, tMGExtendedGloss);
        char MGFinal = getRelationFromRels(MatchManager.MORE_GENERAL_THAN, MGRel);
        // Compute final relation
        if (MGFinal == LGFinal)
            return MGFinal;
        if (MGFinal == MatchManager.IDK_RELATION)
            return LGFinal;
        if (LGFinal == MatchManager.IDK_RELATION)
            return MGFinal;
        return MatchManager.IDK_RELATION;
    }

    /**
     * Gets Semantic relation occurring more frequently between words in two extended glosses.
     *
     * @param sExtendedGloss extended gloss of source
     * @param tExtendedGloss extended gloss of target
     * @return more general, less general or IDK relation
     */
    private char getDominantRelation(String sExtendedGloss, String tExtendedGloss) {
        int Equals = 0;
        int moreGeneral = 0;
        int lessGeneral = 0;
        int Opposite = 0;
        StringTokenizer stSource = new StringTokenizer(sExtendedGloss, " ,.\"'()");
        String lemmaS, lemmaT;
        int counter = 0;
        while (stSource.hasMoreTokens()) {
            StringTokenizer stTarget = new StringTokenizer(tExtendedGloss, " ,.\"'()");
            lemmaS = stSource.nextToken();
            if (MatchManager.meaninglessWords.indexOf(lemmaS) == -1)
                while (stTarget.hasMoreTokens()) {
                    lemmaT = stTarget.nextToken();
                    if (MatchManager.meaninglessWords.indexOf(lemmaT) == -1) {
                        if (isWordLessGeneral(lemmaS, lemmaT))
                            lessGeneral++;
                        else if (isWordMoreGeneral(lemmaS, lemmaT))
                            moreGeneral++;
                        else if (isWordSynonym(lemmaS, lemmaT))
                            Equals++;
                        else if (isWordOpposite(lemmaS, lemmaT))
                            Opposite++;
                    }
                }
        }
        return getRelationFromInts(lessGeneral, moreGeneral, Equals, Opposite);
    }

    /**
     * Decides which relation to return.
     *
     * @param lg number of less general words between two extended gloss
     * @param mg number of more general words between two extended gloss
     * @param syn number of synonym words between two extended gloss
     * @param opp number of opposite words between two extended gloss
     * @return the more frequent relation between two extended glosses.
     */
    private char getRelationFromInts(int lg, int mg, int syn, int opp) {
        if ((lg >= mg) && (lg >= syn) && (lg >= opp) && (lg > 0))
            return MatchManager.LESS_GENERAL_THAN;
        if ((mg >= lg) && (mg >= syn) && (mg >= opp) && (mg > 0))
            return MatchManager.MORE_GENERAL_THAN;
        if ((syn >= mg) && (syn >= lg) && (syn >= opp) && (syn > 0))
            return MatchManager.LESS_GENERAL_THAN;
        if ((opp >= mg) && (opp >= syn) && (opp >= lg) && (opp > 0))
            return MatchManager.LESS_GENERAL_THAN;
        return MatchManager.IDK_RELATION;
    }

    /**
     * Decides which relation to return as a function of relation for which extended gloss was built.
     *
     * @param builtForRel
     * @param glossRel
     * @return less general, more general or IDK relation
     */
    // TODO Need comments about parameters
    private char getRelationFromRels(char builtForRel, char glossRel) {
        if (builtForRel == MatchManager.SYNOMYM)
            return glossRel;
        if (builtForRel == MatchManager.LESS_GENERAL_THAN)
            if ((glossRel == MatchManager.LESS_GENERAL_THAN) || (glossRel == MatchManager.SYNOMYM))
                return MatchManager.LESS_GENERAL_THAN;
        if (builtForRel == MatchManager.MORE_GENERAL_THAN)
            if ((glossRel == MatchManager.MORE_GENERAL_THAN) || (glossRel == MatchManager.SYNOMYM))
                return MatchManager.MORE_GENERAL_THAN;
        return MatchManager.IDK_RELATION;
    }


}
