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

/**
 * Retains only specified kind of links in the mapping. Accepts relations kinds in a parameter retainRelations.
 * By default retains only equivalences (=). For other relation kinds see constants in
 * {@link it.unitn.disi.smatch.data.mappings.IMappingElement}. 
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RetainRelationsMappingFilter extends Configurable implements IMappingFilter {

    private static final Logger log = Logger.getLogger(RetainRelationsMappingFilter.class);

    private static final String RETAIN_RELATIONS_KEY = "retainRelations";
    private String retainRelations = "=";

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            if (newProperties.containsKey(RETAIN_RELATIONS_KEY)) {
                retainRelations = newProperties.getProperty(RETAIN_RELATIONS_KEY);
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

        //check each mapping
        for (IMappingElement e : mapping) {
            if (-1 < retainRelations.indexOf(e.getRelation())) {
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
