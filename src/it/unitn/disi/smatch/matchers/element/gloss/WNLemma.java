package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.ISenseGlossBasedElementLevelSemanticMatcher;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.Vector;

/**
 * Implements WNLemma matcher.
 * See Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */

public class WNLemma implements ISenseGlossBasedElementLevelSemanticMatcher {

	/**
	 * Computes the relation with WordNet lemma matcher.
	 *
	 * @param source the gloss of source
	 * @param target the gloss of target
	 * @return synonym or IDk relation
	 */
	public char match(ISynset source, ISynset target) {
        Vector<String> sourceLemmas = source.getLemmas();
        Vector<String> targetLemmas = target.getLemmas();
        for (int i = 0; i < sourceLemmas.size(); i++) {
            String sourceLemma = sourceLemmas.get(i);
            for (int j = 0; j < targetLemmas.size(); j++) {
                String targetLemma = targetLemmas.get(j);
                if (sourceLemma.equals(targetLemma))
                    return MatchManager.SYNOMYM;
            }
        }
        return MatchManager.IDK_RELATION;
    }
}
