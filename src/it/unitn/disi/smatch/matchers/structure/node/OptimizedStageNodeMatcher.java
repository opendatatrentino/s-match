package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Node matcher for StageTreeMatcher for minimal links matching.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class OptimizedStageNodeMatcher extends BaseNodeMatcher implements INodeMatcher {

    /**
     * Checks whether source node and target node are disjoint.
     *
     * @param acolMapping mapping between acols
     * @param sourceACoLs mapping acol id -> acol object
     * @param targetACoLs mapping acol id -> acol object
     * @param sourceNode  interface of source node
     * @param targetNode  interface of target node
     * @return true if the nodes are in disjoint relation
     * @throws NodeMatcherException NodeMatcherException
     */
    public boolean nodeDisjoint(IContextMapping<IAtomicConceptOfLabel> acolMapping,
                                Map<String, IAtomicConceptOfLabel> sourceACoLs, Map<String, IAtomicConceptOfLabel> targetACoLs,
                                INode sourceNode, INode targetNode) throws NodeMatcherException {
        boolean result = false;
        String sourceCNodeFormula = sourceNode.getNodeData().getcNodeFormula();
        String targetCNodeFormula = targetNode.getNodeData().getcNodeFormula();
        String sourceCLabFormula = sourceNode.getNodeData().getcLabFormula();
        String targetCLabFormula = targetNode.getNodeData().getcLabFormula();

        if (null != sourceCNodeFormula && null != targetCNodeFormula && !"".equals(sourceCNodeFormula) && !"".equals(targetCNodeFormula) &&
                null != sourceCLabFormula && null != targetCLabFormula && !"".equals(sourceCLabFormula) && !"".equals(targetCLabFormula)
                ) {
            //contains ACoLs ids as keys and numbers of variables in DIMACS format as values
            HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber = new HashMap<IAtomicConceptOfLabel, Integer>();
            Object[] obj = mkAxioms(hashConceptNumber, acolMapping, sourceNode, targetNode);
            String axioms = (String) obj[0];
            int num_of_axiom_clauses = (Integer) obj[1];

            //convert contexts into ArrayLists
            ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, sourceACoLs, sourceNode);
            ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, targetACoLs, targetNode);
            //create contexts in DIMACS format
            String contextAInDIMACSFormat = DIMACSfromList(contextA);
            String contextBInDIMACSFormat = DIMACSfromList(contextB);

            //Disjointness test
            //get the sat problem in DIMACS format
            String satProblemInDIMACS = axioms + contextBInDIMACSFormat + contextAInDIMACSFormat;
            //get number of clauses for SAT problem
            int numberOfClauses = contextA.size() + contextB.size() + num_of_axiom_clauses;
            int numberOfVariables = hashConceptNumber.size();
            //add DIMACS header
            String DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;

            //do disjointness test
            result = isUnsatisfiable(DIMACSproblem);
        }
        return result;
    }

    /**
     * Checks whether the source node is subsumed by the target node.
     *
     * @param acolMapping mapping between acols
     * @param sourceACoLs mapping acol id -> acol object
     * @param targetACoLs mapping acol id -> acol object
     * @param sourceNode  interface of source node
     * @param targetNode  interface of target node
     * @return true if the nodes are in subsumption relation
     * @throws NodeMatcherException NodeMatcherException
     */
    public boolean nodeSubsumedBy(IContextMapping<IAtomicConceptOfLabel> acolMapping,
                                  Map<String, IAtomicConceptOfLabel> sourceACoLs, Map<String, IAtomicConceptOfLabel> targetACoLs,
                                  INode sourceNode, INode targetNode) throws NodeMatcherException {
        boolean result = false;
        String sourceCNodeFormula = sourceNode.getNodeData().getcNodeFormula();
        String targetCNodeFormula = targetNode.getNodeData().getcNodeFormula();
        String sourceCLabFormula = sourceNode.getNodeData().getcLabFormula();
        String targetCLabFormula = targetNode.getNodeData().getcLabFormula();

        if (null != sourceCNodeFormula && null != targetCNodeFormula && !"".equals(sourceCNodeFormula) && !"".equals(targetCNodeFormula) &&
                null != sourceCLabFormula && null != targetCLabFormula && !"".equals(sourceCLabFormula) && !"".equals(targetCLabFormula)
                ) {
            if (sourceNode.getNodeData().getSource()) {
                HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber = new HashMap<IAtomicConceptOfLabel, Integer>();
                Object[] obj = mkAxioms(hashConceptNumber, acolMapping, sourceNode, targetNode);
                String axioms = (String) obj[0];
                int num_of_axiom_clauses = (Integer) obj[1];

                //convert contexts into ArrayLists
                ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, sourceACoLs, sourceNode);
                ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, targetACoLs, targetNode);
                //create contexts in DIMACS format
                String contextAInDIMACSFormat = DIMACSfromList(contextA);
                //String contextBInDIMACSFormat = DIMACSfromList(contextB);

                //ArrayList with negated context
                ArrayList<ArrayList<String>> negatedContext = new ArrayList<ArrayList<String>>();
                //LG test
                //negate the context
                Integer numberOfVariables = negateFormulaInList(hashConceptNumber, contextB, negatedContext);
                //get the sat problem in DIMACS format
                String satProblemInDIMACS = axioms + contextAInDIMACSFormat + DIMACSfromList(negatedContext);
                //get number of clauses for SAT problem
                Integer numberOfClauses = num_of_axiom_clauses + contextA.size() + negatedContext.size();
                //add DIMACS header
                String DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;

                result = isUnsatisfiable(DIMACSproblem);
            } else {
                //swap source, target and relation
                HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber = new HashMap<IAtomicConceptOfLabel, Integer>();
                Object[] obj = mkAxioms(hashConceptNumber, acolMapping, targetNode, sourceNode);
                String axioms = (String) obj[0];
                int num_of_axiom_clauses = (Integer) obj[1];

                //convert contexts into ArrayLists
                ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, sourceACoLs, targetNode);
                ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, targetACoLs, sourceNode);
                //create contexts in DIMACS format
                //String contextAInDIMACSFormat = DIMACSfromList(contextA);
                String contextBInDIMACSFormat = DIMACSfromList(contextB);

                //ArrayList with negated context
                ArrayList<ArrayList<String>> negatedContext = new ArrayList<ArrayList<String>>();
                //MG test
                //negate the context
                Integer numberOfVariables = negateFormulaInList(hashConceptNumber, contextA, negatedContext);
                //get the sat problem in DIMACS format
                String satProblemInDIMACS = axioms + contextBInDIMACSFormat + DIMACSfromList(negatedContext);
                //get number of clauses for SAT problem
                Integer numberOfClauses = num_of_axiom_clauses + contextB.size() + negatedContext.size();
                //add DIMACS header
                String DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;

                result = isUnsatisfiable(DIMACSproblem);
            }
        }
        return result;
    }

    // stub to allow it to be created as node matcher.

    public char nodeMatch(IContextMapping<IAtomicConceptOfLabel> acolMapping,
                          Map<String, IAtomicConceptOfLabel> sourceACoLs, Map<String, IAtomicConceptOfLabel> targetACoLs,
                          INode sourceNode, INode targetNode) throws NodeMatcherException {
        return IMappingElement.IDK;
    }
}