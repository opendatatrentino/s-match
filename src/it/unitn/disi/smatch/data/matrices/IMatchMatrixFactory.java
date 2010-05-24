package it.unitn.disi.smatch.data.matrices;

import it.unitn.disi.smatch.components.IConfigurable;

/**
 * Produces matching matrices.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatchMatrixFactory extends IConfigurable {

    IMatchMatrix getInstance();
    
}
