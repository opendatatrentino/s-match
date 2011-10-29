package it.unitn.disi.nlptools.components.sensetaggers;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Tags senses using linguistic oracle.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class LinguisticOracleSenseTagger extends PipelineComponent {

    private static final Logger log = Logger.getLogger(LinguisticOracleSenseTagger.class);

    private static final String ORACLE_KEY = "oracle";
    private ILinguisticOracle oracle;

    public void process(ILabel label) throws PipelineComponentException {
        tagSenses(label.getTokens());
    }

    private void tagSenses(List<? extends IToken> tokens) throws PipelineComponentException {
        for (IToken token : tokens) {
            try {
                List<ISense> senses = oracle.getSenses(token.getText());
                if (0 < senses.size()) {//to save memory with default empty lists already there
                    token.setSenses(senses);
                }
            } catch (LinguisticOracleException e) {
                throw new PipelineComponentException(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading configuration...");
        }
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(ORACLE_KEY)) {
                oracle = (ILinguisticOracle) configureComponent(oracle, oldProperties, newProperties, "linguistic oracle", ORACLE_KEY, ILinguisticOracle.class);
            } else {
                final String errMessage = "Cannot find configuration key " + ORACLE_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }
}