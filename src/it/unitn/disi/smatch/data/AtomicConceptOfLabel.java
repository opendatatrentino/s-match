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

    private int id;
    private String token;
    private String lemma;

    ISensesSet wSenses = new SensesSet();

    public AtomicConceptOfLabel() {
    }

    /**
     * Constructor class which sets the id, token and lemma.
     *
     * @param id    id of token
     * @param token token
     * @param lemma lemma
     */
    public AtomicConceptOfLabel(int id, String token, String lemma) {
        this.id = id;
        this.token = token;
        this.lemma = lemma;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ISensesSet getSenses() {
        return wSenses;
    }

    public void addSenses(List<String> senseList) {
        wSenses.addNewSenses(senseList);
    }

    public String toString() {
        return token;
    }
}