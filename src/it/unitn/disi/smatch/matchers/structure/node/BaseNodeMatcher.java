package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.deciders.ISATSolver;
import it.unitn.disi.smatch.deciders.SATSolverException;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Contains routines used by other matchers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class BaseNodeMatcher extends Configurable {

    private static final Logger log = Logger.getLogger(BaseNodeMatcher.class);

    private static final String SAT_SOLVER_KEY = "SATSolver";
    protected ISATSolver satSolver = null;

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            satSolver = (ISATSolver) configureComponent(satSolver, properties, newProperties, "SAT solver", SAT_SOLVER_KEY, ISATSolver.class);

            properties.clear();
            properties.putAll(newProperties);
        }
    }

    /**
     * Makes axioms for a CNF formula out of relations between atomic concepts.
     *
     * @param hashConceptNumber HashMap for atomic concept of labels with its id
     * @param acolMapping       mapping between atomic concepts
     * @param sourceNode        source node
     * @param targetNode        target node
     * @return axiom string and axiom count
     */
    protected static Object[] mkAxioms(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, IContextMapping<IAtomicConceptOfLabel> acolMapping, INode sourceNode, INode targetNode) {
        StringBuilder axioms = new StringBuilder();
        Integer numberOfClauses = 0;
        // create DIMACS variables for all acols in the matching task
        createVariables(hashConceptNumber, sourceNode);
        createVariables(hashConceptNumber, targetNode);

        for (Iterator<IAtomicConceptOfLabel> i = sourceNode.getNodeData().getNodeMatchingTaskACoLs(); i.hasNext();) {
            IAtomicConceptOfLabel sourceACoL = i.next();
            for (Iterator<IAtomicConceptOfLabel> j = targetNode.getNodeData().getNodeMatchingTaskACoLs(); j.hasNext();) {
                IAtomicConceptOfLabel targetACoL = j.next();
                char relation = acolMapping.getRelation(sourceACoL, targetACoL);
                if (IMappingElement.IDK != relation) {
                    //get the numbers of DIMACS variables corresponding to ACoLs
                    String sourceVarNumber = (hashConceptNumber.get(sourceACoL)).toString();
                    String targetVarNumber = (hashConceptNumber.get(targetACoL)).toString();
                    if (IMappingElement.LESS_GENERAL == relation) {
                        String tmp = "-" + sourceVarNumber + " " + targetVarNumber + " 0\n";
                        //if not already present add to axioms
                        if ((axioms.indexOf(tmp) != 0) || (axioms.indexOf("\0" + tmp) == -1)) {
                            axioms.append("-").append(sourceVarNumber).append(" ").append(targetVarNumber).append(" 0\n");
                            numberOfClauses++;
                        }
                    } else if (IMappingElement.MORE_GENERAL == relation) {
                        String tmp = sourceVarNumber + " -" + targetVarNumber + " 0\n";
                        if ((axioms.indexOf(tmp) != 0) || (axioms.indexOf("\0" + tmp) == -1)) {
                            axioms.append(sourceVarNumber).append(" -").append(targetVarNumber).append(" 0\n");
                            numberOfClauses++;
                        }
                    } else if (IMappingElement.EQUIVALENCE == relation) {
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
                    } else if (IMappingElement.DISJOINT == relation) {
                        String tmp = "-" + sourceVarNumber + " -" + targetVarNumber + " 0\n";
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

    private static void createVariables(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, INode node) {
        // creates DIMACS variables for all concepts in the node matching task
        for (Iterator<IAtomicConceptOfLabel> i = node.getNodeData().getNodeMatchingTaskACoLs(); i.hasNext();) {
            IAtomicConceptOfLabel sourceACoL = i.next();
            //create corresponding to id variable number
            //and put it as a value of hash table with key equal to ACoL
            if (!hashConceptNumber.containsKey(sourceACoL)) {
                Integer value = hashConceptNumber.size() + 1;
                hashConceptNumber.put(sourceACoL, value);
            }
        }
    }

    /**
     * Parses a c@node formula replacing references to acols with references to the DIMACS variables. Uses and depends
     * on CNF representation which is "conjunction of disjunctions",  that is the first level list represents
     * conjunction of second-level lists representing disjunction clauses.
     *
     * @param hashConceptNumber HashMap acol -> variable number
     * @param acolsMap          map with acol id -> acol mapping
     * @param node              node
     * @return formula with DIMACS variables
     */
    protected ArrayList<ArrayList<String>> parseFormula(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber,
                                                        Map<String, IAtomicConceptOfLabel> acolsMap, INode node) {
        ArrayList<ArrayList<String>> representation = new ArrayList<ArrayList<String>>();
        boolean saved_negation = false;
        for (StringTokenizer clauseTokenizer = new StringTokenizer(node.getNodeData().getcNodeFormula(), "&"); clauseTokenizer.hasMoreTokens();) {
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
                String var_num = hashConceptNumber.get(acolsMap.get(var)).toString();
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
     * Converts parsed formula into DIMACS format.
     *
     * @param formula parsed formula
     * @return formula in DIMACS format
     */
    protected static String DIMACSfromList(ArrayList<ArrayList<String>> formula) {
        StringBuilder dimacs = new StringBuilder("");
        for (List<String> conjClause : formula) {
            for (String disjClause : conjClause) {
                dimacs.append(disjClause).append(" ");
            }
            dimacs.append(" 0\n");
        }
        return dimacs.toString();
    }

    protected static int negateFormulaInList(HashMap<IAtomicConceptOfLabel, Integer> hashConceptNumber, ArrayList<ArrayList<String>> pivot, ArrayList<ArrayList<String>> result) {
        result.clear();
        ArrayList<String> firstClause = new ArrayList<String>();
        int numberOfVariables = hashConceptNumber.size();
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
        if (firstClause.size() > 0)
            result.add(firstClause);
        return numberOfVariables;
    }

    protected boolean isUnsatisfiable(String satProblem) throws NodeMatcherException {
        try {
            return !satSolver.isSatisfiable(satProblem);
        } catch (SATSolverException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new NodeMatcherException(errMessage, e);
        }
    }

    protected static char getRelationString(boolean isContains, boolean isContained, boolean isOpposite) {
        //return the tests results
        if (isOpposite) {
            //The concepts have opposite meaning
            return IMappingElement.DISJOINT;
        }
        if (isContains && isContained) {
            //The concepts are equivalent
            return IMappingElement.EQUIVALENCE;
        }
        if (isContained) {
            //The source concept is LG the target concept
            return IMappingElement.LESS_GENERAL;
        }
        if (isContains) {
            //The target concept is LG the source concept
            return IMappingElement.MORE_GENERAL;
        }
        return IMappingElement.IDK;
    }

    protected static String changeSign(String strClause) {
        if (strClause.trim().startsWith("-")) {
            strClause = strClause.substring(1);
        } else {
            strClause = "-" + strClause;
        }
        return strClause;
    }
}
