package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.StringTokenizer;

/**
 * implements WNExtendedGlossComparison matcher
 * see Element Level Semantic matchers paper for more details
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNExtendedGlossComparison extends BasicGlossMatcher implements ISenseGlossBasedElementLevelSemanticMatcher {
    
    static int threshold = 5;

    public char match(ISynset source1, ISynset target1) {
        String tExtendedGloss = getExtendedGloss(target1, 1, MatchManager.LESS_GENERAL_THAN);
        String sExtendedGloss = getExtendedGloss(source1, 1, MatchManager.LESS_GENERAL_THAN);
        //variations of this matcher
        StringTokenizer stSource = new StringTokenizer(tExtendedGloss, " ,.\"'();");
        String lemmaS, lemmaT;
        int counter = 0;
        while (stSource.hasMoreTokens()) {
            StringTokenizer stTarget = new StringTokenizer(sExtendedGloss, " ,.\"'();");
            lemmaS = stSource.nextToken();
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
