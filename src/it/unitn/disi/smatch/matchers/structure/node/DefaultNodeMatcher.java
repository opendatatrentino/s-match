package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Default node matcher.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DefaultNodeMatcher extends BaseNodeMatcher implements INodeMatcher {

    public char nodeMatch(IContextMapping<IAtomicConceptOfLabel> acolMapping,
                          Map<String, IAtomicConceptOfLabel> sourceACoLs, Map<String, IAtomicConceptOfLabel> targetACoLs,
                          INode sourceNode, INode targetNode) throws NodeMatcherException {
        char result = IMappingElement.IDK;
        String sourceCNodeFormula = sourceNode.getNodeData().getcNodeFormula();
        String targetCNodeFormula = targetNode.getNodeData().getcNodeFormula();
        String sourceCLabFormula = sourceNode.getNodeData().getcLabFormula();
        String targetCLabFormula = targetNode.getNodeData().getcLabFormula();

        if (null != sourceCNodeFormula && null != targetCNodeFormula && !"".equals(sourceCNodeFormula) && !"".equals(targetCNodeFormula) &&
                null != sourceCLabFormula && null != targetCLabFormula && !"".equals(sourceCLabFormula) && !"".equals(targetCLabFormula)
                ) {
            //whether particular relation holds
            boolean isContains;
            boolean isContained;
            boolean isOpposite;

            //contains ACoLs ids as keys and numbers of variables in DIMACS format as values
            HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber = new HashMap<IAtomicConceptOfLabel, Integer>();
            //Number of variables in SAT problem
            Integer numberOfVariables;
            //Number of clauses in SAT problem
            Integer numberOfClauses;

            Object[] obj = mkAxioms(hashConceptNumber, acolMapping, sourceNode, targetNode);
            String axioms = (String) obj[0];
            int num_of_axiom_clauses = (Integer) obj[1];
            //convert contexts into ArrayLists
            ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, sourceACoLs, sourceNode);
            ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, targetACoLs, targetNode);
            //create contexts in DIMACS format
            String contextAInDIMACSFormat = DIMACSfromList(contextA);
            String contextBInDIMACSFormat = DIMACSfromList(contextB);

            //ArrayList with negated context
            ArrayList<ArrayList<String>> negatedContext = new ArrayList<ArrayList<String>>();
            //sat problem in DIMACS format
            String satProblemInDIMACS;
            //sat problem with DIMACS header
            String DIMACSproblem;
            //whether contexts are conjunctive
            //if the contexts are not conjunctive
            //LG test
            //negate the context
            numberOfVariables = negateFormulaInList(hashConceptNumber, contextB, negatedContext);
            //get the sat problem in DIMACS format
            satProblemInDIMACS = axioms + contextAInDIMACSFormat + DIMACSfromList(negatedContext);
            //get number of clauses for SAT problem
            numberOfClauses = num_of_axiom_clauses + contextA.size() + negatedContext.size();
            //add DIMACS header
            DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;
            //do LG test
            isContained = isUnsatisfiable(DIMACSproblem);

            //MG test
            //negate the context
            numberOfVariables = negateFormulaInList(hashConceptNumber, contextA, negatedContext);
            //get the sat problem in DIMACS format
            satProblemInDIMACS = axioms + contextBInDIMACSFormat + DIMACSfromList(negatedContext);
            //get number of clauses for SAT problem
            numberOfClauses = num_of_axiom_clauses + contextB.size() + negatedContext.size();
            //add DIMACS header
            DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;
            //do MG test
            isContains = isUnsatisfiable(DIMACSproblem);

            //Disjointness test
            //get the sat problem in DIMACS format
            satProblemInDIMACS = axioms + contextBInDIMACSFormat + contextAInDIMACSFormat;
            //get number of clauses for SAT problem
            numberOfClauses = contextA.size() + contextB.size() + num_of_axiom_clauses;
            numberOfVariables = hashConceptNumber.size();
            //add DIMACS header
            DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;
            //do disjointness test
            isOpposite = isUnsatisfiable(DIMACSproblem);

            result = getRelationString(isContains, isContained, isOpposite);
        }
        return result;
    }
}
