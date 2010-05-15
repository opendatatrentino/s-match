package it.unitn.disi.smatch.data;

import java.util.List;

/**
 * Context from a matching perspective.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatchingContext {

    List<IAtomicConceptOfLabel> getAllContextACoLs();

    /**
     * Clears all data acquired in linguistic preprocessing phase.
     */
    void resetOldPreprocessing();
}