package it.unitn.disi.smatch.matchers.element.string;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.IStringBasedElementLevelSemanticMatcher;

import java.util.Arrays;
import java.util.HashSet;

/**
 * implements Prefix matcher
 * see Element Level Semantic matchers paper for more details
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Prefix implements IStringBasedElementLevelSemanticMatcher {
    private static int invocationCount = 0;
    private static int relCount = 0;
    private static HashSet<String> hm = new HashSet<String>();


    public char match(String str1, String str2) {
        invocationCount++;
        char rel = MatchManager.IDK_RELATION;

        if (str1 == null || str2 == null) {
            rel = MatchManager.IDK_RELATION;
        } else {
            if ((str1.length() > 3) && (str2.length() > 3)) {
                if (str1.startsWith(str2)) {
                    if (str1.indexOf(" ") > -1) {
                        rel = MatchManager.LESS_GENERAL_THAN;
                    } else {
                        rel = MatchManager.SYNOMYM;
                    }
                } else {
                    if (str2.startsWith(str1)) {
                        if (str2.indexOf(" ") > -1) {
                            rel = MatchManager.MORE_GENERAL_THAN;
                        } else {
                            rel = MatchManager.SYNOMYM;
                        }
                    }
                }
            }//if ((str1.length() > 3) && (str2.length() > 3)) {
        }

        if (rel != MatchManager.IDK_RELATION) {
            relCount++;
            addCase(str1, str2, rel);
        }
        return rel;
    }


    public void reportUsage() {
        System.out.println("Prefix");
        System.out.println("Prefix rel count = " + relCount);
        System.out.println("Prefix invocation count = " + invocationCount);
        String[] arr = new String[hm.size()];
        hm.toArray(arr);
        Arrays.sort(arr);
        for (String entry : arr) {
            System.out.println(entry);
        }
        System.out.println("Prefix rel count = " + hm.size());
        System.out.println("Prefix");
    }

    private void addCase(String lstr1, String lstr2, char rel) {
        if (lstr1.compareTo(lstr2) < 0) {
            if (!hm.contains(lstr1 + rel + lstr2)) {
                hm.add(lstr1 + rel + lstr2);
            }
        } else {
            if (rel == MatchManager.MORE_GENERAL_THAN) {
                rel = MatchManager.LESS_GENERAL_THAN;
            } else {
                if (rel == MatchManager.LESS_GENERAL_THAN) {
                    rel = MatchManager.MORE_GENERAL_THAN;
                }
            }
            if (!hm.contains(lstr2 + rel + lstr1)) {
                hm.add(lstr2 + rel + lstr1);
            }
        }
    }
}
