package it.unitn.disi.smatch.oracles;

import it.unitn.disi.smatch.components.IConfigurable;

import java.util.Vector;

/**
 * Interface to Linguistic Oracle, such as WordNet.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ILinguisticOracle extends IConfigurable {

    /**
     * Represents the words whos meaning is unknown. The ones that are out of vocabulary (WordNet).
     */
    String UNKNOWN_MEANING = "n#000000";

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
     * @param derivation the word to get a base form for
     * @return base form of a derivation
     */
    public String getBaseForm(String derivation);
}