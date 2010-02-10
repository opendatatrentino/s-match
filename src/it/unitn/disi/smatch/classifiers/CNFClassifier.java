package it.unitn.disi.smatch.classifiers;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.INodeData;
import orbital.logic.imp.Formula;
import orbital.moon.logic.ClassicalLogic;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Converts cLabFormula into CNF before use.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CNFClassifier implements IClassifier {

    private static final Logger log = Logger.getLogger(CNFClassifier.class);

    public IContext buildCNodeFormulas(IContext context) {
        ArrayList<INode> queue = new ArrayList<INode>();
        queue.add(context.getRoot());

        while (!queue.isEmpty()) {
            INode currentNode = queue.remove(0);
            if (null == currentNode) {
//                pathToRoot.remove(pathToRoot.size() - 1);
//                pathToRootPhrases.remove(pathToRootPhrases.size() - 1);
            } else {
//                INLPhrase currentPhrase = processNode(currentNode, pathToRoot, pathToRootPhrases);
//                processedCount++;
                buildCNode(currentNode);

                Vector<INode> children = currentNode.getChildren();
                if (0 < children.size()) {
                    queue.add(0, null);
//                    pathToRoot.add(currentNode);
//                    pathToRootPhrases.add(currentPhrase);
                }
                for (int i = children.size() - 1; i >= 0; i--) {
                    // go DFS = depth-first search
                    queue.add(0, children.get(i));
                }
            }
        }

        return context;
    }

    /**
     * constructs cNode for the concept
     *
     * @param in node to process
     */
    public void buildCNode(INode in) {
        StringBuffer path = new StringBuffer();
        INode cpt = in;
        INodeData nd = cpt.getNodeData();
        String formula = toCNF(in, nd.getcLabFormula());
        formula = formula.trim();
        if (formula != null && !formula.equals("") && !formula.equals(" ")) {
            if (formula.contains(" ")) {
                formula = "(" + formula + ")";
            }
            path.append(formula);
        }
        if (!cpt.isRoot()) {
            formula = cpt.getParent().getNodeData().getCNodeFormula();
            formula = formula.trim();
            if (formula != null && !formula.equals("") && !formula.equals(" ")) {
                //formula = "(" + formula + ")";
                if (2 < path.length())
                    path.append(" & ").append(formula);
                else
                    path.append(formula);
            }
        }

        nd.setcNodeFormula(path.toString());
    }

    public String toCNF(INode in, String formula) {
        String result = formula;
        if ((formula.contains("&") && formula.contains("|")) || formula.contains("~")) {
            String tmpFormula = formula;
            tmpFormula = tmpFormula.trim();
            try {
                ClassicalLogic cl = new ClassicalLogic();
                if (!tmpFormula.equals("")) {
                    tmpFormula = tmpFormula.replace('.', 'P');
                    Formula f = (Formula) (cl.createExpression(tmpFormula));
                    Formula cnf = ClassicalLogic.Utilities.conjunctiveForm(f);
                    tmpFormula = cnf.toString();
                    result = tmpFormula.replace('P', '.');
                } else {
                    result = tmpFormula;
                }
            } catch (Exception e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Logic parse exception for: " + formula + " at node: " + in.getNodeName());
                    log.error("Logic parse exception: " + e.getMessage(), e);
                }
                //pe.printStackTrace();
            }
        } else {
            result = formula;
        }

        return result;
    }
}
