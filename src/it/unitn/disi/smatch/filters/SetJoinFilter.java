package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * Joins the mapping passed as a parameter with the mapping specified in the configuration. Needs the
 * following configuration parameters:
 * <p/>
 * mappingLoader - an instance of IMappingLoader
 * <p/>
 * mapping - location of the mapping
 * <p/>
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SetJoinFilter extends BaseMappingBasedFilter {

    public IContextMapping<INode> filter(IContextMapping<INode> mapping) throws MappingFilterException {
        super.filter(mapping);

        mapping.addAll(filterMapping);

        return mapping;
    }

}