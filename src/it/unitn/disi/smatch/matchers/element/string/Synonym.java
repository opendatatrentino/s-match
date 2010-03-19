package it.unitn.disi.smatch.matchers.element.string;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.IStringBasedElementLevelSemanticMatcher;

/**
 * Implements Synonym matcher.
 * See Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Synonym implements IStringBasedElementLevelSemanticMatcher {

	/**
     * Computes relation with synonym matcher
     *
     * @param str1 the source string
	 * @param str2 the target string
	 * @return synonym or IDK relation
     */
	public char match(String str1, String str2) {
        if (str1 == null || str2 == null)
            return MatchManager.IDK_RELATION;
        if (str1.equals(str2)) {
            return MatchManager.SYNOMYM;
        } else
            return MatchManager.IDK_RELATION;
    }
}
