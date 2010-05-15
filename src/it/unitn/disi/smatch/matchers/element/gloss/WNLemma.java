package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.matchers.element.MatcherLibraryException;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.List;

/**
 * Implements WNLemma matcher.
 * See Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */

public class WNLemma extends Configurable implements ISenseGlossBasedElementLevelSemanticMatcher {

    /**
     * Computes the relation with WordNet lemma matcher.
     *
     * @param source the gloss of source
     * @param target the gloss of target
     * @return synonym or IDk relation
     */
    public char match(ISynset source, ISynset target) throws MatcherLibraryException {
        List<String> sourceLemmas = source.getLemmas();
        List<String> targetLemmas = target.getLemmas();
        for (String sourceLemma : sourceLemmas) {
            for (String targetLemma : targetLemmas) {
                if (sourceLemma.equals(targetLemma)) {
                    return IMappingElement.EQUIVALENCE;
                }
            }
        }
        return IMappingElement.IDK;
    }
}
