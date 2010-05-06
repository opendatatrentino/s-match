package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.Properties;
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

    private static final String THRESHOLD_KEY = "threshold";
    private int threshold = 5;

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

            properties = newProperties;
        }
    }

    /**
     * Computes the relation for extended gloss matcher.
     *
     * @param source1 the gloss of source
     * @param target1 the gloss of target
     * @return synonym or IDK relation
     */
    public char match(ISynset source1, ISynset target1) {
        String tExtendedGloss = getExtendedGloss(target1, 1, IMappingElement.LESS_GENERAL);
        Vector<String> sourceLemmas = source1.getLemmas();
        //variations of this matcher
//        StringTokenizer stSource = new StringTokenizer(tExtendedGloss, " ,.\"'();");
        String lemmaT;
        int counter = 0;
        for (String sourceLemma : sourceLemmas) {
            StringTokenizer stTarget = new StringTokenizer(tExtendedGloss, " ,.\"'();");
            if (meaninglessWords.indexOf(sourceLemma) == -1)
                while (stTarget.hasMoreTokens()) {
                    lemmaT = stTarget.nextToken();
                    if (meaninglessWords.indexOf(lemmaT) == -1) {
                        if (sourceLemma.equalsIgnoreCase(lemmaT)) {
                            counter++;
                        }
                    }
                }
        }
        if (counter > threshold)
            return IMappingElement.EQUIVALENCE;
        else
            return IMappingElement.IDK;
    }

}
