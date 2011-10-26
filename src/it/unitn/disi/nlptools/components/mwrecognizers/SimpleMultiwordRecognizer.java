package it.unitn.disi.nlptools.components.mwrecognizers;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.IMultiWord;
import it.unitn.disi.nlptools.data.ISentence;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.data.MultiWord;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Recognizes multiwords within consecutive tokens. Given:
 * [a] [cappella] [and] [gospel] [singing], it finds two multiwords {0,1} and {3,4}.
 * <p/>
 * Optionally joins multiwords, replacing original tokens with multiwords.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SimpleMultiwordRecognizer extends PipelineComponent {

    private static final Logger log = Logger.getLogger(SimpleMultiwordRecognizer.class);

    private static final String ORACLE_KEY = "oracle";
    private ILinguisticOracle oracle;

    private static final String JOIN_TOKENS_KEY = "joinTokens";
    private boolean joinTokens = true;

    private static final Comparator<IMultiWord> mwComparator = new Comparator<IMultiWord>() {
        public int compare(IMultiWord o1, IMultiWord o2) {
            return o2.getTokens().size() - o1.getTokens().size();
        }
    };

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

            if (newProperties.containsKey(JOIN_TOKENS_KEY)) {
                joinTokens = Boolean.parseBoolean((String) newProperties.get(JOIN_TOKENS_KEY));
            }
        }
        return result;
    }

    public void process(ISentence sentence) throws PipelineComponentException {
        for (int i = 0; i < sentence.getTokens().size(); i++) {
            IToken token = sentence.getTokens().get(i);
            ArrayList<ArrayList<String>> entries;
            try {
                entries = oracle.getMultiwords(token.getLemma().toLowerCase());
            } catch (LinguisticOracleException e) {
                throw new PipelineComponentException(e.getMessage(), e);
            }
            if (null != entries && 0 < entries.size()) {
                for (int j = 0, maxJ = entries.size(); j < maxJ; j++) {
                    int mwIdx = 0;
                    while (mwIdx < entries.get(j).size() && (mwIdx < (sentence.getTokens().size() - i))) {
                        if (sentence.getTokens().get(i + mwIdx).getText().equalsIgnoreCase(entries.get(j).get(mwIdx)) ||
                                //last token can be in plural
                                (entries.get(j).size() - 1 == mwIdx && sentence.getTokens().get(i + mwIdx).getLemma().equalsIgnoreCase(entries.get(j).get(mwIdx)))
                                ) {
                            mwIdx++;
                        } else {
                            break;
                        }
                    }
                    if (mwIdx == entries.get(j).size()) {
                        StringBuilder b = new StringBuilder();
                        for (String piece : entries.get(j)) {
                            b.append(piece).append(' ');
                        }
                        MultiWord mw = new MultiWord(b.substring(0, b.length() - 1));
                        ArrayList<Integer> indexes = new ArrayList<Integer>();
                        ArrayList<IToken> tokens = new ArrayList<IToken>();
                        for (int idx = 0; idx < entries.get(j).size(); idx++) {
                            indexes.add(i + idx);
                            tokens.add(sentence.getTokens().get(i + idx));
                        }
                        mw.setTokenIndexes(indexes);
                        mw.setTokens(tokens);
                        mw.setLemma(mw.getText());
                        mw.setPOSTag(sentence.getTokens().get(i + mwIdx - 1).getPOSTag());
                        if (0 == sentence.getMultiWords().size()) {
                            sentence.setMultiWords(new ArrayList<IMultiWord>(Arrays.asList(mw)));
                        } else {
                            sentence.getMultiWords().add(mw);
                        }
                    }
                }//for entries
            }//if entries
        }

        if (joinTokens) {
            //start from longest ones, to handle cases like "adult male", "adult male body"
            ArrayList<IMultiWord> mws = new ArrayList<IMultiWord>(sentence.getMultiWords());
            Collections.sort(mws, mwComparator);
            for (IMultiWord multiWord : mws) {
                int idx = sentence.getTokens().indexOf(multiWord.getTokens().get(0));
                if (-1 < idx) {
                    for (IToken token : multiWord.getTokens()) {
                        sentence.getTokens().remove(token);
                    }
                    sentence.getTokens().add(idx, multiWord);
                }
            }
        }
    }
}
