package it.unitn.disi.smatch.oracles;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.ling.ISense;

import java.util.List;

/**
 * Interface to Linguistic Oracles, such as WordNet.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface ILinguisticOracle extends IConfigurable {

    /**
     * Returns a synset given its id.
     *
     * @param sense sense
     * @return synset
     * @throws LinguisticOracleException LinguisticOracleException
     */
    public ISynset getISynset(ISense sense) throws LinguisticOracleException;

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
    public List<ISense> getSenses(String word) throws LinguisticOracleException;

    /**
     * Returns base form (lemma) of a word.
     *
     * @param derivation the word to get a base form for
     * @return base form of a derivation
     * @throws LinguisticOracleException LinguisticOracleException
     */
    public String getBaseForm(String derivation) throws LinguisticOracleException;
}