package it.unitn.disi.annotation.pipelines;

import it.unitn.disi.annotation.data.INLPNode;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.ILabelPipeline;
import it.unitn.disi.nlptools.INLPTools;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.Label;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IBaseNode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Applies pipeline to all nodes. Warning: it creates a label with shared context, which changes after the label is
 * processed.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PipelineContextPipelineComponent extends BaseContextPipelineComponent<INLPNode> {

    private static final Logger log = Logger.getLogger(PipelineContextPipelineComponent.class);

    private static final String NLPTOOLS_KEY = "nlp";

    private INLPTools nlpTools;
    private ILabelPipeline pipeline;

    private long counter;
    private long total;
    private long reportInt;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(NLPTOOLS_KEY)) {
                nlpTools = (INLPTools) configureComponent(nlpTools, oldProperties, newProperties, "NLPTools", NLPTOOLS_KEY, INLPTools.class);
                pipeline = nlpTools.getPipeline();
            } else {
                final String errMessage = "Cannot find configuration key " + NLPTOOLS_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }

    public void process(IBaseContext<INLPNode> instance) throws PipelineComponentException {
        //go DFS, processing node-by-node, keeping path-to-root as context

        counter = 0;
        if (null != instance.getRoot()) {
            total = instance.getRoot().getDescendantCount() + 1;
        } else {
            total = 0;
        }
        reportInt = (total / 20) + 1;//i.e. report every 5%

        ArrayList<INLPNode> queue = new ArrayList<INLPNode>();
        ArrayList<IBaseNode> pathToRoot = new ArrayList<IBaseNode>();
        ArrayList<ILabel> pathToRootPhrases = new ArrayList<ILabel>();
        queue.add(instance.getRoot());

        while (!queue.isEmpty()) {
            INLPNode currentNode = queue.remove(0);
            if (null == currentNode) {
                pathToRoot.remove(pathToRoot.size() - 1);
                pathToRootPhrases.remove(pathToRootPhrases.size() - 1);
            } else {
                ILabel currentPhrase;
                currentPhrase = processNode(currentNode, pathToRootPhrases);

                List<INLPNode> children = currentNode.getChildrenList();
                if (0 < children.size()) {
                    queue.add(0, null);
                    pathToRoot.add(currentNode);
                    pathToRootPhrases.add(currentPhrase);
                }
                for (int i = children.size() - 1; i >= 0; i--) {
                    queue.add(0, children.get(i));
                }
            }
        }
    }

    protected ILabel processNode(INLPNode currentNode, ArrayList<ILabel> pathToRootPhrases) {
        ILabel label = new Label(currentNode.getNodeData().getName());
        label.setContext(pathToRootPhrases);
        try {
            pipeline.process(label);
        } catch (PipelineComponentException e) {
            log.error(e.getMessage(), e);
        }
        currentNode.getNodeData().setLabel(label);
        reportProgress();
        return label;
    }

    protected void reportProgress() {
        counter++;
        if ((SMatchConstants.LARGE_TREE < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
            log.info(100 * counter / total + "%");
        }
    }
}