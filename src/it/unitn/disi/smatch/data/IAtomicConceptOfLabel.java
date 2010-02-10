package it.unitn.disi.smatch.data;

import java.util.Vector;

/**
 * An interface for atomic concept of label implementations.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IAtomicConceptOfLabel {

    //Set id in node
    void setIdToken(int idToken);

    //Set logical id
    void setTokenUID(String tokenUID);

    //Set token
    void setToken(String token);

    //Set lemma
    void setLemma(String lemma);

    //Set part of speech
    void setPos(String pos);

    //Add senses set to the lemma
    void addSenses(Vector<String> senseList);

    //Get token
    String getToken();

    //Get position in the sentence
    int getIdToken();

    //Get logical representation
    String getTokenUID();

    String getLemma();

    String getPos();

    ISensesSet getSenses();

    //index for cLabMatrix
    int getIndex();

    void setIndex(int index);

    String toString();

    boolean equals(Object o);

    int hashCode();
}
