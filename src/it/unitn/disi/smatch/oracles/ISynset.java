package it.unitn.disi.smatch.oracles;

import java.util.List;

/**
 * Interface for synsets.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface ISynset {

    /**
     * Returns a synset gloss, that is a textual description of the meaning of the synset.
     *
     * @return a gloss
     */
    String getGloss();

    /**
     * Get lemmas of this synset.
     *
     * @return lemmas
     */
    List<String> getLemmas();

    /**
     * Returns "parents", that is hypernyms of the synset.
     *
     * @return hypernyms of the synset
     * @throws LinguisticOracleException LinguisticOracleException
     */
    List<ISynset> getParents() throws LinguisticOracleException;

    /**
     * Returns "parents", that is hypernyms of the synset, up to certain depth.
     *
     * @param depth a search depth
     * @return "parents"
     * @throws LinguisticOracleException LinguisticOracleException
     */
    List<ISynset> getParents(int depth) throws LinguisticOracleException;

    /**
     * Returns "children", that is hyponyms of the synset.
     *
     * @return "children"
     * @throws LinguisticOracleException LinguisticOracleException
     */
    List<ISynset> getChildren() throws LinguisticOracleException;

    /**
     * Returns "children", that is hyponyms of the synset, down to certain depth.
     *
     * @param depth a search depth
     * @return "children"
     * @throws LinguisticOracleException LinguisticOracleException
     */
    List<ISynset> getChildren(int depth) throws LinguisticOracleException;
}