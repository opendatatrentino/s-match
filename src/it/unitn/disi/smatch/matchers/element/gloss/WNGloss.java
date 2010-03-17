package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Implements WNGloss matcher.
 * See Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNGloss implements ISenseGlossBasedElementLevelSemanticMatcher {

    static int threshold = 1;

    /**
     * Computes the relations with WordNet gloss matcher.
     *
     * @param source gloss of source
     * @param target gloss of target
     * @return more general, less general or IDK relation
     */
    public char match(ISynset source, ISynset target) {
        String sSynset = source.getGloss();
        String tSynset = target.getGloss();
        StringTokenizer stSource = new StringTokenizer(sSynset, " ,.\"'();");
        StringTokenizer stTarget = new StringTokenizer(tSynset, " ,.\"'();");
        String lemma;
        String lemmaToCompare;
        int counter = 0;
        while (stSource.hasMoreTokens()) {
            lemma = stSource.nextToken();
            if (MatchManager.meaninglessWords.indexOf(lemma) == -1) {
                Vector<String> lemmas = target.getLemmas();
                for (int i = 0; i < lemmas.size(); i++) {
                    lemmaToCompare = lemmas.get(i);
                    if (lemma.equals(lemmaToCompare))
                        counter++;
                }
            }
        }
        if (counter >= threshold) {
            return MatchManager.LESS_GENERAL_THAN;
        }

        while (stTarget.hasMoreTokens()) {
            lemma = stTarget.nextToken();
            if (MatchManager.meaninglessWords.indexOf(lemma) == -1) {
                Vector<String> lemmas = source.getLemmas();
                for (int i = 0; i < lemmas.size(); i++) {
                    lemmaToCompare = lemmas.get(i);
                    if (lemma.equals(lemmaToCompare))
                        counter++;
                }
            }
        }
        if (counter >= threshold) {
            return MatchManager.MORE_GENERAL_THAN;
        }
        return MatchManager.IDK_RELATION;
    }
    // TODO more than one main function. It is confusing
    public static void main(String[] args) {
        WNSemanticGlossComparison wng = new WNSemanticGlossComparison();
        ILinguisticOracle ILO = MatchManager.getLinguisticOracle();

        ISynset sourceSynset = ILO.getISynset("n#2004548");
        ISynset targetSynset = ILO.getISynset("n#2001223");

        System.out.println(wng.match(sourceSynset, targetSynset));
    }


}
