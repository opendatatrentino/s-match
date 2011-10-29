package it.unitn.disi.nlptools.data;

import java.util.List;

/**
 * An interface for a short labels.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface ILabel extends ITextSpan {

    String getText();

    void setText(String text);

    List<ILabel> getContext();

    void setContext(List<ILabel> context);

    List<IToken> getTokens();

    void setTokens(List<IToken> tokens);

    List<IMultiWord> getMultiWords();

    void setMultiWords(List<IMultiWord> multiWords);

    String getFormula();

    void setFormula(String formula);
}