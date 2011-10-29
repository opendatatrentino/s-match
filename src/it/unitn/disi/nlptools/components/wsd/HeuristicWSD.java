package it.unitn.disi.nlptools.components.wsd;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.oracles.SenseMatcherException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Disambiguates senses using simple heuristics.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class HeuristicWSD extends PipelineComponent {

    private static final String SENSE_MATCHER_KEY = "senseMatcher";
    private ISenseMatcher senseMatcher = null;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(SENSE_MATCHER_KEY)) {
                senseMatcher = (ISenseMatcher) configureComponent(senseMatcher, oldProperties, newProperties, "sense matcher", SENSE_MATCHER_KEY, ISenseMatcher.class);
            } else {
                final String errMessage = "Cannot find configuration key " + SENSE_MATCHER_KEY;
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }

    public void process(ILabel label) throws PipelineComponentException {
        HashMap<IToken, List<ISense>> refinedSenses = new HashMap<IToken, List<ISense>>();

        try {
            for (IToken sourceToken : label.getTokens()) {
                for (IToken targetToken : label.getTokens()) {
                    if (!targetToken.equals(sourceToken)) {
                        for (ISense sourceSense : sourceToken.getSenses()) {
                            for (ISense targetSense : targetToken.getSenses()) {
                                if (senseMatcher.isSourceSynonymTarget(sourceSense, targetSense) ||
                                        senseMatcher.isSourceLessGeneralThanTarget(sourceSense, targetSense) ||
                                        senseMatcher.isSourceMoreGeneralThanTarget(sourceSense, targetSense)) {
                                    addToRefinedSenses(refinedSenses, sourceToken, sourceSense);
                                    addToRefinedSenses(refinedSenses, targetToken, targetSense);
                                }
                            }
                        }
                    }
                }
            }

            //sense disambiguation in context
            for (IToken sourceToken : label.getTokens()) {
                if (!refinedSenses.containsKey(sourceToken)) {
                    for (ISense sourceSense : sourceToken.getSenses()) {
                        // for all context labels
                        senseFilteringAmong(label.getContext(), sourceSense, sourceToken, refinedSenses);
                    }
                }
            }

            //replace sense with refined ones, if there are any
            for (IToken token : label.getTokens()) {
                List<ISense> refined = refinedSenses.get(token);
                if (null != refined) {
                    token.setSenses(refined);
                }
            }
        } catch (SenseMatcherException e) {
            throw new PipelineComponentException(e.getMessage(), e);
        }
    }

    private void senseFilteringAmong(List<ILabel> context, ISense sourceSense, IToken sourceToken, HashMap<IToken, List<ISense>> refinedSenses) throws SenseMatcherException {
        for (ILabel targetLabel : context) {
            for (IToken targetToken : targetLabel.getTokens()) {
                if (!refinedSenses.containsKey(targetToken)) {
                    for (ISense targetSense : targetToken.getSenses()) {
                        //check whether each sense not synonym or more general, less general then the senses of
                        //the ancestors and descendants of the node in context hierarchy
                        if ((senseMatcher.isSourceSynonymTarget(sourceSense, targetSense)) ||
                                (senseMatcher.isSourceLessGeneralThanTarget(sourceSense, targetSense)) ||
                                (senseMatcher.isSourceMoreGeneralThanTarget(sourceSense, targetSense))) {
                            addToRefinedSenses(refinedSenses, sourceToken, sourceSense);
                            addToRefinedSenses(refinedSenses, targetToken, targetSense);
                        }
                    }
                }
            }
        }
    }

    private void addToRefinedSenses(HashMap<IToken, List<ISense>> refinedSenses, IToken token, ISense sense) {
        List<ISense> senses = refinedSenses.get(token);
        if (null == senses) {
            senses = new ArrayList<ISense>();
        }
        senses.add(sense);
        refinedSenses.put(token, senses);
    }
}