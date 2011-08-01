package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMappingFactory;
import it.unitn.disi.smatch.matchers.structure.node.INodeMatcher;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Base class for tree matchers. Needs the following configuration parameters"
 * <p/>
 * nodeMatcher string parameter which should point to a class implementing a
 * {@link it.unitn.disi.smatch.matchers.structure.node.INodeMatcher} interface.
 * <p/>
 * mappingFactory string parameter with a class implementing
 * {@link it.unitn.disi.smatch.data.mappings.IMappingFactory} interface.
 *
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public class BaseTreeMatcher extends Configurable {

    private static final Logger log = Logger.getLogger(BaseTreeMatcher.class);

    private static final String NODE_MATCHER_KEY = "nodeMatcher";
    protected INodeMatcher nodeMatcher = null;

    private static final String MAPPING_FACTORY_KEY = "mappingFactory";
    protected IMappingFactory mappingFactory = null;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(NODE_MATCHER_KEY)) {
                nodeMatcher = (INodeMatcher) configureComponent(nodeMatcher, oldProperties, newProperties, "node matcher", NODE_MATCHER_KEY, INodeMatcher.class);
            } else {
                final String errMessage = "Cannot find configuration key " + NODE_MATCHER_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

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