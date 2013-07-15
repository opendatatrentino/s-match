package it.unitn.disi.annotation.renderers.context;

import it.unitn.disi.annotation.data.INLPNode;
import it.unitn.disi.nlptools.data.IToken;

/**
 * Renders context for OpenNLP POS tag trainer.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class POSTrainContextRenderer extends AbstractTextContextRenderer {

    @Override
    protected String getTrainSample(INLPNode curNode) {
        if (null != curNode.getNodeData().getLabel()) {
            StringBuilder result = new StringBuilder();
            for (IToken token : curNode.getNodeData().getLabel().getTokens()) {
                if (null != token.getPOSTag()) {
                    result.append(token.getText().replace(' ', '_')).append("_").append(token.getPOSTag()).append(" ");
                }
            }
            return result.substring(0, result.length() - 1);
        }
        return null;
    }
}