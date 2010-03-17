package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.MatchManager;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Implements TextCorpus matcher.
 * see Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TextCorpus {

	/**
	 * Matches between two string using TextCorpus matcher.
	 *
	 * @param source the string of source label
	 * @param target the string of target label
	 * @param corpus the sentence which is used for matching.
	 * @return synonym or IDK relation
	 */
    public static char match(String source, String target, String corpus) {
        int threshold = 4;
        Vector<Integer> s = new Vector<Integer>();
        Vector<Integer> t = new Vector<Integer>();
        int closeItems = 0;
        int count = 0;
        for (StringTokenizer tokenizer = new StringTokenizer(corpus); tokenizer.hasMoreTokens();) {
            count++;
            String token = tokenizer.nextToken();
            if (token.startsWith(source))
                s.add(new Integer(count));
            if (token.startsWith(target))
                t.add(new Integer(count));
        }
        for (int i = 0; i < s.size(); i++) {
            int si = (s.get(i)).intValue();
            for (int j = 0; j < t.size(); j++) {
                int ti = ((Integer) t.get(j)).intValue();
                if (Math.abs(si - ti) <= threshold)
                    closeItems++;
            }
        }
        if (closeItems > 0)
            return MatchManager.SYNOMYM;
        else
            return MatchManager.IDK_RELATION;
    }

}
