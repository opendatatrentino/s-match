package it.unitn.disi.smatch.preprocessors;

import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.INLPPipeline;
import it.unitn.disi.nlptools.INLPTools;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.data.Label;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Performs linguistic preprocessing using NLPTools, on errors falls back to heuristic-based one.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NLPToolsContextPreprocessor extends Configurable implements IContextPreprocessor {

    private static final Logger log = Logger.getLogger(NLPToolsContextPreprocessor.class);

    private static final String NLPTOOLS_KEY = "nlp";

    private INLPTools nlpTools;
    private INLPPipeline pipeline;

    private final static String DCP_KEY = "dcp";
    private DefaultContextPreprocessor dcp;

    // flag to output the label being translated in logs
    private final static String DEBUG_LABELS_KEY = "debugLabels";
    private boolean debugLabels = false;

    private int fallbackCount;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(DEBUG_LABELS_KEY)) {
                debugLabels = Boolean.parseBoolean(newProperties.getProperty(DEBUG_LABELS_KEY));
            }

            if (newProperties.containsKey(NLPTOOLS_KEY)) {
                nlpTools = (INLPTools) configureComponent(nlpTools, oldProperties, newProperties, "NLPTools", NLPTOOLS_KEY, INLPTools.class);
                pipeline = nlpTools.getPipeline();
            } else {
                final String errMessage = "Cannot find configuration key " + NLPTOOLS_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            if (newProperties.containsKey(DCP_KEY)) {
                dcp = (DefaultContextPreprocessor) configureComponent(dcp, oldProperties, newProperties, "DefaultContextPreprocessor", DCP_KEY, DefaultContextPreprocessor.class);
            } else {
                final String errMessage = "Cannot find configuration key " + DCP_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }

    public void preprocess(IContext context) throws ContextPreprocessorException {
        //go DFS, processing label-by-label, keeping path-to-root as context
        //process each text getting the formula

        int processedCount = 0;
        fallbackCount = 0;

        ArrayList<INode> queue = new ArrayList<INode>();
        ArrayList<INode> pathToRoot = new ArrayList<INode>();
        ArrayList<ILabel> pathToRootPhrases = new ArrayList<ILabel>();
        queue.add(context.getRoot());

        while (!queue.isEmpty()) {
            INode currentNode = queue.remove(0);
            if (null == currentNode) {
                pathToRoot.remove(pathToRoot.size() - 1);
                pathToRootPhrases.remove(pathToRootPhrases.size() - 1);
            } else {
                ILabel currentPhrase;
                currentPhrase = processNode(currentNode, pathToRootPhrases);
                processedCount++;

                List<INode> children = currentNode.getChildrenList();
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

        log.info("Processed nodes: " + processedCount + ", fallbacks: " + fallbackCount);
    }

    /**
     * Converts current node label into a formula using path to root as a context
     *
     *
     * @param currentNode       a node to process
     * @param pathToRootPhrases phrases in the path to root
     * @return phrase instance for a current node label
     * @throws ContextPreprocessorException ContextPreprocessorException
     */
    private ILabel processNode(INode currentNode, ArrayList<ILabel> pathToRootPhrases) throws ContextPreprocessorException {
        if (debugLabels) {
            log.debug("preprocessing node: " + currentNode.getNodeData().getId() + ", label: " + currentNode.getNodeData().getName());
        }

        // reset old preprocessing
        currentNode.getNodeData().setcLabFormula("");
        currentNode.getNodeData().setcNodeFormula("");
        while (0 < currentNode.getNodeData().getACoLCount()) {
            currentNode.getNodeData().removeACoL(0);
        }

        String label = currentNode.getNodeData().getName();
        ILabel result = new Label(label);
        result.setContext(pathToRootPhrases);
        try {
            pipeline.process(result);

            //should contain only token indexes. including not recognized, but except closed class tokens.
            //something like
            // 1 & 2
            // 1 & (3 | 4)
            String formula = result.getFormula();

            //create acols. one acol for each concept (meaningful) token
            //non-concept tokens should not make it up to a formula.
            String[] tokenIndexes = formula.split("[ ()&|~]");
            Set<String> indexes = new HashSet<String>(Arrays.asList(tokenIndexes));
            List<IToken> tokens = result.getTokens();
            for (int i = 0; i < tokens.size(); i++) {
                IToken token = tokens.get(i);
                String tokenIdx = Integer.toString(i);
                if (indexes.contains(tokenIdx)) {
                    IAtomicConceptOfLabel acol = currentNode.getNodeData().createACoL();
                    acol.setId(i);
                    acol.setToken(token.getText());
                    acol.setLemma(token.getLemma());
                    for (ISense sense : token.getSenses()) {
                        acol.addSense(sense);
                    }
                    currentNode.getNodeData().addACoL(acol);
                }
            }

            //prepend all token references with node id
            formula = formula.replaceAll("(\\d+)", currentNode.getNodeData().getId() + ".$1");
            formula = formula.trim();
            //set it to the node
            currentNode.getNodeData().setcLabFormula(formula);
        } catch (PipelineComponentException e) {
            if (log.isEnabledFor(Level.WARN)) {
                log.warn("Falling back to heuristic parser for label (" + result.getText() + "): " + e.getMessage(), e);
                fallbackCount++;
                dcp.processNode(currentNode);
            }
        }
        return result;
    }
}