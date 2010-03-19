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

	/**
     * Filters the semantic relation.
     *
     * @param args a vector which have context of source, target and matrix of relation between labels. nodes
     * @return minimal semantic relation
     */
	// TODO untyped variable in vector
    public IMatchMatrix filter(Vector args);
}
