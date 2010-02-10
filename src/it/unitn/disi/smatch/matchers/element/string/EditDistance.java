package it.unitn.disi.smatch.matchers.element.string;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.IStringBasedElementLevelSemanticMatcher;

/**
 * implements Edit Distance matcher
 * see Element Level Semantic matchers paper for more details
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class EditDistance implements IStringBasedElementLevelSemanticMatcher {

    private static int MATCH = 0;
    private static int MISMATCH = 1;
    private static int GAP = 1; // treating gap = mismatch

    public char match(String str1, String str2) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0) {
            return MatchManager.IDK_RELATION;
        }
        float sim = 1 - (float) levenshteinDistance(str1, str2) / java.lang.Math.max(str1.length(), str2.length());
        if (MatchManager.ELSMthreshold <= sim) {
            return MatchManager.SYNOMYM;
        } else {
            return MatchManager.IDK_RELATION;
        }
    }

    /**
     * Calculates edit distance.
     *
     * @param str1 source string
     * @param str2 target string
     * @return edit distance
     */
    private static int levenshteinDistance(String str1, String str2) {
        //Add a dummy character to the beginning of both strings
        str1 = " " + str1;
        str2 = " " + str2;
        int n = str1.length(), m = str2.length();
        int[][] D = new int[n][m];
        D[0][0] = 0;
        int i, j;
        for (i = 1; i < n; i++) D[i][0] = D[i - 1][0] + GAP;//distance(null, null);
        for (j = 1; j < m; j++) D[0][j] = D[0][j - 1] + GAP;//distance(null, null);
        for (i = 1; i < n; i++) {
            for (j = 1; j < m; j++) {
                int m1 = D[i - 1][j] + GAP;//distance(str1.charAt(i), null);
                int m2 = D[i - 1][j - 1] + distance(str1.charAt(i), str2.charAt(j));
                int m3 = D[i][j - 1] + GAP;//distance(null, str2.charAt(j));
                D[i][j] = Math.min(Math.min(m1, m2), m3);
            }
        }
        return D[n - 1][m - 1];
    }

    /**
     * Treats with online mismatch and gap
     *
     * @param a
     * @param b
     * @return
     */
    private static int distance(Character a, Character b) {
        if (null == a || null == b) return GAP;
        if (!a.equals(b)) return MISMATCH;
        return MATCH;
    }

    //TODO move to unit test
//    public static void main(String[] args) {
//        EditDistance ed = new EditDistance();
//        System.out.println(ed.match("Courses/Colledge of arts and sciencies/Earth and Atmospheric Sciencies", "Classes/Colledge of arts and sciencies/Earth Sciencies"));
//    }
}
