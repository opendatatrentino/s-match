package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

import java.util.Properties;

/**
 * Default mapping implementation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ContextMapping<T> extends Mapping<T> implements IContextMapping<T>, IMappingFactory {

    private IContext sourceContext;
    private IContext targetContext;

    protected Properties properties;

    public ContextMapping() {
    }

    public ContextMapping(IContext sourceContext, IContext targetContext) {
        this.sourceContext = sourceContext;
        this.targetContext = targetContext;
        properties = new Properties();
    }

    public ContextMapping(Properties properties) {
        super();
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = !newProperties.equals(properties);
        if (result) {
            properties.clear();
            properties.putAll(newProperties);
        }

        return result;
    }

    public boolean setProperties(String fileName) throws ConfigurableException {
        return setProperties(Configurable.loadProperties(fileName));
    }

    public IContext getSourceContext() {
        return sourceContext;
    }

    public IContext getTargetContext() {
        return targetContext;
    }

    public void setSourceContext(IContext newContext) {
        sourceContext = newContext;
    }

    public void setTargetContext(IContext newContext) {
        targetContext = newContext;
    }

    public IContextMapping<INode> getContextMappingInstance(IContext source, IContext target) {
        return new ContextMapping<INode>(source, target);
    }

    public IContextMapping<IAtomicConceptOfLabel> getACoLMappingInstance(IContext source, IContext target) {
        return new ContextMapping<IAtomicConceptOfLabel>(source, target);
    }
}