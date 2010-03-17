package it.unitn.disi.smatch.oracles;

import java.util.Vector;

/**
 * Interface to Linguistic Oracle.
 */
public interface ILinguisticOracle {

    /**
     * Returns a synset given its id.
     *
     * @param source synset id
     * @return synset
     */
    public ISynset getISynset(String source);

    /**
     * Checks if lemmas of two strings are equal (e. g. the string are the same modulo inflections).
     *
     * @param str1 source string
     * @param str2 target string
     * @return true if lemmas are equal
     */
    public boolean isEqual(String str1, String str2);

    /**
     * Returns all senses of a word.
     *
     * @param word the word to which the sense will be retrieve
     * @return word senses
     */
    public Vector<String> getSenses(String word);

    /**
     * Returns base form (lemma) of a word.
     *
     * @param deriviation the word need to deriviate as lemma
     * @return base form of a derivation
     */
    public String getBaseForm(String deriviation);

}
