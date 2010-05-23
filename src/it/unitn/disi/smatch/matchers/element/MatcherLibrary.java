package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.ContextMapping;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.*;
import it.unitn.disi.smatch.utils.ClassFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This class performs all element level matching routines
 * and provides library of Element level matchers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatcherLibrary extends Configurable implements IMatcherLibrary {

    private static final Logger log = Logger.getLogger(MatcherLibrary.class);

    // sense matcher
    private static final String SENSE_MATCHER_KEY = "senseMatcher";
    private ISenseMatcher senseMatcher = null;

    // linguistic oracle
    private static final String LINGUISTIC_ORACLE_KEY = "linguisticOracle";
    private ILinguisticOracle linguisticOracle = null;

    // exploit only WordNet (false) or use element level semantic matchers library (true)
    private static final String USE_WEAK_SEMANTICS_MATCHERS_KEY = "useWeakSemanticsElementLevelMatchersLibrary";
    private boolean useWeakSemanticsElementLevelMatchersLibrary = true;

    // contains the classes of string matchers (Implementations of IStringBasedElementLevelSemanticMatcher interface)
    private static final String STRING_MATCHERS_KEY = "stringMatchers";
    private List<IStringBasedElementLevelSemanticMatcher> stringMatchers = new ArrayList<IStringBasedElementLevelSemanticMatcher>();

    // contains the classes of sense and gloss based matchers (Implementations of ISenseGlossBasedElementLevelSemanticMatcher interface)
    private static final String SENSE_GLOSS_MATCHERS_KEY = "senseGlossMatchers";
    private List<ISenseGlossBasedElementLevelSemanticMatcher> senseGlossMatchers = new ArrayList<ISenseGlossBasedElementLevelSemanticMatcher>();

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            if (newProperties.containsKey(SENSE_MATCHER_KEY)) {
                senseMatcher = (ISenseMatcher) configureComponent(senseMatcher, properties, newProperties, "sense matcher", SENSE_MATCHER_KEY, ISenseMatcher.class);
            } else {
                final String errMessage = "Cannot find configuration key " + SENSE_MATCHER_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            if (newProperties.containsKey(LINGUISTIC_ORACLE_KEY)) {
                linguisticOracle = (ILinguisticOracle) configureComponent(linguisticOracle, properties, newProperties, "linguistic oracle", LINGUISTIC_ORACLE_KEY, ILinguisticOracle.class);
            } else {
                final String errMessage = "Cannot find configuration key " + LINGUISTIC_ORACLE_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            if (newProperties.containsKey(USE_WEAK_SEMANTICS_MATCHERS_KEY)) {
                useWeakSemanticsElementLevelMatchersLibrary = Boolean.parseBoolean(newProperties.getProperty(USE_WEAK_SEMANTICS_MATCHERS_KEY));
            }

            if (newProperties.containsKey(USE_WEAK_SEMANTICS_MATCHERS_KEY)) {
                useWeakSemanticsElementLevelMatchersLibrary = Boolean.parseBoolean(newProperties.getProperty(USE_WEAK_SEMANTICS_MATCHERS_KEY));
            }

            if (newProperties.containsKey(STRING_MATCHERS_KEY)) {
                stringMatchers.clear();
                for (Object o : ClassFactory.stringToClasses(newProperties.getProperty(STRING_MATCHERS_KEY), ";")) {
                    stringMatchers.add((IStringBasedElementLevelSemanticMatcher) o);
                }
                // common properties for all of them
                Properties p = getComponentProperties(STRING_MATCHERS_KEY + ".*.", newProperties);
                for (IStringBasedElementLevelSemanticMatcher m : stringMatchers) {
                    // specific properties for each of them
                    Properties sp = getComponentProperties(STRING_MATCHERS_KEY + "." + m.getClass().getSimpleName() + ".", newProperties);
                    sp.putAll(p);
                    m.setProperties(sp);
                }

            }

            if (newProperties.containsKey(SENSE_GLOSS_MATCHERS_KEY)) {
                senseGlossMatchers.clear();
                for (Object o : ClassFactory.stringToClasses(newProperties.getProperty(SENSE_GLOSS_MATCHERS_KEY), ";")) {
                    senseGlossMatchers.add((ISenseGlossBasedElementLevelSemanticMatcher) o);
                }
                Properties p = getComponentProperties(SENSE_GLOSS_MATCHERS_KEY + ".*.", newProperties);
                for (ISenseGlossBasedElementLevelSemanticMatcher m : senseGlossMatchers) {
                    Properties sp = getComponentProperties(SENSE_GLOSS_MATCHERS_KEY + "." + m.getClass().getSimpleName() + ".", newProperties);
                    sp.putAll(p);
                    m.setProperties(sp);
                }
            }

            properties.clear();
            properties.putAll(newProperties);
        }
    }

    public IContextMapping<IAtomicConceptOfLabel> elementLevelMatching(IContext sourceContext, IContext targetContext) throws MatcherLibraryException {
        //  Calculate relations between all ACoLs in both contexts and produce the mapping between them.
        //  Corresponds to Step 3 of the semantic matching algorithm.

        IContextMapping<IAtomicConceptOfLabel> result = new ContextMapping<IAtomicConceptOfLabel>(sourceContext, targetContext);

        long counter = 0;
        long total = getACoLCount(sourceContext) * getACoLCount(targetContext);
        long reportInt = (total / 20) + 1;//i.e. report every 5%
        for (Iterator<INode> i = sourceContext.getRoot().getSubtree(); i.hasNext();) {
            INode sourceNode = i.next();
            for (Iterator<IAtomicConceptOfLabel> ii = sourceNode.getNodeData().getACoLs(); ii.hasNext();) {
                IAtomicConceptOfLabel sourceACoL = ii.next();
                for (Iterator<INode> j = targetContext.getRoot().getSubtree(); j.hasNext();) {
                    INode targetNode = j.next();
                    for (Iterator<IAtomicConceptOfLabel> jj = targetNode.getNodeData().getACoLs(); jj.hasNext();) {
                        IAtomicConceptOfLabel targetACoL = jj.next();
                        //Use Element level semantic matchers library
                        //to check the relation holding between two ACoLs represented by lists of WN senses and tokens
                        final char relation = getRelation(sourceACoL, targetACoL);
                        result.setRelation(sourceACoL, targetACoL, relation);

                        counter++;
                        if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                            log.info(100 * counter / total + "%");
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a semantic relation between two concept of labels.
     *
     * @param sourceACoL interface of source label concept
     * @param targetACoL interface of target label concept
     * @return relation between concept of labels
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public char getRelation(IAtomicConceptOfLabel sourceACoL, IAtomicConceptOfLabel targetACoL) throws MatcherLibraryException {
        try {
            sourceACoL.getSenses().convertSenses();
            targetACoL.getSenses().convertSenses();

            List<String> sourceSenses = sourceACoL.getSenses().getSenseList();
            List<String> targetSenses = targetACoL.getSenses().getSenseList();
            char relation = senseMatcher.getRelationACoL(sourceACoL, targetACoL);

            //if WN matcher did not find relation
            if (IMappingElement.IDK == relation) {
                if (useWeakSemanticsElementLevelMatchersLibrary) {
                    //use string based matchers
                    relation = getRelationFromStringMatchers(sourceACoL.getLemma(), targetACoL.getLemma());
                    //if they did not find relation
                    if (IMappingElement.IDK == relation) {
                        //use sense and gloss based matchers
                        relation = getRelationFromSenseGlossMatchers(sourceSenses, targetSenses);
                    }
                }
            } else {
                sourceACoL.getSenses().setSenseList(sourceSenses);
                targetACoL.getSenses().setSenseList(targetSenses);
            }

            return relation;
        } catch (SenseMatcherException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MatcherLibraryException(errMessage, e);
        }
    }

    /**
     * Returns semantic relation holding between two labels as computed by string based matchers.
     *
     * @param sourceLabel the string of the source label
     * @param targetLabel the string of the target label
     * @return semantic relation holding between two labels as computed by string based matchers
     */
    private char getRelationFromStringMatchers(String sourceLabel, String targetLabel) {
        char relation = IMappingElement.IDK;
        int i = 0;
        while ((relation == IMappingElement.IDK) && (i < stringMatchers.size())) {
            relation = stringMatchers.get(i).match(sourceLabel, targetLabel);
            i++;
        }
        return relation;
    }

    /**
     * Returns semantic relation between two ACoLs (represented by lists of WN senses) by WN sense based matchers.
     *
     * @param sourceSenses the string of sense of source label
     * @param targetSenses the string of sense of target label
     * @return semantic relation between two ACoLs of labels computed by WN sense based matchers
     * @throws MatcherLibraryException MatcherLibraryException
     */
    private char getRelationFromSenseGlossMatchers(List<String> sourceSenses, List<String> targetSenses) throws MatcherLibraryException {
        try {
            char relation = IMappingElement.IDK;
            for (String sourceSense : sourceSenses) {
                ISynset sourceSynset = linguisticOracle.getISynset(sourceSense);
                for (String targetSense : targetSenses) {
                    ISynset targetSynset = linguisticOracle.getISynset(targetSense);
                    int k = 0;
                    while ((relation == IMappingElement.IDK) && (k < senseGlossMatchers.size())) {
                        relation = senseGlossMatchers.get(k).match(sourceSynset, targetSynset);
                        k++;
                    }
                    return relation;
                }
            }
            return relation;
        } catch (LinguisticOracleException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MatcherLibraryException(errMessage, e);
        }
    }

    private long getACoLCount(IContext context) {
        long result = 0;
        for (Iterator<INode> i = context.getRoot().getSubtree(); i.hasNext();) {
            for (Iterator<IAtomicConceptOfLabel> j = i.next().getNodeData().getACoLs(); j.hasNext();) {
                j.next();
                result++;
            }
        }
        return result;
    }
}