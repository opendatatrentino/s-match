package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.mappings.IMapping;

/**
 * Interface for mapping filters.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingFilter extends IConfigurable {

    /**
     * Filters the mapping.
     *
     * @param mapping source mapping
     * @return filtered mapping
     * @throws MappingFilterException MappingFilterException
     */
    IMapping filter(IMapping mapping) throws MappingFilterException;
}