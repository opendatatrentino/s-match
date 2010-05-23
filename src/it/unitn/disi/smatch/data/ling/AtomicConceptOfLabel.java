package it.unitn.disi.smatch.data.ling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

    private List<ISense> senses = new ArrayList<ISense>();
    private static final Iterator<ISense> EMPTY_SENSE_ITERATOR = Collections.<ISense>emptyList().iterator();

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

    public ISense getSenseAt(int index) {
        if (senses == null) {
            throw new ArrayIndexOutOfBoundsException("acol has no senses");
        }
        return senses.get(index);
    }

    public int getSenseCount() {
        if (senses == null) {
            return 0;
        } else {
            return senses.size();
        }
    }

    public int getSenseIndex(ISense sense) {
        if (null == sense) {
            throw new IllegalArgumentException("argument is null");
        }

        return senses.indexOf(sense);
    }

    public Iterator<ISense> getSenses() {
        if (null == senses) {
            return EMPTY_SENSE_ITERATOR;
        } else {
            return senses.iterator();
        }
    }

    public List<ISense> getSenseList() {
        return Collections.unmodifiableList(senses);
    }

    public ISense createSense(char pos, long id) {
        ISense sense = new Sense(pos, id);
        addSense(sense);
        return sense;
    }

    public void addSense(ISense sense) {
        addSense(getSenseCount(), sense);
    }

    public void addSense(int index, ISense sense) {
        if (null == sense) {
            throw new IllegalArgumentException("new sense is null");
        }

        if (null == senses) {
            senses = new ArrayList<ISense>();
        }

        if (-1 == senses.indexOf(sense)) {
            senses.add(index, sense);
        }
    }

    public void removeSense(int index) {
        senses.remove(index);
    }

    public void removeSense(ISense sense) {
        if (null == sense) {
            throw new IllegalArgumentException("argument is null");
        }

        removeSense(getSenseIndex(sense));
    }

    public String toString() {
        return token;
    }
}