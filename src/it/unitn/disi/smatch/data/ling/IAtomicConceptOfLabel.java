package it.unitn.disi.smatch.data.ling;

import java.util.Iterator;
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


    /**
     * Returns the sense at index index.
     *
     * @param index index
     * @return sense at index index
     */
    ISense getSenseAt(int index);

    /**
     * Returns the number of senses.
     *
     * @return the number of senses
     */
    int getSenseCount();

    /**
     * Returns the index of sense in the receivers senses. If the receiver does not contain sense, -1 will be
     * returned.
     *
     * @param sense a sense to search for
     * @return the index of sense in the receivers senses
     */
    int getSenseIndex(ISense sense);

    /**
     * Returns the iterator over the senses of the receiver.
     *
     * @return the iterator over the senses of the receiver
     */
    Iterator<ISense> getSenses();

    /**
     * Returns unmodifiable list of senses of the receiver.
     *
     * @return unmodifiable list of senses of the receiver
     */
    List<ISense> getSenseList();

    /**
     * Creates a sense and adds it as last sense.
     *
     * @param pos pos
     * @param id id
     * @return a newly created sense
     */
    ISense createSense(char pos, long id);

    /**
     * Adds a sense to the given node as the last sense.
     *
     * @param sense sense to add
     */
    void addSense(ISense sense);

    /**
     * Adds sense to the receiver at index.
     *
     * @param index index where the sense will be added
     * @param sense sense to add
     */
    void addSense(int index, ISense sense);

    /**
     * Removes the sense at index from the receiver.
     *
     * @param index index of a sense to remove
     */
    void removeSense(int index);

    /**
     * Removes sense from the receiver.
     *
     * @param sense sense to remove
     */
    void removeSense(ISense sense);
}