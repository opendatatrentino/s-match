package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.mappings.IMapping;

/**
 * Interface for mapping filters.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IFilter {

    /**
     * Filters the mapping.
     *
     * @param mapping source mapping
     * @return filtered mapping
     */
    public IMapping filter(IMapping mapping);
}
