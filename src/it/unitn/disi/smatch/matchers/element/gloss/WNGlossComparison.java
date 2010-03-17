package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.StringTokenizer;

/**
 * Implements WNGlossComparison matcher.
 * See Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNGlossComparison implements ISenseGlossBasedElementLevelSemanticMatcher {

    static int threshold = 2;

    /**
     * Computes the relations with WordNet gloss comparison matcher.
     *
     * @param source gloss of source
     * @param target gloss of target
     * @return synonym or IDK relation
     */
    public char match(ISynset source, ISynset target) {
        String sSynset = source.getGloss();
        String tSynset = target.getGloss();
        StringTokenizer stSource = new StringTokenizer(sSynset, " ,.\"'();");
        String lemmaS, lemmaT;
        int counter = 0;
        while (stSource.hasMoreTokens()) {
            StringTokenizer stTarget = new StringTokenizer(tSynset, " ,.\"'();");
            lemmaS = stSource.nextToken();
            if (MatchManager.meaninglessWords.indexOf(lemmaS) == -1)
                while (stTarget.hasMoreTokens()) {
                    lemmaT = stTarget.nextToken();
                    if (MatchManager.meaninglessWords.indexOf(lemmaT) == -1)
                        if (lemmaS.equals(lemmaT))
                            counter++;
                }
        }
        if (counter >= threshold)
            return MatchManager.SYNOMYM;
        else
            return MatchManager.IDK_RELATION;
    }

}
