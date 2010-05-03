package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.mappings.IMapping;

/**
 * Does nothing.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */

public class ZeroFilter implements IFilter {

    public IMapping filter(IMapping mapping) {
        return mapping;
    }
}
