package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches acols of formulas for evaluation.
 * Expects the source and target to be the same context, pre-processed in a different way.
 * To be used with EvalTLMatcher for formulas evaluation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class EvalELMatcher extends Configurable implements IMatcherLibrary {

    private static final Logger log = Logger.getLogger(EvalELMatcher.class);

    //constants for error types

    //exact sense match: sourceSense = targetSenses
    public static final char EXACT_SENSE_MATCH = '=';
    //there some extra sourceSenses
    public static final char APPROXIMATE_SENSE_MATCH = '~';
    //not all target senses are present in source
    public static final char SENSE_MISMATCH = '!';
    //no senses on both sides, matching tokens
    public static final char TOKEN_MATCH = 'T';
    //no senses on both sides, matching lemmas
    public static final char LEMMA_MATCH = 'L';
    //no senses where they should be
    public static final char NO_SENSES = '<';
    //extra senses where they should not be
    public static final char EXTRA_SENSES = '>';


    /**
     * Matches acols of same nodes.
     *
     * @param sourceContext context to be evaluated
     * @param targetContext golden context
     * @return relational matrix of concept of labels
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public IMatchMatrix elementLevelMatching(IContext sourceContext, IContext targetContext) throws MatcherLibraryException {
        //compare each pair of nodes
        //for each pair compare all node acols

        List<IAtomicConceptOfLabel> sourceACoLs = sourceContext.getMatchingContext().getAllContextACoLs();
        List<IAtomicConceptOfLabel> targetACoLs = targetContext.getMatchingContext().getAllContextACoLs();

        //Initialization of matrix
        IMatchMatrix ClabMatrix = MatrixFactory.getInstance(sourceACoLs.size(), targetACoLs.size());

        List<INode> sourceNodes = sourceContext.getAllNodes();
        List<INode> targetNodes = targetContext.getAllNodes();

        long counter = 0;
        long total = (long) sourceNodes.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        for (int i = 0; i < sourceNodes.size(); i++) {
            //should be the same nodes!
            INode sourceNode = sourceNodes.get(i);
            INode targetNode = targetNodes.get(i);

            //check
            String s = sourceNode.getNodeData().getPathToRootString();
            String t = targetNode.getNodeData().getPathToRootString();
            if (!s.equals(t) && log.isEnabledFor(Level.WARN)) {
                log.warn("The nodes mismatch!");
                log.warn(s);
                log.warn(t);
            }

            List<IAtomicConceptOfLabel> sourceNodeACoLs = sourceNode.getNodeData().getACoLs();
            List<IAtomicConceptOfLabel> targetNodeACoLs = targetNode.getNodeData().getACoLs();

            for (IAtomicConceptOfLabel sourceACOL : sourceNodeACoLs) {
                for (IAtomicConceptOfLabel targetACOL : targetNodeACoLs) {
                    ClabMatrix.setElement(sourceACOL.getIndex(), targetACOL.getIndex(), getRelation(sourceACOL, targetACOL));
                }
            }

            counter++;
            if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                log.info(100 * counter / total + "%");
            }
        }
        return ClabMatrix;
    }

    /**
     * Returns a semantic relation between two concept of labels.
     *
     * @param sourceACoL concept of source label
     * @param targetACoL concept of target label
     * @return relation between concept of labels
     */
    public char getRelation(IAtomicConceptOfLabel sourceACoL, IAtomicConceptOfLabel targetACoL) {
        char result = IMappingElement.IDK;

        //if all target (golden) senses are present in source and nothing more - exact match
        //if more are in source - approximate match
        //none - mismatch
        //no senses in target, there are some in source - mismatch
        //no senses everywhere - tokens equality -> match
        //tokens do not match -> lemmas equality -> match

        //compare sets of senses
        List<String> sourceSenses = new ArrayList<String>(sourceACoL.getSenses().getSenseList());
        List<String> targetSenses = new ArrayList<String>(targetACoL.getSenses().getSenseList());

        removeUnknownSenses(sourceSenses);
        removeUnknownSenses(targetSenses);

        boolean tokensEqual = sourceACoL.getToken().toLowerCase().equals(targetACoL.getToken().toLowerCase());

        if (0 < sourceSenses.size() && 0 < targetSenses.size()) {
            //if all target (golden) senses are present in source and nothing more - exact match
            //if more are in source - approximate match
            //none - mismatch
            int idx = 0;
            while (idx < targetSenses.size()) {
                int sIdx = sourceSenses.indexOf(targetSenses.get(idx));
                if (-1 < sIdx) {
                    sourceSenses.remove(sIdx);
                    targetSenses.remove(idx);
                } else {
                    idx++;
                }
            }

            if (0 == targetSenses.size()) {
                if (0 == sourceSenses.size()) {
                    //= exact sense match
                    result = EXACT_SENSE_MATCH;
                } else {
                    //~ approximate sense match
                    //TODO how much approximate
                    //that is, the more senses are there in the synset, the more difficult is the task
                    result = APPROXIMATE_SENSE_MATCH;
                }
            } else {
                //! mismatch
                result = SENSE_MISMATCH;
            }

        } else {
            if (0 == sourceSenses.size() && 0 == targetSenses.size()) {
                //match tokens
                //compare tokens

                if (tokensEqual) {
                    result = TOKEN_MATCH;//T no senses, token match
                } else {
                    //compare lemmas
                    boolean lemmasEqual = sourceACoL.getLemma().toLowerCase().equals(targetACoL.getLemma().toLowerCase());

                    if (lemmasEqual) {
                        result = LEMMA_MATCH;//L no senses, lemmas match
                    }
                }
            } else {
                if (0 == sourceSenses.size()) {
                    //and 0 < targetSenses.size()
                    //incorrect senses
                    //no senses where they should be
                    //<
                    result = NO_SENSES;
                } else {
                    if (0 == targetSenses.size()) {
                        //and 0 < sourceSenses.size()
                        //incorrect senses
                        //senses present where they should not be
                        //>
                        result = EXTRA_SENSES;
                    }
                }
            }
        }

        return result;
    }

    private void removeUnknownSenses(List<String> senses) {
        int i = 0;
        while (i < senses.size()) {
            if (senses.get(i).startsWith(ILinguisticOracle.UNKNOWN_MEANING)) {
                senses.remove(i);
            } else {
                i++;
            }
        }
    }
}