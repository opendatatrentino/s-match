package it.unitn.disi.annotation.renderers.context;

import it.unitn.disi.annotation.data.INLPNode;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import opennlp.tools.tokenize.TokenSample;

/**
 * Renders context for OpenNLP tokenizer trainer.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TokTrainContextRenderer extends AbstractTextContextRenderer {

    protected String getTrainSample(INLPNode curNode) {
        ILabel label = curNode.getNodeData().getLabel();
        if (null != label) {
            StringBuilder result = new StringBuilder();
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < label.getTokens().size(); i++) {
                IToken token = label.getTokens().get(i);
                if (null != token.getText()) {
                    result.append(token.getText());
                    if (i < label.getTokens().size() - 1) {//for every but the last token
                        text.append(token.getText());
                        String s = text.toString();
                        if (label.getText().startsWith(s + " ")) {
                            result.append(" ");
                            text.append(" ");
                        } else {
                            result.append(TokenSample.DEFAULT_SEPARATOR_CHARS);
                        }
                    }
                }
            }
            return result.toString();
        }
        return null;
    }
}