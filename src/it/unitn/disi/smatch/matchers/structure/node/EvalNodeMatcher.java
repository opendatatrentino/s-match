package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.matchers.element.EvalELMatcher;
import orbital.logic.imp.Formula;
import orbital.moon.logic.ClassicalLogic;

import java.util.*;

/**
 * Node matcher for evaluation purposes.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class EvalNodeMatcher extends BaseNodeMatcher implements INodeMatcher {

    public char nodeMatch(IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) throws SMatchException {
        char result = MatchManager.IDK_RELATION;
        String sourceCLabFormula = sourceNode.getNodeData().getcLabFormula();
        String targetCLabFormula = targetNode.getNodeData().getcLabFormula();

        if (null != sourceCLabFormula && null != targetCLabFormula && !"".equals(sourceCLabFormula) && !"".equals(targetCLabFormula)
                ) {
            sourceCLabFormula = toCNF(sourceCLabFormula);
            targetCLabFormula = toCNF(targetCLabFormula);

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
            ArrayList<ArrayList<String>> contextAVector = parseFormula(hashConceptNumber, sourceNode, sourceCLabFormula);
            ArrayList<ArrayList<String>> contextBVector = parseFormula(hashConceptNumber, targetNode, targetCLabFormula);
            //create contexts in DIMACS format
            String contextAInDIMACSFormat = DIMACSfromVector(contextAVector);
            String contextBInDIMACSFormat = DIMACSfromVector(contextBVector);

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
            numberOfVariables = negateFormulaInVector(hashConceptNumber, contextBVector, negatedContext);
            //get the sat problem in DIMACS format
            satProblemInDIMACS = axioms + contextAInDIMACSFormat + DIMACSfromVector(negatedContext);
            //get number of clauses for SAT problem
            numberOfClauses = num_of_axiom_clauses + contextAVector.size() + negatedContext.size();
            //add DIMACS header
            DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;
            //do LG test
            isContained = isUnsatisfiable(DIMACSproblem);

            //MG test
            //negate the context
            numberOfVariables = negateFormulaInVector(hashConceptNumber, contextAVector, negatedContext);
            //get the sat problem in DIMACS format
            satProblemInDIMACS = axioms + contextBInDIMACSFormat + DIMACSfromVector(negatedContext);
            //get number of clauses for SAT problem
            numberOfClauses = num_of_axiom_clauses + contextBVector.size() + negatedContext.size();
            //add DIMACS header
            DIMACSproblem = "p cnf " + numberOfVariables + " " + numberOfClauses + "\n" + satProblemInDIMACS;
            //do MG test
            isContains = isUnsatisfiable(DIMACSproblem);

            result = getRelationString(isContains, isContained, false);
        } else {
            if (null == sourceCLabFormula && null == targetCLabFormula || "".equals(sourceCLabFormula) && "".equals(targetCLabFormula)) {
                result = MatchManager.SYNOMYM;
            }
        }
        return result;
    }

    protected static Object[] mkAxioms(Hashtable<IAtomicConceptOfLabel, Integer> hashConceptNumber, IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) {
        StringBuffer axioms = new StringBuffer();
        Integer numberOfClauses = 0;
        //create variables
        final Vector<IAtomicConceptOfLabel> sourceNodeACols = sourceNode.getNodeData().getACoLs();
        for (IAtomicConceptOfLabel sourceACoL : sourceNodeACols) {
            //create corresponding to id variable number
            //and put it as a value of hashtable with key equal to ACoL id
            if (!hashConceptNumber.containsKey(sourceACoL)) {
                Integer value = hashConceptNumber.size() + 1;
                hashConceptNumber.put(sourceACoL, value);
            }
        }
        //for all columns of relMatrix
        final Vector<IAtomicConceptOfLabel> targetNodeACols = targetNode.getNodeData().getACoLs();
        for (IAtomicConceptOfLabel targetACoL : targetNodeACols) {
            //create corresponding to id variable number
            //and put it as a value of hashtable with key equal to ACoL id
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
                if (MatchManager.IDK_RELATION != relation) {
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
                String var_num = null;
                try {
                    var_num = hashConceptNumber.get(node.getNodeData().getAColById(var)).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(2);
                }
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

    public String toCNF(String formula) {
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
                e.printStackTrace();
            }
        } else {
            result = formula;
        }

        return result;
    }

}
