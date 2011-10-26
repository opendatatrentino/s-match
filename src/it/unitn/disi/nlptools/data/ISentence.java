package it.unitn.disi.nlptools.data;

import java.util.List;

/**
 * An interface for a short sentence.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface ISentence extends ITextSpan {

    String getText();

    void setText(String text);

    List<ISentence> getContext();

    void setContext(List<ISentence> context);

    List<IToken> getTokens();

    void setTokens(List<IToken> tokens);

    List<IMultiWord> getMultiWords();

    void setMultiWords(List<IMultiWord> multiWords);

    String getFormula();

    void setFormula(String formula);
}