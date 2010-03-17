package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

import java.util.Vector;

/**
 * Does nothing.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
//TODO commenting needed. This class is assigned for filter component. but here in class description does nothing.
public class ZeroFilter implements IFilter {

    public IMatchMatrix filter(Vector args) {
        return (IMatchMatrix) args.get(1);
    }
}
