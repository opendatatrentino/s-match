package it.unitn.disi.nlptools.components.tokenizers;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.NLPToolsConstants;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.data.Token;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Implements simple rule-based tokenization.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SimpleTokenizer extends PipelineComponent {

    private static final Logger log = Logger.getLogger(SimpleTokenizer.class);

    private Pattern tokenPattern;

    private static final String DELIMITERS_KEY = "delimiters";
    private String delimiters = NLPToolsConstants.DELIMITERS_EXCLUDING_BRACKETS;

    public void process(ILabel label) {
        String[] tokens = tokenPattern.split(label.getText());
        List<IToken> tokenList = new ArrayList<IToken>(tokens.length);
        for (String token : tokens) {
            tokenList.add(new Token(token));
        }
        label.setTokens(tokenList);
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading configuration...");
        }
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(DELIMITERS_KEY)) {
                delimiters = (String) newProperties.get(DELIMITERS_KEY);
            }
        }

        tokenPattern = Pattern.compile(delimiters);

        return result;
    }
}