package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.data.mappings.IMapping;

import java.util.Vector;

/**
 * An interface for mapping renderers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingRenderer {
    /**
     * 0th element of the Vector is filename to save mappings
     * 1st element is char[][] CnodMatrix
     * 2nd element is char[][] CLabMatrix
     * 3rd element is Context sourceContext
     * 4th element is Context targetContext
     * Other can be used for further parameters
     *
     * @param args
     */
    public IMapping render(Vector args);
}
