package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.StringTokenizer;

/**
 * implements WNExtendedSemanticGlossComparison matcher
 * see Element Level Semantic matchers paper for more details
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNExtendedSemanticGlossComparison extends BasicGlossMatcher implements ISenseGlossBasedElementLevelSemanticMatcher {

    public char match(ISynset source1, ISynset target1) {
        String sSynset = source1.getGloss();
        String tSynset = target1.getGloss();

        String tLGExtendedGloss = getExtendedGloss(target1, 1, MatchManager.LESS_GENERAL_THAN);
        char LGRel = getDominantRelation(sSynset, tLGExtendedGloss);
        char LGFinal = getRelationFromRels(MatchManager.LESS_GENERAL_THAN, LGRel);
        String tMGExtendedGloss = getExtendedGloss(target1, 1, MatchManager.MORE_GENERAL_THAN);
        char MGRel = getDominantRelation(sSynset, tMGExtendedGloss);
        char MGFinal = getRelationFromRels(MatchManager.MORE_GENERAL_THAN, MGRel);
        if (MGFinal == LGFinal)
            return MGFinal;
        if (MGFinal == MatchManager.IDK_RELATION)
            return LGFinal;
        if (LGFinal == MatchManager.IDK_RELATION)
            return MGFinal;
        return MatchManager.IDK_RELATION;
    }

    /**
     * get Semantic relation occuring more frequently between words in two extended glosses
     *
     * @param sExtendedGloss
     * @param tExtendedGloss
     * @return
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
     * decide which relation to return
     *
     * @param lg
     * @param mg
     * @param syn
     * @param opp
     * @return
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
     * Decide which relation to return as a function of relation for which extended gloss was built
     *
     * @param builtForRel
     * @param glossRel
     * @return
     */
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
