package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.matchers.element.MatcherLibraryException;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Implements WNGloss matcher.
 * See Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNGloss extends Configurable implements ISenseGlossBasedElementLevelSemanticMatcher {

    private static final String THRESHOLD_KEY = "threshold";
    private int threshold = 1;

    // the words which are cut off from the area of discourse
    public static String MEANINGLESS_WORDS_KEY = "meaninglessWords";
    private String meaninglessWords = "of on to their than from for by in at is are have has the a as with your etc our into its his her which him among those against ";

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            if (newProperties.containsKey(THRESHOLD_KEY)) {
                threshold = Integer.parseInt(newProperties.getProperty(THRESHOLD_KEY));
            }

            if (newProperties.containsKey(MEANINGLESS_WORDS_KEY)) {
                meaninglessWords = newProperties.getProperty(MEANINGLESS_WORDS_KEY) + " ";
            }

            properties.clear();
            properties.putAll(newProperties);
        }
    }

    /**
     * Computes the relations with WordNet gloss matcher.
     *
     * @param source gloss of source
     * @param target gloss of target
     * @return more general, less general or IDK relation
     */
    public char match(ISynset source, ISynset target) throws MatcherLibraryException {
        String sSynset = source.getGloss();
        String tSynset = target.getGloss();
        StringTokenizer stSource = new StringTokenizer(sSynset, " ,.\"'();");
        StringTokenizer stTarget = new StringTokenizer(tSynset, " ,.\"'();");
        String lemma;
        int counter = 0;
        while (stSource.hasMoreTokens()) {
            lemma = stSource.nextToken();
            if (meaninglessWords.indexOf(lemma) == -1) {
                List<String> lemmas = target.getLemmas();
                for (String lemmaToCompare : lemmas) {
                    if (lemma.equals(lemmaToCompare)) {
                        counter++;
                    }
                }
            }
        }
        if (counter >= threshold) {
            return IMappingElement.LESS_GENERAL;
        }

        while (stTarget.hasMoreTokens()) {
            lemma = stTarget.nextToken();
            if (meaninglessWords.indexOf(lemma) == -1) {
                List<String> lemmas = source.getLemmas();
                for (String lemmaToCompare : lemmas) {
                    if (lemma.equals(lemmaToCompare)) {
                        counter++;
                    }
                }
            }
        }
        if (counter >= threshold) {
            return IMappingElement.MORE_GENERAL;
        }
        return IMappingElement.IDK;
    }
}
