package it.unitn.disi.smatch.data;

import java.util.Vector;

/**
 * Context from a matching perspective.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatchingContext {

    Vector<IAtomicConceptOfLabel> getAllContextACoLs();

    /**
     * clear all data acquired in linguistic preprocessing phase
     */
    void resetOldPreprocessing();

}
