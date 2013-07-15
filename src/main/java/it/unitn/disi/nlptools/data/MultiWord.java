package it.unitn.disi.nlptools.data;

import java.util.Collections;
import java.util.List;

/**
 * Container for multiwords.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MultiWord extends Token implements IMultiWord {

    private List<Integer> tokenIndexes = Collections.emptyList();
    private List<IToken> tokens = Collections.emptyList();

    public MultiWord(String text) {
        super(text);
    }

    public List<Integer> getTokenIndexes() {
        return tokenIndexes;
    }

    public void setTokenIndexes(List<Integer> tokenIndexes) {
        this.tokenIndexes = tokenIndexes;
    }

    public List<IToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<IToken> tokens) {
        this.tokens = tokens;
    }
}
