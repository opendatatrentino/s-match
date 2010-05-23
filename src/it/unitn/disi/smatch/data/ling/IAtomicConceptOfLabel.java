package it.unitn.disi.smatch.data.ling;

import java.util.List;

/**
 * An interface for implementation of atomic concept of label.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IAtomicConceptOfLabel {

    /**
     * Returns the token(s) corresponding to the atomic concept.
     *
     * @return the token(s) corresponding to the atomic concept
     */
    String getToken();

    /**
     * Sets the token(s) corresponding to the atomic concept.
     *
     * @param token token(s) corresponding to the atomic concept.
     */
    void setToken(String token);

    /**
     * Returns lemmatized version of the token(s).
     *
     * @return lemmatized version of the token(s)
     */
    String getLemma();

    /**
     * Sets lemmatized version of the token(s).
     *
     * @param lemma lemmatized version of the token(s)
     */
    void setLemma(String lemma);

    /**
     * Returns token identifier in the label. In most cases equals to token index.
     *
     * @return token identifier in the label
     */
    int getId();

    /**
     * Sets token identifier in the label. In most cases equals to token index.
     *
     * @param id token identifier in the label
     */
    void setId(int id);

    ISensesSet getSenses();

    void addSenses(List<String> senseList);

    String toString();
}