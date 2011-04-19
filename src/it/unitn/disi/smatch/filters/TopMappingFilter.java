package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Removes links having a root node on the one or the other side from the mapping.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TopMappingFilter extends BaseFilter implements IMappingFilter {

    private static final Logger log = Logger.getLogger(TopMappingFilter.class);

    public IContextMapping<INode> filter(IContextMapping<INode> mapping) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering started...");
        }
        long start = System.currentTimeMillis();

        IContextMapping<INode> result = mappingFactory.getContextMappingInstance(mapping.getSourceContext(), mapping.getTargetContext());

        long counter = 0;
        long total = mapping.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //sampling
        for (IMappingElement<INode> e : mapping) {
            if (e.getSource().hasParent() && e.getTarget().hasParent()) {
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