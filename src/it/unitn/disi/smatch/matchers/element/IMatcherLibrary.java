package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.mappings.IContextMapping;

/**
 * Interface for collections of matchers, which perform element-level matching.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatcherLibrary extends IConfigurable {

    /**
     * Performs Step 3 of semantic matching algorithm.
     *
     * @param sourceContext interface of source context
     * @param targetContext interface of target context
     * @return mapping between atomic concepts in both contexts
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public IContextMapping<IAtomicConceptOfLabel> elementLevelMatching(IContext sourceContext, IContext targetContext) throws MatcherLibraryException;
}
