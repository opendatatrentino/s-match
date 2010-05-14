package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.matchers.structure.node.INodeMatcher;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * Base class for tree matchers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class BaseTreeMatcher extends Configurable {

    private static final Logger log = Logger.getLogger(BaseTreeMatcher.class);

    private static final String NODE_MATCHER_KEY = "NodeMatcher";
    protected INodeMatcher nodeMatcher = null;

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            nodeMatcher = (INodeMatcher) configureComponent(nodeMatcher, properties, newProperties, "node matcher", NODE_MATCHER_KEY, INodeMatcher.class);

            properties.clear();
            properties.putAll(newProperties);
        }
    }
}