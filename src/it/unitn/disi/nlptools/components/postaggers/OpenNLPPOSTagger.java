package it.unitn.disi.nlptools.components.postaggers;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.pipelines.LabelPipelineComponent;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Tags the label using OpenNLP POS tagger.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class OpenNLPPOSTagger extends LabelPipelineComponent {

    private static final Logger log = Logger.getLogger(OpenNLPPOSTagger.class);

    private static final String MODEL_FILE_NAME_KEY = "model";
    private String modelFileName;

    private POSTaggerME tagger;

    public void process(ILabel instance) {
        String tokens[] = new String[instance.getTokens().size()];
        for (int i = 0; i < instance.getTokens().size(); i++) {
            tokens[i] = instance.getTokens().get(i).getText();
        }
        String[] tags = tagger.tag(tokens);
        for (int i = 0; i < instance.getTokens().size(); i++) {
            instance.getTokens().get(i).setPOSTag(tags[i]);
        }
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
                if (null != newModelFileName && !newModelFileName.isEmpty() && !newModelFileName.equals(modelFileName)) {
                    modelFileName = newModelFileName;
                    if (log.isEnabledFor(Level.INFO)) {
                        log.info("Loading model: " + modelFileName);
                    }

                    InputStream modelIn = null;
                    try {
                        modelIn = new FileInputStream(modelFileName);
                        POSModel model = new POSModel(modelIn);
                        tagger = new POSTaggerME(model);
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
