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

    //position in concept
    private int idToken;
    //logical representation
    private String tokenUID = "";
    //token
    private String token;
    //lemma
    private String lemma;

    //Part of speech
    private String pos = "";

    ISensesSet wSenses = new SensesSet();

    //index for cLabMatrix
    private int index;

    public AtomicConceptOfLabel() {
    }

    static public IAtomicConceptOfLabel getInstance() {
        return new AtomicConceptOfLabel();
    }

    static public IAtomicConceptOfLabel getInstance(int idToken, String token, String lemma, String pos) {
        return new AtomicConceptOfLabel(idToken, token, lemma, pos);
    }

    /**
     * Constructor class which sets the id, position of token, name of token and lemma.
     *
     * @param idToken Id of token
     * @param token   token name
     * @param lemma   lemma name
     * @param pos     position of token
     */
    public AtomicConceptOfLabel(int idToken, String token, String lemma, String pos) {
        this.idToken = idToken;
        this.token = token;
        this.lemma = lemma;
        this.pos = pos;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    //Set id in node

    public void setIdToken(int idToken) {
        this.idToken = idToken;
    }

    //Set logical id

    public void setTokenUID(String tokenUID) {
        this.tokenUID = tokenUID;
    }

    //Set token

    public void setToken(String token) {
        this.token = token;
    }

    //Set lemma

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    //Set part of speech

    public void setPos(String pos) {
        this.pos = pos;
    }

    //Add senses to the lemma

    public void addSenses(List<String> senseList) {
        wSenses.addNewSenses(senseList);
    }

    //Get token

    public String getToken() {
        return token;
    }

    //Get position in the sentence

    public int getIdToken() {
        return idToken;
    }

    //Get logical representation

    public String getTokenUID() {
        return tokenUID;
    }

    //Get lemma

    public String getLemma() {
        return lemma;
    }

    public String getPos() {
        return pos;
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
