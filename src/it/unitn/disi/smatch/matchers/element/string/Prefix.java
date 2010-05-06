package it.unitn.disi.smatch.matchers.element.string;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.matchers.element.IStringBasedElementLevelSemanticMatcher;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Implements Prefix matcher.
 * See Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Prefix extends Configurable implements IStringBasedElementLevelSemanticMatcher {
    private static int invocationCount = 0;
    private static int relCount = 0;
    private static HashSet<String> hm = new HashSet<String>();

    /**
     * Computes the relation with prefix matcher.
     *
     * @param str1 the source string
     * @param str2 the target string
     * @return synonym, more general, less general or IDK relation
     */
    public char match(String str1, String str2) {
        invocationCount++;
        char rel = IMappingElement.IDK;

        if (str1 == null || str2 == null) {
            rel = IMappingElement.IDK;
        } else {
            if ((str1.length() > 3) && (str2.length() > 3)) {
                if (str1.startsWith(str2)) {
                    if (str1.indexOf(" ") > -1) {
                        rel = IMappingElement.LESS_GENERAL;
                    } else {
                        rel = IMappingElement.EQUIVALENCE;
                    }
                } else {
                    if (str2.startsWith(str1)) {
                        if (str2.indexOf(" ") > -1) {
                            rel = IMappingElement.MORE_GENERAL;
                        } else {
                            rel = IMappingElement.EQUIVALENCE;
                        }
                    }
                }
            }//if ((str1.length() > 3) && (str2.length() > 3)) {
        }

        if (rel != IMappingElement.IDK) {
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
            if (rel == IMappingElement.MORE_GENERAL) {
                rel = IMappingElement.LESS_GENERAL;
            } else {
                if (rel == IMappingElement.LESS_GENERAL) {
                    rel = IMappingElement.MORE_GENERAL;
                }
            }
            if (!hm.contains(lstr2 + rel + lstr1)) {
                hm.add(lstr2 + rel + lstr1);
            }
        }
    }
}
