package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * Intersects the mapping passed as a parameter with the mapping specified in the configuration.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SetIntersectFilter extends BaseMappingBasedFilter {

    public IContextMapping<INode> filter(IContextMapping<INode> mapping) throws MappingFilterException {
        super.filter(mapping);

        mapping.retainAll(filterMapping);

        return mapping;
    }

}
