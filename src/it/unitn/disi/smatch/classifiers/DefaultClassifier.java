package it.unitn.disi.smatch.classifiers;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.INodeData;

import java.util.Vector;

/**
 * Constructs concept@node formulas.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DefaultClassifier implements IClassifier {

    public void buildCNodeFormulas(IContext context) {
        Vector<INode> allNodes = context.getAllNodes();
        //for all nodes in the context
        for (int i = 0; i < allNodes.size(); i++) {
            INode concept = allNodes.elementAt(i);
            // Creates and interface for node.
            INodeData nd = concept.getNodeData();
            //build cNode
            String cNode = buildCNode(concept);
            // Sets concept at node formula.
            nd.setcNodeFormula(cNode);
        }
    }

    /**
     * Constructs concept@node for a node.
     *
     * @param in node to process
     * @return concept@node formula
     */
    public String buildCNode(INode in) {
        StringBuffer path = new StringBuffer();
        INode cpt = in;
        INodeData nd = cpt.getNodeData();
        String formula = nd.getcLabFormula();
        formula = formula.trim();
        if (formula != null && !formula.equals("") && !formula.equals(" ")) {
            formula = "(" + formula + ")";
            path.append(formula);
        }
        while (!cpt.isRoot()) {
            cpt = cpt.getParent();
            nd = cpt.getNodeData();
            formula = nd.getcLabFormula();
            formula = formula.trim();
            if (formula != null && !formula.equals("") && !formula.equals(" ")) {
                formula = "(" + formula + ")";
                if (path != null && path.length() > 2)
                    path.append(" & ").append(formula);
                else
                    path.append(formula);
            }
        }
        return path.toString();
    }

}
