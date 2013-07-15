package it.unitn.disi.nlptools.data;

import it.unitn.disi.smatch.data.ling.ISense;

import java.util.List;

/**
 * Interface to text tokens.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IToken extends ITextSpan {

    String getLemma();

    void setLemma(String lemma);

    String getPOSTag();

    void setPOSTag(String posTag);

    List<ISense> getSenses();

    void setSenses(List<ISense> senses);
}