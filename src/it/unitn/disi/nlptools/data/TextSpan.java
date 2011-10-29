package it.unitn.disi.nlptools.data;

/**
 * A span of text.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class TextSpan {

    protected String text;

    protected TextSpan() {
    }

    protected TextSpan(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
