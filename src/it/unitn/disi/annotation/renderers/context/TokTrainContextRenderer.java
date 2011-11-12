package it.unitn.disi.annotation.renderers.context;

import it.unitn.disi.annotation.data.INLPContext;
import it.unitn.disi.annotation.data.INLPNode;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.smatch.renderers.context.BaseFileContextRenderer;
import it.unitn.disi.smatch.renderers.context.ContextRendererException;
import opennlp.tools.tokenize.TokenSample;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Renders context for OpenNLP tokenizer trainer.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TokTrainContextRenderer extends BaseFileContextRenderer<INLPContext> implements INLPContextRenderer {

    private static final Logger log = Logger.getLogger(TokTrainContextRenderer.class);

    public static final String TRAIN_FILES = "Train files (*.train)";
    private long renderedCount;

    @Override
    protected void process(INLPContext context, BufferedWriter out) throws IOException, ContextRendererException {
        renderedCount = 0;
        INLPNode curNode = context.getRoot();
        processNode(curNode, out);
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Rendered labels: " + renderedCount);
        }
    }

    private void processNode(INLPNode curNode, BufferedWriter out) throws IOException {
        String toWrite = getTrainSample(curNode);
        if (null != toWrite) {
            out.write(toWrite);
            out.write("\n");
            renderedCount++;
        }
        Iterator<INLPNode> i = curNode.getChildren();
        while (i.hasNext()) {
            processNode(i.next(), out);
        }
    }

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

    public String getDescription() {
        return TRAIN_FILES;
    }
}