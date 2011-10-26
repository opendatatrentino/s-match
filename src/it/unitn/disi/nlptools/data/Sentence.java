package it.unitn.disi.nlptools.data;

import java.util.Collections;
import java.util.List;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Sentence extends TextSpan implements ISentence {

    protected List<ISentence> context = Collections.<ISentence>emptyList();
    protected List<IToken> tokens = Collections.<IToken>emptyList();
    protected List<IMultiWord> multiWords = Collections.<IMultiWord>emptyList();
    protected String formula;

    public Sentence(String text) {
        super(text);
    }

    public List<ISentence> getContext() {
        return context;
    }

    public void setContext(List<ISentence> context) {
        this.context = context;
    }

    public List<IToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<IToken> tokens) {
        this.tokens = tokens;
    }

    public List<IMultiWord> getMultiWords() {
        return multiWords;
    }

    public void setMultiWords(List<IMultiWord> multiWords) {
        this.multiWords = multiWords;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}