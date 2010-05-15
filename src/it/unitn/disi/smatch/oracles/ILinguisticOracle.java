package it.unitn.disi.smatch.oracles;

import it.unitn.disi.smatch.components.IConfigurable;

import java.util.List;

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
     * @throws LinguisticOracleException LinguisticOracleException
     */
    public ISynset getISynset(String source) throws LinguisticOracleException;

    /**
     * Checks if lemmas of two strings are equal (e. g. the string are the same modulo inflections).
     *
     * @param str1 source string
     * @param str2 target string
     * @return true if lemmas are equal
     * @throws LinguisticOracleException LinguisticOracleException
     */
    public boolean isEqual(String str1, String str2) throws LinguisticOracleException;

    /**
     * Returns all senses of a word.
     *
     * @param word the word to which the sense will be retrieve
     * @return word senses
     * @throws LinguisticOracleException LinguisticOracleException
     */
    public List<String> getSenses(String word) throws LinguisticOracleException;

    /**
     * Returns base form (lemma) of a word.
     *
     * @param derivation the word to get a base form for
     * @return base form of a derivation
     * @throws LinguisticOracleException LinguisticOracleException
     */
    public String getBaseForm(String derivation) throws LinguisticOracleException;
}