package it.unitn.disi.nlptools.data;

import java.util.List;

/**
 * Interface for multiwords.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IMultiWord extends IToken {

    List<Integer> getTokenIndexes();

    void setTokenIndexes(List<Integer> tokenIndexes);

    List<IToken> getTokens();

    void setTokens(List<IToken> tokens);
}
