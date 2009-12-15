package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * Renders a mapping into an HTML file. Suits well small matching tasks for debugging.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DefaultHTMLMappingRenderer implements IMappingRenderer {

    public IMapping render(Vector args) {
        String fileName = (String) args.get(0);
        IMatchMatrix CnodMatrix = (IMatchMatrix) args.get(1);
        IMatchMatrix ClabMatrix = (IMatchMatrix) args.get(2);
        IContext sourceContext = (IContext) args.get(3);
        IContext targetContext = (IContext) args.get(4);

        //get all ACoLs in contexts
        Vector<IAtomicConceptOfLabel> sourceACoLs = sourceContext.getMatchingContext().getAllContextACoLs();
        Vector<IAtomicConceptOfLabel> targetACoLs = targetContext.getMatchingContext().getAllContextACoLs();

        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();
        printMatricesToFile(fileName, ClabMatrix, CnodMatrix,
                sourceACoLs, targetACoLs, sourceNodes, targetNodes);
        return null;
    }

    /**
     * Prints cLab and cNode Matrices into file
     *
     * @param fileName   File name
     * @param ClabMatrix cLabMatrix
     * @param CnodMatrix cNodeMatrix
     */
    protected void printMatricesToFile(String fileName, IMatchMatrix ClabMatrix, IMatchMatrix CnodMatrix,
                                       Vector<IAtomicConceptOfLabel> sourceACoLs, Vector<IAtomicConceptOfLabel> targetACoLs,
                                       Vector<INode> sourceNodes, Vector<INode> targetNodes) {
        //Output file
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(fileName));
            out.write("<html><body>");
            if (null != ClabMatrix) {
                out.write("<h1>Matrix of semantic relations between atomic concepts of labels</h1>");
                out.write("<table border=1><tr><td></td>");
                for (int i = 0; i < targetACoLs.size(); i++) {
                    out
                            .write("<td DIR=rtl style='layout-flow:vertical-ideographic'>"
                                    + targetACoLs.get(i).getToken()
                                    + "</td>");
                }
                for (int i = 0; i < sourceACoLs.size(); i++) {
                    out.write("<tr align=center><td NOWRAP align=right>"
                            + sourceACoLs.get(i).getToken() + "</td>");
                    for (int j = 0; j < targetACoLs.size(); j++) {
                        if ((ClabMatrix.getElement(i, j) != MatchManager.IDK_RELATION))
                            out.write("<td STYLE='font-size:larger'>"
                                    + ClabMatrix.getElement(i, j) + "</td>");
                        else
                            out.write("<td STYLE='color:#c0c0c0'>"
                                    + ClabMatrix.getElement(i, j) + "</td>");
                    }
                    out.write("</tr>");
                }
                out.write("</table>");
            }
            String sourceConceptName;
            String targetConceptName;
            out.write("<h1>Matrix of semantic relations between concepts at nodes (matching result)</h1>");
            out.write("<table border=1><tr><td></td>");
            for (INode targetNode : targetNodes) {
                targetConceptName = targetNode.getNodeName();
                out.write("<td DIR=rtl style='layout-flow:vertical-ideographic'>" + targetConceptName + "</td>");
            }
            out.write("</tr>");

            //  For every concept in both context
            for (int i = 0; i < sourceNodes.size(); i++) {
                out.flush();
                sourceConceptName = (sourceNodes.get(i)).getNodeName();
                out.write("<tr align=center><td NOWRAP align=right>" + sourceConceptName + "</td>");
                for (int j = 0; j < targetNodes.size(); j++) {
                    char resultSat = CnodMatrix.getElement(i, j);
                    if (resultSat == ' ')
                        resultSat = 'n';
                    if (resultSat != MatchManager.IDK_RELATION)
                        out.write("<td STYLE='font-size:larger'>" + resultSat + "</td>");
                    else
                        out.write("<td STYLE='color:#c0c0c0'>" + resultSat + "</td>");
                }
                out.write("</tr>");
            }
            out.write("</table></body></html>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

}
