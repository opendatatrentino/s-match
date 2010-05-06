package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.Mapping;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.Random;

/**
 * Selects random sample. Accepts sample size parameter as sampleSize.
 * By default samples 100 links.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RandomSampleMappingFilter extends Configurable implements IMappingFilter {

    private static final Logger log = Logger.getLogger(RandomSampleMappingFilter.class);

    private static final String SAMPLE_SIZE_KEY = "sampleSize";
    private int sampleSize = 100;

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            if (newProperties.containsKey(SAMPLE_SIZE_KEY)) {
                sampleSize = Integer.parseInt(newProperties.getProperty(SAMPLE_SIZE_KEY));
            }
        }
        properties = newProperties;
    }

    public IMapping filter(IMapping mapping) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering started...");
        }
        long start = System.currentTimeMillis();

        IMapping result = new Mapping(mapping.getSourceContext(), mapping.getTargetContext());

        long counter = 0;
        long total = mapping.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //sampling
        int oneIn = (mapping.size() / sampleSize) - (mapping.size() / (10 * sampleSize));
        Random r = new Random();
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Sampling...");
        }
        for (IMappingElement e : mapping) {
            if (0 == r.nextInt(oneIn) && result.size() < sampleSize) {
                result.add(e);
            }

            counter++;
            if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                log.info(100 * counter / total + "%");
            }
        }

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering finished: " + (System.currentTimeMillis() - start) + " ms");
        }
        return result;
    }
}
