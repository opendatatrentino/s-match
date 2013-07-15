package it.unitn.disi.annotation.renderers.context;

import it.unitn.disi.annotation.data.INLPNode;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;

/**
 * Renders annotated context in <a href="http://ilk.uvt.nl/conll/#dataformat">CONLL-X</a> format.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class CONLLXContextRenderer extends AbstractTextContextRenderer {

    public static final String TRAIN_FILES = "CONLL files (*.conll)";

    protected String getTrainSample(INLPNode curNode) {
        ILabel label = curNode.getNodeData().getLabel();
        if (null != label) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < label.getTokens().size(); i++) {
                IToken token = label.getTokens().get(i);
                result.append(Integer.toString(i + 1)).append("\t")//1 ID	 Token counter, starting at 1 for each new sentence.
                        .append(token.getText()).append("\t");//2	 FORM	 Word form or punctuation symbol.

                //3	 LEMMA	 Lemma or stem (depending on particular data set) of word form, or an underscore if not available.
                if (null != token.getLemma() && !token.getLemma().isEmpty()) {
                    result.append(token.getLemma()).append("\t");
                } else {
                    result.append("_\t");
                }

                //4	CPOSTAG	 Coarse-grained part-of-speech tag, where tagset depends on the language.
                result.append("_\t");

                //5	 POSTAG	 Fine-grained part-of-speech tag, where the tagset depends on the language, or identical to the coarse-grained part-of-speech tag if not available.
                if (null != token.getPOSTag() && !token.getPOSTag().isEmpty()) {
                    result.append(token.getPOSTag()).append("\t");
                } else {
                    result.append("_\t");
                }

                //6-10
                result.append("_\t");//6
                result.append("_\t");//7
                result.append("_\t");//8
                result.append("_\t");//9
                result.append("_\t");//10
                result.append("\n");
            }
            return result.toString();
        }
        return null;
    }

    public String getDescription() {
        return TRAIN_FILES;
    }
}