package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.Mapping;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Retains only EQ links in the mapping.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RetainEQFilter implements IFilter {

    private static final Logger log = Logger.getLogger(RetainEQFilter.class);

    public IMapping filter(IMapping mapping) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering started...");
        }
        long start = System.currentTimeMillis();

        IMapping result = new Mapping(mapping.getSourceContext(), mapping.getTargetContext());

        long counter = 0;
        long total = mapping.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //check each mapping
        for (IMappingElement e : mapping) {
            if (MatchManager.SYNOMYM == e.getRelation()) {
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
