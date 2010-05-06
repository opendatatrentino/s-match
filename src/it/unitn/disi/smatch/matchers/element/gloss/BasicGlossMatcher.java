package it.unitn.disi.smatch.matchers.element.gloss;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.ISynset;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.Vector;

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

            properties = newProperties;
        }
    }

    //Next 4 method are used by element level matchers to calculate relations between words

    /**
     * Checks the source is more general than the target or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if the source is more general than target
     */
    public boolean isWordMoreGeneral(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
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
    }

    /**
     * Checks the source is less general than the target or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if the source is less general than target
     */
    public boolean isWordLessGeneral(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
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
    }

    /**
     * Checks the source and target is synonym or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if they are synonym
     */
    public boolean isWordSynonym(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
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
    }

    /**
     * Checks the source and target is opposite or not.
     *
     * @param source sense of source
     * @param target sense of target
     * @return true if they are in opposite relation
     */
    public boolean isWordOpposite(String source, String target) {
        Vector<String> sSenses;
        Vector<String> tSenses;
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
    }

    /**
     * Gets extended gloss i.e. the gloss of parents or children. <br>
     * The direction and depth is according to requirement.
     *
     * @param original  the original gloss of input string
     * @param intSource how much depth the gloss should be taken
     * @param Rel       for less than relation get child gloss and vice versa
     * @return the extended gloss
     */
    public String getExtendedGloss(ISynset original, int intSource, char Rel) {
        Vector<ISynset> children = new Vector<ISynset>();
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
