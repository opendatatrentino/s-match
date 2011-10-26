package it.unitn.disi.nlptools.components.tokenizers;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.data.ISentence;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.data.Token;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Tokenizes the sentence using OpenNLP tokenizer.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class OpenNLPTokenizer extends PipelineComponent {

    private static final Logger log = Logger.getLogger(OpenNLPTokenizer.class);

    private static final String MODEL_FILE_NAME_KEY = "model";
    private String modelFileName;

    private Tokenizer tokenizer;

    public void process(ISentence sentence) {
        String tokens[] = tokenizer.tokenize(sentence.getText());
        List<IToken> tokenList = new ArrayList<IToken>(tokens.length);
        for (String token : tokens) {
            tokenList.add(new Token(token));
        }
        sentence.setTokens(tokenList);
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading configuration...");
        }
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(MODEL_FILE_NAME_KEY)) {
                String newModelFileName = (String) newProperties.get(MODEL_FILE_NAME_KEY);
                if (null != newModelFileName && !"".equals(newModelFileName) && !newModelFileName.equals(modelFileName)) {
                    modelFileName = newModelFileName;
                    if (log.isEnabledFor(Level.INFO)) {
                        log.info("Loading model: " + modelFileName);
                    }

                    InputStream modelIn = null;
                    try {
                        modelIn = new FileInputStream(modelFileName);
                        TokenizerModel model = new TokenizerModel(modelIn);
                        tokenizer = new TokenizerME(model);
                    } catch (IOException e) {
                        throw new ConfigurableException(e.getMessage(), e);
                    } finally {
                        if (modelIn != null) {
                            try {
                                modelIn.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            } else {
                final String errMessage = "Cannot find configuration key " + MODEL_FILE_NAME_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }
}
