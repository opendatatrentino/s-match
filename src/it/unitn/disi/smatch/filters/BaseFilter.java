package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMappingFactory;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Base class for filters. Needs a configuration key mappingFactory with a class implementing
 * {@link it.unitn.disi.smatch.data.mappings.IMappingFactory} to produce mapping instances.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class BaseFilter extends Configurable implements IMappingFilter {

    private static final Logger log = Logger.getLogger(BaseFilter.class);

    private static final String MAPPING_FACTORY_KEY = "mappingFactory";
    protected IMappingFactory mappingFactory = null;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(MAPPING_FACTORY_KEY)) {
                mappingFactory = (IMappingFactory) configureComponent(mappingFactory, oldProperties, newProperties, "mapping factory", MAPPING_FACTORY_KEY, IMappingFactory.class);
            } else {
                final String errMessage = "Cannot find configuration key " + MAPPING_FACTORY_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }
}
