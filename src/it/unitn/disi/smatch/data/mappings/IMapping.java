package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.IContext;

import java.util.Set;

/**
 * Interface for mappings.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMapping extends Set<IMappingElement> {

    IContext getSourceContext();

    IContext getTargetContext();

    void setSourceContext(IContext newContext);

    void setTargetContext(IContext newContext);
}
