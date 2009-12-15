package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.oracles.ISynset;

/**
 * An interface for sense and gloss based element level matchers.
 * s *
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISenseGlossBasedElementLevelSemanticMatcher {

    /**
     * Returns a relation between source and target synsets.
     *
     * @param source source synset
     * @param target target synset.
     * @return a relation
     */
    char match(ISynset source, ISynset target);
}
