package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Node matcher for StageTreeMatcher for minimal links matching.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class OptimizedStageNodeMatcher extends BaseNodeMatcher implements INodeMatcher {

    /**
     * Checks source node and target node are disjoint or not for optimizing tree matcher.
     *
     * @param cLabMatrix interface of relational matrix with concept of labels
     * @param sourceNode interface of source node
     * @param targetNode interface of target node
     * @return true if the nodes are in disjoint relation
     * @throws NodeMatcherException NodeMatcherException
     */
    public boolean nodeDisjoint(IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) throws NodeMatcherException {
        boolean result = false;
        String sourceCNodeFormula = sourceNode.getNodeData().getCNodeFormula();
        String targetCNodeFormula = targetNode.getNodeData().getCNodeFormula();
        String sourceCLabFormula = sourceNode.getNodeData().getcLabFormula();
        String targetCLabFormula = targetNode.getNodeData().getcLabFormula();

        if (null != sourceCNodeFormula && null != targetCNodeFormula && !"".equals(sourceCNodeFormula) && !"".equals(targetCNodeFormula) &&
                null != sourceCLabFormula && null != targetCLabFormula && !"".equals(sourceCLabFormula) && !"".equals(targetCLabFormula)
                ) {
            //contains ACoLs ids as keys and numbers of variables in DIMACS format as values
            HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber = new HashMap<IAtomicConceptOfLabel, Integer>();
            Object[] obj = mkAxioms(hashConceptNumber, cLabMatrix, sourceNode, targetNode);
            String axioms = (String) obj[0];
            int num_of_axiom_clauses = (Integer) obj[1];

            //convert contexts into ArrayLists
            ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, sourceNode);
            ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, targetNode);
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

    // TODO Needs comments

    public boolean nodeSubsumedBy(IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) throws NodeMatcherException {
        boolean result = false;
        String sourceCNodeFormula = sourceNode.getNodeData().getCNodeFormula();
        String targetCNodeFormula = targetNode.getNodeData().getCNodeFormula();
        String sourceCLabFormula = sourceNode.getNodeData().getcLabFormula();
        String targetCLabFormula = targetNode.getNodeData().getcLabFormula();

        if (null != sourceCNodeFormula && null != targetCNodeFormula && !"".equals(sourceCNodeFormula) && !"".equals(targetCNodeFormula) &&
                null != sourceCLabFormula && null != targetCLabFormula && !"".equals(sourceCLabFormula) && !"".equals(targetCLabFormula)
                ) {
            if (sourceNode.getNodeData().getSource()) {
                HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber = new HashMap<IAtomicConceptOfLabel, Integer>();
                Object[] obj = mkAxioms(hashConceptNumber, cLabMatrix, sourceNode, targetNode);
                String axioms = (String) obj[0];
                int num_of_axiom_clauses = (Integer) obj[1];

                //convert contexts into ArrayLists
                ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, sourceNode);
                ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, targetNode);
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
                Object[] obj = mkAxioms(hashConceptNumber, cLabMatrix, targetNode, sourceNode);
                String axioms = (String) obj[0];
                int num_of_axiom_clauses = (Integer) obj[1];

                //convert contexts into ArrayLists
                ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, targetNode);
                ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, sourceNode);
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
    public char nodeMatch(IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) throws NodeMatcherException {
        return IMappingElement.IDK;  
    }
}