package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

import java.util.Vector;

/**
 * Interface for result filters.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IFilter {
    public IMatchMatrix filter(Vector args);
}
