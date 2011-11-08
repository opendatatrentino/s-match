package it.unitn.disi.nlptools.data;

import it.unitn.disi.smatch.data.ling.ISense;

import java.util.Collections;
import java.util.List;

/**
 * Default token implementation.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Token extends TextSpan implements IToken {

    protected String lemma;
    protected String posTag;
    protected List<ISense> senses = Collections.emptyList();

    public Token() {
        super();
    }

    public Token(String text) {
        super(text);
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getPOSTag() {
        return posTag;
    }

    public void setPOSTag(String posTag) {
        this.posTag = posTag;
    }

    public List<ISense> getSenses() {
        return senses;
    }

    public void setSenses(List<ISense> senses) {
        this.senses = senses;
    }

    @Override
    public String toString() {
        return "Token{" +
                "text='" + text + '\'' +
                ", lemma='" + lemma + '\'' +
                ", posTag='" + posTag + '\'' +
                ", senses=" + senses +
                '}';
    }
}