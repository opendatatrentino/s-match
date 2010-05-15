package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.matchers.element.MatcherLibraryException;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.ISynset;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Matches glosses of word senses.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class BasicGlossMatcher extends Configurable {

    private static final Logger log = Logger.getLogger(BasicGlossMatcher.class);

    // linguistic oracle
    private static final String LINGUISTIC_ORACLE_KEY = "linguisticOracle";
    private ILinguisticOracle linguisticOracle = null;

    private static final String SENSE_MATCHER_KEY = "SenseMatcher";
    private ISenseMatcher senseMatcher = null;

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

            properties.clear();
            properties.putAll(newProperties);
        }
    }

    //Next 4 method are used by element level matchers to calculate relations between words

    /**
     * Checks the source is more general than the target or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if the source is more general than target
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public boolean isWordMoreGeneral(String source, String target) throws MatcherLibraryException {
        try {
            List<String> sSenses;
            List<String> tSenses;
            sSenses = linguisticOracle.getSenses(source);
            tSenses = linguisticOracle.getSenses(target);
            if ((sSenses != null) && (tSenses != null))
                if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                    for (String sSense : sSenses) {
                        for (String tSense : tSenses) {
                            if (senseMatcher.isSourceMoreGeneralThanTarget(sSense, tSense))
                                return true;
                        }
                    }
                }
            return false;
        } catch (LinguisticOracleException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MatcherLibraryException(errMessage, e);
        }
    }

    /**
     * Checks the source is less general than the target or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if the source is less general than target
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public boolean isWordLessGeneral(String source, String target) throws MatcherLibraryException {
        try {
            List<String> sSenses;
            List<String> tSenses;
            sSenses = linguisticOracle.getSenses(source);
            tSenses = linguisticOracle.getSenses(target);
            if ((sSenses != null) && (tSenses != null))
                if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                    for (String sSense : sSenses) {
                        for (String tSense : tSenses) {
                            if (senseMatcher.isSourceLessGeneralThanTarget(sSense, tSense))
                                return true;
                        }
                    }
                }
            return false;
        } catch (LinguisticOracleException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MatcherLibraryException(errMessage, e);
        }
    }

    /**
     * Checks the source and target is synonym or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if they are synonym
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public boolean isWordSynonym(String source, String target) throws MatcherLibraryException {
        try {
            List<String> sSenses;
            List<String> tSenses;
            sSenses = linguisticOracle.getSenses(source);
            tSenses = linguisticOracle.getSenses(target);

            if ((sSenses != null) && (tSenses != null))
                if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                    for (String sSense : sSenses) {
                        for (String tSense : tSenses) {
                            if (senseMatcher.isSourceSynonymTarget(sSense, tSense))
                                return true;
                        }
                    }
                }
            return false;
        } catch (LinguisticOracleException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MatcherLibraryException(errMessage, e);
        }
    }

    /**
     * Checks the source and target is opposite or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if they are in opposite relation
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public boolean isWordOpposite(String source, String target) throws MatcherLibraryException {
        try {
            List<String> sSenses;
            List<String> tSenses;
            sSenses = linguisticOracle.getSenses(source);
            tSenses = linguisticOracle.getSenses(target);
            if ((sSenses != null) && (tSenses != null))
                if ((sSenses.size() > 0) && (tSenses.size() > 0)) {
                    for (String sSense : sSenses) {
                        for (String tSense : tSenses) {
                            if (senseMatcher.isSourceOppositeToTarget(sSense, tSense))
                                return true;
                        }
                    }
                }
            return false;
        } catch (LinguisticOracleException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MatcherLibraryException(errMessage, e);
        }
    }

    /**
     * Gets extended gloss i.e. the gloss of parents or children. <br>
     * The direction and depth is according to requirement.
     *
     * @param original  the original gloss of input string
     * @param intSource how much depth the gloss should be taken
     * @param Rel       for less than relation get child gloss and vice versa
     * @return the extended gloss
     * @throws LinguisticOracleException LinguisticOracleException
     */
    public String getExtendedGloss(ISynset original, int intSource, char Rel) throws LinguisticOracleException {
        List<ISynset> children = new ArrayList<ISynset>();
        String result = "";
        if (Rel == IMappingElement.LESS_GENERAL) {
            children = original.getChildren(intSource);
        } else if (Rel == IMappingElement.MORE_GENERAL) {
            children = original.getParents(intSource);
        }
        for (ISynset iSynset : children) {
            String gloss = iSynset.getGloss();
            result = result + gloss + ".";
        }
        return result;
    }
}
