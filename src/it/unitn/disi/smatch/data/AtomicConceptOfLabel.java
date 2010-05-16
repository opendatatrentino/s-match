package it.unitn.disi.smatch.data;

import java.util.List;

/**
 * This class represents atomic concept of label (ACoL) as a
 * concept label and list of associated senses in WordNet.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class AtomicConceptOfLabel implements IAtomicConceptOfLabel {

    // position in concept
    private int idToken;
    // logical representation
    private String tokenUID = "";
    private String token;
    private String lemma;

    ISensesSet wSenses = new SensesSet();

    // index for cLabMatrix
    private int index;

    public AtomicConceptOfLabel() {
    }

    static public IAtomicConceptOfLabel getInstance() {
        return new AtomicConceptOfLabel();
    }

    static public IAtomicConceptOfLabel getInstance(int idToken, String token, String lemma) {
        return new AtomicConceptOfLabel(idToken, token, lemma);
    }

    /**
     * Constructor class which sets the id, position of token, name of token and lemma.
     *
     * @param idToken Id of token
     * @param token   token name
     * @param lemma   lemma name
     */
    public AtomicConceptOfLabel(int idToken, String token, String lemma) {
        this.idToken = idToken;
        this.token = token;
        this.lemma = lemma;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setIdToken(int idToken) {
        this.idToken = idToken;
    }

    public void setTokenUID(String tokenUID) {
        this.tokenUID = tokenUID;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public void addSenses(List<String> senseList) {
        wSenses.addNewSenses(senseList);
    }

    public String getToken() {
        return token;
    }

    public int getIdToken() {
        return idToken;
    }

    public String getTokenUID() {
        return tokenUID;
    }

    public String getLemma() {
        return lemma;
    }

    public ISensesSet getSenses() {
        return wSenses;
    }

    public String toString() {
        return token;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IAtomicConceptOfLabel)) {
            return false;
        }

        final AtomicConceptOfLabel atomicConceptOfLabel = (AtomicConceptOfLabel) o;

        if (!tokenUID.equals(atomicConceptOfLabel.tokenUID)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return tokenUID.hashCode();
    }
}
