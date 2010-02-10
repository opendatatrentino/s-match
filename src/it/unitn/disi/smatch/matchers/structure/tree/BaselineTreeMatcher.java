package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.*;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Nickh
 * Date: 03.03.2005
 * Time: 15:30:58
 * To change this template use File | Settings | File Templates.
 */
public class BaselineTreeMatcher extends DefaultTreeMatcher implements ITreeMatcher {

    public IMatchMatrix treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) {
//        IMapping result = new Mapping();
        Vector sourceLemmas = null;
        Vector targetLemmas = null;

        //get all ACoLs in contexts
        Vector<IAtomicConceptOfLabel> sourceACoLs = sourceContext.getMatchingContext().getAllContextACoLs();
        Vector<IAtomicConceptOfLabel> targetACoLs = targetContext.getMatchingContext().getAllContextACoLs();

        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();

        System.out.println("Source context size: " + sourceNodes.size());
        System.out.println("Target context size: " + targetNodes.size());
        //initialize CnodMatrix
        IMatchMatrix CnodMatrix = MatrixFactory.getInstance(sourceNodes.size(), targetNodes.size());

        //  semantic relation for particular node matching task
        char relation;

        //  For every concept in source context
        for (int i = 0; i < sourceNodes.size(); i++) {
            INode sourceNode = sourceNodes.get(i);
            sourceLemmas = getLemmasForReasoning(sourceNode);
            sourceLemmas = removeDublicates(sourceLemmas);

            for (int j = 0; j < targetNodes.size(); j++) {
                INode targetNode = targetNodes.get(j);
                targetLemmas = getLemmasForReasoning(targetNode);
                targetLemmas = removeDublicates(targetLemmas);

                boolean isMG = sourceLemmas.containsAll(targetLemmas);
                boolean isLG = targetLemmas.containsAll(sourceLemmas);
                relation = getRelationString(isMG, isLG, false);
                CnodMatrix.setElement(i, j, relation);
                //fill mapping with relations
//                if ((relation!=it.unitn.disi.smatch.MatchManager.IDK_RELATION) && (relation!=it.unitn.disi.smatch.MatchManager.OPPOSITE_MEANING)) {
//                    String source = sourceNode.getPathToRootString();
//                    String target = targetNode.getPathToRootString();
//                    MappingElement me = new MappingElement(source, target, relation);
//                    if (!result.contains(me))
//                        result.add(me);
//                }
            }
        }

        //Print cLabMatrix and cNodeMatrix to file
//        printMatricesToFile(it.unitn.disi.smatch.MatchManager.outputFile, ClabMatrix, CnodMatrix,
//                sourceACoLs, targetACoLs, sourceNodes, targetNodes);

//        result.toFile("C:\\res123.txt");
        return CnodMatrix;
    }

    protected Vector removeDublicates(Vector tmp) {
        Vector res = new Vector();
        for (int i = 0; i < tmp.size(); i++) {
            String s = (String) tmp.get(i);
            for (StringTokenizer stringTokenizer = new StringTokenizer(s); stringTokenizer.hasMoreTokens();) {
                String s1 = stringTokenizer.nextToken();
                if (!res.contains(s))
                    res.add(s);
            }
        }
        return res;
    }

    protected char getRelationString(boolean isContains, boolean isContained, boolean isOpposite) {
        //return the tests results
        if (isContains && isContained) {
            //The concepts are equivalent
            return MatchManager.SYNOMYM;
        }
        if (isContained) {
            //The source concept is LG the target concept
            return MatchManager.LESS_GENERAL_THAN;
        }
        if (isContains) {
            //The target concept is LG the source concept
            return MatchManager.MORE_GENERAL_THAN;
        }
        if (isOpposite == true) {
            //The concepts have opposite menaning
            return MatchManager.OPPOSITE_MEANING;
        }
        return MatchManager.IDK_RELATION;
    }

    public Vector<String> getLemmasForReasoning(INode node) {
        Vector<String> result = new Vector<String>();
        result = getLemmasVector(node, result);
        return result;
    }

    private Vector<String> getLemmasVector(INode cpt, Vector<String> partialResult) {
        if (cpt.isRoot() == false)
            getLemmasVector(cpt.getParent(), partialResult);
        Vector<IAtomicConceptOfLabel> table = cpt.getNodeData().getACoLs();
        for (int i = 0; i < table.size(); i++) {
            IAtomicConceptOfLabel s = (table.get(i));
            String name = s.getLemma();
            partialResult.add(name);
        }
        return partialResult;
    }


}
