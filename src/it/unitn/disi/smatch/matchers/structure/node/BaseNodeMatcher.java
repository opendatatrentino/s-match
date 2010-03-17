package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.deciders.ISATSolver;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * Contains routines used by many other matchers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class BaseNodeMatcher {
	// create an interface of SAT solver
    protected ISATSolver satSolver = (ISATSolver) MatchManager.getClassForName(MatchManager.satSolverClass);

    /**
     * Makes axioms for CNF formula.
     *
     * @param hashConceptNumber HashMap for atomic concept of labels with its id.
     * @param cLabMatrix relation between atomic concept of labels
     * @param sourceNode interface of source node
     * @param targetNode interface of target node
     * @return an object of axioms
     */
    protected static Object[] mkAxioms(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, IMatchMatrix cLabMatrix, INode sourceNode, INode targetNode) {
        StringBuffer axioms = new StringBuffer();
        Integer numberOfClauses = 0;
        //create variables
        for (IAtomicConceptOfLabel sourceACoL : sourceNode.getNodeData().getNodeMatchingTaskACols()) {
            //create corresponding to id variable number
            //and put it as a value of hash table with key equal to ACoL id
            if (!hashConceptNumber.containsKey(sourceACoL)) {
                Integer value = hashConceptNumber.size() + 1;
                hashConceptNumber.put(sourceACoL, value);
            }
        }
        //for all columns of relMatrix
        for (IAtomicConceptOfLabel targetACoL : targetNode.getNodeData().getNodeMatchingTaskACols()) {
            //create corresponding to id variable number
            //and put it as a value of hashtable with key equal to ACoL id
            if (!hashConceptNumber.containsKey(targetACoL)) {
                Integer value = hashConceptNumber.size() + 1;
                hashConceptNumber.put(targetACoL, value);
            }
        }

        //for all rows of relMatrix
        for (IAtomicConceptOfLabel sourceACoL : sourceNode.getNodeData().getNodeMatchingTaskACols()) {
            //for all columns of relMatrix
            for (IAtomicConceptOfLabel targetACoL : targetNode.getNodeData().getNodeMatchingTaskACols()) {
                //if there are semantic relation between ACoLS in relMatrix
                char relation = cLabMatrix.getElement(sourceACoL.getIndex(), targetACoL.getIndex());
                if (MatchManager.IDK_RELATION != relation) {
                    //get the numbers of DIMACS variables corresponding to ACoLs
                    String sourceVarNumber = (hashConceptNumber.get(sourceACoL)).toString();
                    String targetVarNumber = (hashConceptNumber.get(targetACoL)).toString();
                    //if LG
                    if (MatchManager.LESS_GENERAL_THAN == relation) {
                        //create corresponding clause
                        String tmp = "-" + sourceVarNumber + " " + targetVarNumber + " 0\n";
                        //if not already present add to axioms
                        if ((axioms.indexOf(tmp) != 0) || (axioms.indexOf("\0" + tmp) == -1)) {
                            axioms.append("-").append(sourceVarNumber).append(" ").append(targetVarNumber).append(" 0\n");
                            //increment number of clauses
                            numberOfClauses++;
                        }
                    } else if (MatchManager.MORE_GENERAL_THAN == relation) {
                        //if MG
                        //create corresponding clause
                        String tmp = sourceVarNumber + " -" + targetVarNumber + " 0\n";
                        //if not already present add to axioms
                        if ((axioms.indexOf(tmp) != 0) || (axioms.indexOf("\0" + tmp) == -1)) {
                            axioms.append(sourceVarNumber).append(" -").append(targetVarNumber).append(" 0\n");
                            //increment number of clauses
                            numberOfClauses++;
                        }
                    } else if (MatchManager.SYNOMYM == relation) {
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
                    } else if (MatchManager.OPPOSITE_MEANING == relation) {
                        //if disjointness
                        //create corresponding clause
                        String tmp = "-" + sourceVarNumber + " -" + targetVarNumber + " 0\n";
                        //if not already present add to axioms
                        if ((axioms.indexOf(tmp) != 0) || (axioms.indexOf("\0" + tmp) == -1)) {
                            axioms.append("-").append(sourceVarNumber).append(" -").append(targetVarNumber).append(" 0\n");
                            numberOfClauses++;
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
     * @param node interface of the node
     * @return array list of context of node
     */
    protected ArrayList<ArrayList<String>> parseFormula(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, INode node) {
        ArrayList<ArrayList<String>> representation = new ArrayList<ArrayList<String>>();
        boolean saved_negation = false;
        for (StringTokenizer clauseTokenizer = new StringTokenizer(node.getNodeData().getCNodeFormula(), "&"); clauseTokenizer.hasMoreTokens();) {
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
                    var_num = hashConceptNumber.get(node.getNodeData().getNMTAColById(var)).toString();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
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

    /**
     * Converts array list of context of node into DIMACS form.
     *
     * @param tmp array list of context of a node
     * @return nodes in DIMACS format
     */
    protected static String DIMACSfromVector(ArrayList<ArrayList<String>> tmp) {
        StringBuffer DIMACS = new StringBuffer("");
        for (ArrayList<String> clause : tmp) {
            for (String aClause : clause) {
                DIMACS.append(aClause).append(" ");
            }
            DIMACS.append(" 0\n");
        }
        return DIMACS.toString();
    }
    // TODO Need comments
    protected static int negateFormulaInVector(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, ArrayList<ArrayList<String>> pivot, ArrayList<ArrayList<String>> result) {
        //ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        result.clear();
        ArrayList<String> firstClause = new ArrayList<String>();
        int numberOfVariables = hashConceptNumber.size();
        try {
            for (ArrayList<String> v : pivot) {
                if (v.size() == 1) {
                    firstClause.add(changeSign(v.get(0)));
                }
                if (v.size() > 1) {
                    numberOfVariables++;
                    String lsn = Integer.toString(numberOfVariables);
                    firstClause.add("-" + lsn);
                    ArrayList<String> longClause = new ArrayList<String>();
                    longClause.add("-" + lsn);
                    for (String var : v) {
                        longClause.add(var);
                        ArrayList<String> tmp = new ArrayList<String>();
                        tmp.add(lsn);
                        tmp.add(changeSign(var));
                        result.add(tmp);
                    }
                    result.add(longClause);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstClause.size() > 0)
            result.add(firstClause);
        return numberOfVariables;
    }

    protected boolean isUnsatisfiable(String satProblem) throws SMatchException {
        boolean satResult = satSolver.isSatisfiable(satProblem);
        return !satResult;
    }

    protected static char getRelationString(boolean isContains, boolean isContained, boolean isOpposite) {
        //return the tests results
        if (isOpposite) {
            //The concepts have opposite menaning
            return MatchManager.OPPOSITE_MEANING;
        }
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
        return MatchManager.IDK_RELATION;
    }

    protected static String changeSign(String strClause) {
        if (strClause.trim().startsWith("-")) {
            strClause = strClause.substring(1);
        } else {
            strClause = "-" + strClause;// + " ";
        }
        return strClause;
    }
}
