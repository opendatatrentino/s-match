package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;

/**
 * Default mapping implementation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ContextMapping<T> extends Mapping<T> implements IContextMapping<T> {

    private IContext sourceContext;
    private IContext targetContext;

    public ContextMapping(IContext sourceContext, IContext targetContext) {
        this.sourceContext = sourceContext;
        this.targetContext = targetContext;
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
}