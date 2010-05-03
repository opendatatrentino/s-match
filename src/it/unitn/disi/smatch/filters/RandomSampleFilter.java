package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.Mapping;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Selects random sample.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RandomSampleFilter implements IFilter {

    private static final Logger log = Logger.getLogger(RandomSampleFilter.class);

    private static final int SAMPLE_SIZE = 200;

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
        int oneIn = (mapping.size() / SAMPLE_SIZE) - (mapping.size() / (10 * SAMPLE_SIZE));
        Random r = new Random();
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Sampling...");
        }
        for (IMappingElement e : mapping) {
            if (0 == r.nextInt(oneIn) && result.size() < SAMPLE_SIZE) {
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
