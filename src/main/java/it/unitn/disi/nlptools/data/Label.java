package it.unitn.disi.nlptools.data;

import java.util.Collections;
import java.util.List;

/**
 * Default implementation of short label.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Label extends TextSpan implements ILabel {

    protected List<ILabel> context = Collections.emptyList();
    protected List<IToken> tokens = Collections.emptyList();
    protected List<IMultiWord> multiWords = Collections.emptyList();
    protected String formula;

    public Label() {
        super();
    }

    public Label(String text) {
        super(text);
    }

    public List<ILabel> getContext() {
        return context;
    }

    public void setContext(List<ILabel> context) {
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

    @Override
    public String toString() {
        return text;
    }
}