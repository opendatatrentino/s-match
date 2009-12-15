package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

import java.util.Vector;

/**
 * Does nothing.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ZeroFilter implements IFilter {

    public IMatchMatrix filter(Vector args) {
        return (IMatchMatrix) args.get(1);
    }
}
