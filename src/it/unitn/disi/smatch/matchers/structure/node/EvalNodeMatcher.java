package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.classifiers.CNFContextClassifier;
import it.unitn.disi.smatch.classifiers.ContextClassifierException;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.matchers.element.EvalELMatcher;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Node matcher for evaluation purposes.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class EvalNodeMatcher extends BaseNodeMatcher implements INodeMatcher {

    private static final Logger log = Logger.getLogger(EvalNodeMatcher.class);

    public char nodeMatch(IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) throws NodeMatcherException {
        try {
            char result = IMappingElement.IDK;
            String sourceCLabFormula = sourceNode.getNodeData().getcLabFormula();
            String targetCLabFormula = targetNode.getNodeData().getcLabFormula();

            if (null != sourceCLabFormula && null != targetCLabFormula && !"".equals(sourceCLabFormula) && !"".equals(targetCLabFormula)
                    ) {
                sourceCLabFormula = CNFContextClassifier.toCNF(sourceNode, sourceCLabFormula);
                targetCLabFormula = CNFContextClassifier.toCNF(targetNode, targetCLabFormula);

                //whether particular relation holds
                boolean isContains;
                boolean isContained;

                //contains ACoLs ids as keys and numbers of variables in DIMACS format as values
                HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber = new HashMap<IAtomicConceptOfLabel, Integer>();
                //Number of variables in SAT problem
                Integer numberOfVariables;
                //Number of clauses in SAT problem
                Integer numberOfClauses;

                Object[] obj = mkAxioms(hashConceptNumber, cLabMatrix, sourceNode, targetNode);
                String axioms = (String) obj[0];
                int num_of_axiom_clauses = (Integer) obj[1];
                //convert contexts into ArrayLists
                ArrayList<ArrayList<String>> contextA = parseFormula(hashConceptNumber, sourceNode, sourceCLabFormula);
                ArrayList<ArrayList<String>> contextB = parseFormula(hashConceptNumber, targetNode, targetCLabFormula);
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

                result = getRelationString(isContains, isContained, false);
            } else {
                if (null == sourceCLabFormula && null == targetCLabFormula || "".equals(sourceCLabFormula) && "".equals(targetCLabFormula)) {
                    result = IMappingElement.EQUIVALENCE;
                }
            }
            return result;
        } catch (ContextClassifierException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new NodeMatcherException(errMessage, e);
        }

    }

    /**
     * Makes axioms for CNF formula.
     *
     * @param hashConceptNumber HashMap for atomic concept of labels with its id.
     * @param cLabMatrix        relation between atomic concept of labels
     * @param sourceNode        interface of source node
     * @param targetNode        interface of target node
     * @return an object of axioms
     */
    protected static Object[] mkAxioms(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) {
        StringBuffer axioms = new StringBuffer();
        Integer numberOfClauses = 0;
        //create variables
        final List<IAtomicConceptOfLabel> sourceNodeACols = sourceNode.getNodeData().getACoLs();
        for (IAtomicConceptOfLabel sourceACoL : sourceNodeACols) {
            //create corresponding to id variable number
            //and put it as a value of hashmap with key equal to ACoL id
            if (!hashConceptNumber.containsKey(sourceACoL)) {
                Integer value = hashConceptNumber.size() + 1;
                hashConceptNumber.put(sourceACoL, value);
            }
        }
        //for all columns of relMatrix
        final List<IAtomicConceptOfLabel> targetNodeACols = targetNode.getNodeData().getACoLs();
        for (IAtomicConceptOfLabel targetACoL : targetNodeACols) {
            //create corresponding to id variable number
            //and put it as a value of hashmap with key equal to ACoL id
            if (!hashConceptNumber.containsKey(targetACoL)) {
                Integer value = hashConceptNumber.size() + 1;
                hashConceptNumber.put(targetACoL, value);
            }
        }

        //for all rows of relMatrix
        for (IAtomicConceptOfLabel sourceACoL : sourceNodeACols) {
            //for all columns of relMatrix
            for (IAtomicConceptOfLabel targetACoL : targetNodeACols) {
                //if there are semantic relation between ACoLS in relMatrix
                char relation = cLabMatrix.getElement(sourceACoL.getIndex(), targetACoL.getIndex());
                if (IMappingElement.IDK != relation) {
                    //get the numbers of DIMACS variables corresponding to ACoLs
                    String sourceVarNumber = (hashConceptNumber.get(sourceACoL)).toString();
                    String targetVarNumber = (hashConceptNumber.get(targetACoL)).toString();
                    if (
                            EvalELMatcher.EXACT_SENSE_MATCH == relation ||//exact match of senses
                                    EvalELMatcher.APPROXIMATE_SENSE_MATCH == relation ||//approximate match of senses
                                    EvalELMatcher.TOKEN_MATCH == relation ||//no senses, token match
                                    EvalELMatcher.LEMMA_MATCH == relation//no senses, lemmas match
                            ) {
                        //if equal
                        if (!sourceVarNumber.equals(targetVarNumber)) {
                            //add clauses for less and more generality
                            String tmp = "-" + sourceVarNumber + " " + targetVarNumber + " 0\n";
                            if ((axioms.indexOf(tmp) != 0) || (axioms.indexOf("\0" + tmp) == -1)) {
                                axioms.append("-").append(sourceVarNumber).append(" ").append(targetVarNumber).append(" 0\n");
                                numberOfClauses++;
                            }
                            tmp = sourceVarNumber + " -" + targetVarNumber + " 0\n";
                            if ((axioms.indexOf(tmp) != 0) || (axioms.indexOf("\0" + tmp) == -1)) {
                                axioms.append(sourceVarNumber).append(" -").append(targetVarNumber).append(" 0\n");
                                numberOfClauses++;
                            }
                        }
                    } else {
                        if (
                                EvalELMatcher.SENSE_MISMATCH == relation ||//senses mismatch, not all golden senses are in source
                                        EvalELMatcher.NO_SENSES == relation ||//no senses where they should be
                                        EvalELMatcher.EXTRA_SENSES == relation //senses present where they should not be
                                ) {
                            //collect stats?
                        } else {
                            System.out.println("Unexpected relation in EvalNodeMatcher: " + relation);
                            //TODO this needs a refactoring to an exception!
                            System.exit(1);
                        }
                    }
                }
            }
        }
        return new Object[]{axioms.toString(), numberOfClauses};
    }

    /**
     * Converts context of node into array list.
     *
     * @param hashConceptNumber HashMap for atomic concept of label with its id
     * @param node              interface of node
     * @return array list of context of node
     */
    private ArrayList<ArrayList<String>> parseFormula(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, INode node, String formula) {
        ArrayList<ArrayList<String>> representation = new ArrayList<ArrayList<String>>();
        boolean saved_negation = false;
        for (StringTokenizer clauseTokenizer = new StringTokenizer(formula, "&"); clauseTokenizer.hasMoreTokens();) {
            String clause = clauseTokenizer.nextToken();
            ArrayList<String> clause_vec = new ArrayList<String>();
            for (StringTokenizer varTokenizer = new StringTokenizer(clause, "|() "); varTokenizer.hasMoreTokens();) {
                String var = varTokenizer.nextToken();
                boolean negation = false;
                if (var.startsWith("~")) {
                    negation = true;
                    var = var.substring(1);
                }
                if (var.length() < 2) {
                    saved_negation = true;
                    continue;
                }
                String var_num = hashConceptNumber.get(node.getNodeData().getAColById(var)).toString();
                if (negation || saved_negation) {
                    saved_negation = false;
                    var_num = "-" + var_num;
                }
                clause_vec.add(var_num);
            }
            representation.add(clause_vec);
        }
        return representation;
    }
}
