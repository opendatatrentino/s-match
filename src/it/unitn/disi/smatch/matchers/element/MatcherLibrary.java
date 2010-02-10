package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.oracles.ISynset;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Vector;

/**
 * This class performs all element level matching routines
 * and provides library of Element level matchers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatcherLibrary implements IMatcherLibrary {

    private static final Logger log = Logger.getLogger(MatcherLibrary.class);

    private static boolean cachecLabsMatrix = false;

    public char getRelation(IAtomicConceptOfLabel sourceACoL, IAtomicConceptOfLabel targetACoL) {
        sourceACoL.getSenses().convertSenses();
        targetACoL.getSenses().convertSenses();

        Vector<String> sourceSenses = sourceACoL.getSenses().getSenseList();
        Vector<String> targetSenses = targetACoL.getSenses().getSenseList();
        char relation = MatchManager.getIWNMatcher().getRelationACoL(sourceACoL, targetACoL);

        //if WN matcher did not find relation
        if (MatchManager.IDK_RELATION == relation) {
            if (MatchManager.useWeakSemanticsElementLevelMatchersLibrary) {
                //use string based matchers
                relation = getRelationFromStringMatchers(sourceACoL.getLemma(), targetACoL.getLemma());
                //if they did not find relation
                if (MatchManager.IDK_RELATION == relation) {
                    //use sense and gloss based matchers
                    relation = getRelationFromSenseGlossMatchers(sourceSenses, targetSenses);
                }
            }
        } else {
            sourceACoL.getSenses().setSenseList(sourceSenses);
            targetACoL.getSenses().setSenseList(targetSenses);
        }

        return relation;
    }

    /**
     * Return semantic relation holding between two labels as computed by string based matchers.
     *
     * @param sourceLabel sourceLabel
     * @param targetLabel targetLabel
     * @return semantic relation holding between two labels as computed by string based matchers
     */
    private char getRelationFromStringMatchers(String sourceLabel, String targetLabel) {
        char relation = MatchManager.IDK_RELATION;
        int i = 0;
        while ((relation == MatchManager.IDK_RELATION) && (i < MatchManager.stringMatchers.size())) {
            IStringBasedElementLevelSemanticMatcher stringMatcher = (IStringBasedElementLevelSemanticMatcher) MatchManager.stringMatchers.get(i);
            relation = stringMatcher.match(sourceLabel, targetLabel);
            i++;
        }
        return relation;
    }

    /**
     * Returns semantic relation between two ACoLs (represented by Vectors of WN senses).
     *
     * @param sourceSenses sourceSenses
     * @param targetSenses targetSenses
     * @return semantic relation between two ACoLs (represented by Vectors of WN senses)
     */
    private char getRelationFromSenseGlossMatchers(Vector<String> sourceSenses, Vector<String> targetSenses) {
        String synSource;
        String synTarget;
        ISynset sourceSynset;
        ISynset targetSynset;
        char relation = MatchManager.IDK_RELATION;
        for (String sourceSense : sourceSenses) {
            synSource = sourceSense;
            sourceSynset = MatchManager.getLinguisticOracle().getISynset(synSource);
            if (!sourceSynset.isNull()) {
                for (String targetSense : targetSenses) {
                    synTarget = targetSense;
                    targetSynset = MatchManager.getLinguisticOracle().getISynset(synTarget);
                    if (!targetSynset.isNull()) {
                        int k = 0;
                        while ((relation == MatchManager.IDK_RELATION) && (k < MatchManager.senseGlossMatchers.size())) {
                            ISenseGlossBasedElementLevelSemanticMatcher senseGlossMatcher = (ISenseGlossBasedElementLevelSemanticMatcher) MatchManager.senseGlossMatchers.get(k);
                            relation = senseGlossMatcher.match(sourceSynset, targetSynset);
                            k++;
                        }
                        return relation;
                    }
                }
            }
        }
        return relation;
    }

    /**
     * Performs Step 3 of semantic matching algorithm
     *
     * @return matrix of semantic relatons between nodes in both contexts
     */
    public IMatchMatrix elementLevelMatching(IContext sourceContext, IContext targetContext) throws SMatchException {
        IMatchMatrix ClabMatrix = null;
        try {
            //get all ACoLs in contexts
            Vector<IAtomicConceptOfLabel> sourceACoLs = sourceContext.getMatchingContext().getAllContextACoLs();
            Vector<IAtomicConceptOfLabel> targetACoLs = targetContext.getMatchingContext().getAllContextACoLs();

            //  Calculate relations between all ACoLs in both contexts and produce the matrix of
            //  semantic relations between them.
            //  Corresponds to Step 3 of the semantic matching algorithm.
            MatchManager.printMemoryUsage();

            //Initialization of matrix
            String matrixFileName = MatchManager.ctxsSourceFile + ".cLabMatrix";
            File matrixFile = new File(matrixFileName);
            if (cachecLabsMatrix && matrixFile.exists()) {
                ClabMatrix = readMatrix(matrixFileName);
            } else {
                ClabMatrix = MatrixFactory.getInstance(sourceACoLs.size(), targetACoLs.size());

                MatchManager.printMemoryUsage();
                // for all ACoLs in source context
                long counter = 0;
                long total = (long) sourceACoLs.size() * (long) targetACoLs.size();
                long reportInt = (total / 20) + 1;//i.e. report every 5%
                for (int row = 0; row < sourceACoLs.size(); row++) {
                    IAtomicConceptOfLabel sourceACoL = sourceACoLs.get(row);
                    for (int col = 0; col < targetACoLs.size(); col++) {
                        IAtomicConceptOfLabel targetACoL = targetACoLs.get(col);
                        //Use Element level semantic matchers library
                        //in order to check the relation holding between two ACoLs represented
                        //by Vectors of WN senses and tokens
                        ClabMatrix.setElement(row, col, getRelation(sourceACoL, targetACoL));

                        counter++;
                        if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                            log.info(100 * counter / total + "%");
                        }
                    }
                    ClabMatrix.endOfRow();
                }
                if (cachecLabsMatrix) {
                    writeMatrix(ClabMatrix, matrixFileName);
                }
            }
        } catch (Exception e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                log.error("The cLab matrix is not complete due to an exception");
                throw new SMatchException("The cLab matrix is not complete due to an exception", e);
            }
        }
        return ClabMatrix;
    }

    public static void writeMatrix(IMatchMatrix h, String fileName) {
        log.info("Writing cLabMatrix to " + fileName);
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(h);
            oos.close();
            fos.close();
        } catch (IOException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                //throw new SMatchException(errMessage, e);
            }
        }
    }

    public static IMatchMatrix readMatrix(String fileName) {
        log.info("Reading cLabMatrix from " + fileName);
        IMatchMatrix result = null;
        try {
            FileInputStream fos = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fos, MatchManager.BUFFER_SIZE);
            ObjectInputStream oos = new ObjectInputStream(bis);
            try {
                result = (IMatchMatrix) oos.readObject();
            } catch (IOException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    //throw new SMatchException(errMessage, e);
                }
            } catch (ClassNotFoundException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                    //throw new SMatchException(errMessage, e);
                }
            }
            oos.close();
            bis.close();
            fos.close();
        } catch (IOException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                //throw new SMatchException(errMessage, e);
            }
        }
        return result;
    }
}