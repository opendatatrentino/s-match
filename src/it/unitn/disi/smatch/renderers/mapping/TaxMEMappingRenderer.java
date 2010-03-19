package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.Mapping;
import it.unitn.disi.smatch.data.mappings.MappingElement;

import java.util.Vector;

/**
 * Writes a mapping in a TaxME format.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TaxMEMappingRenderer implements IMappingRenderer {

    public IMapping render(Vector args) {
        String fileName = (String) args.get(0);
        IMatchMatrix CnodMatrix = (IMatchMatrix) args.get(1);
        IMatchMatrix ClabMatrix = (IMatchMatrix) args.get(2);
        IContext sourceContext = (IContext) args.get(3);
        IContext targetContext = (IContext) args.get(4);

        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();

        IMapping result = fromMatrixToMapping(CnodMatrix, sourceNodes, targetNodes);
        result.toFile(fileName);
//        System.out.println(result.getSize() + " mappings found");
        return result;
    }

    /**
     * Converts the matrix into mapping with path of the nodes.
     *
     * @param matrix the relational matrix
     * @param sourceNodes interface of source node
     * @param targetNodes interface of target node
     * @return
     */
    public IMapping fromMatrixToMapping(IMatchMatrix matrix, Vector<INode> sourceNodes, Vector<INode> targetNodes) {
        IMapping result = new Mapping();
        int x = sourceNodes.size();
        int y = targetNodes.size();
        Vector<String> sourcePaths = new Vector<String>();
        Vector<String> targetPaths = new Vector<String>();
        for (int i = 0; i < x; i++) {
            sourcePaths.add(sourceNodes.get(i).getNodeData().getPathToRootString().replace(" ", "_"));
        }
        for (int j = 0; j < y; j++) {
            targetPaths.add(targetNodes.get(j).getNodeData().getPathToRootString().replace(" ", "_"));
        }
        for (int i = 0; i < x; i++) {
            String source = sourcePaths.get(i);
//            int source_depth = sourceNodes.get(i).getNodeData().getDepth();
            for (int j = 0; j < y; j++) {
                if ((MatchManager.SYNOMYM == matrix.getElement(i, j)) ||
                        (MatchManager.LESS_GENERAL_THAN == matrix.getElement(i, j)) ||
                        MatchManager.MORE_GENERAL_THAN == matrix.getElement(i, j)) {
                    String target = targetPaths.get(j);
//                    int target_depth = targetNodes.get(j).getNodeData().getDepth();
//                    if ((source_depth == 4) && (target_depth == 4)) {
                    MappingElement me = new MappingElement(source, target, matrix.getElement(i, j));
//                        if (!result.contains(me))
                    result.add(me);
//                    }
                }
            }
        }
        return result;
    }

}
