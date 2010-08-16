package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * Does nothing.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ZeroFilter extends Configurable implements IMappingFilter {
    
    public IContextMapping<INode> filter(IContextMapping<INode> mapping) throws MappingFilterException {
        return mapping;
    }
}
