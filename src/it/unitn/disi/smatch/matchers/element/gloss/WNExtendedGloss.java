package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Implements WNExtendedGlossComparison matcher.
 * see Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNExtendedGloss extends BasicGlossMatcher implements ISenseGlossBasedElementLevelSemanticMatcher {

    static int threshold = 5;

    /**
     * Computes the relation for extended gloss matcher.
     *
     * @param source1 the gloss of source
     * @param target1 the gloss of target
     * @return synonym or IDK relation
     */
    public char match(ISynset source1, ISynset target1) {
        String tExtendedGloss = getExtendedGloss(target1, 1, MatchManager.LESS_GENERAL_THAN);
        Vector<String> sourceLemmas = source1.getLemmas();
        //variations of this matcher
//        StringTokenizer stSource = new StringTokenizer(tExtendedGloss, " ,.\"'();");
        String lemmaS, lemmaT;
        int counter = 0;
        for (int i = 0; i < sourceLemmas.size(); i++) {
            lemmaS = sourceLemmas.get(i);
            StringTokenizer stTarget = new StringTokenizer(tExtendedGloss, " ,.\"'();");
            if (MatchManager.meaninglessWords.indexOf(lemmaS) == -1)
                while (stTarget.hasMoreTokens()) {
                    lemmaT = stTarget.nextToken();
                    if (MatchManager.meaninglessWords.indexOf(lemmaT) == -1)
                        if (lemmaS.equalsIgnoreCase(lemmaT))
                            counter++;
                }
        }
        if (counter > threshold)
            return MatchManager.SYNOMYM;
        else
            return MatchManager.IDK_RELATION;
    }

}
