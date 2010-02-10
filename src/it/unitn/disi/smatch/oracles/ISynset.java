package it.unitn.disi.smatch.oracles;

import java.util.Vector;

/**
 * Interface for synsets.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISynset {

    /**
     * Returns a synset gloss, that is a textual description of the meaning of the synset.
     *
     * @return a gloss
     */
    String getGloss();

    /**
     * Get lemmas of this synset.
     *
     * @return lemmas
     */
    Vector<String> getLemmas();

    /**
     * Returns "parents", that is hypernyms of the synset.
     *
     * @return hypernyms of the synset
     */
    Vector<ISynset> getParents();

    /**
     * Returns "parents", that is hypernyms of the synset, up to certain depth.
     *
     * @param depth a search depth
     * @return "parents"
     */
    Vector<ISynset> getParents(int depth);

    /**
     * Returns "children", that is hyponyms of the synset.
     *
     * @return "children"
     */
    Vector<ISynset> getChildren();

    /**
     * Returns "children", that is hyponyms of the synset, down to certain depth.
     *
     * @param depth a search depth
     * @return "children"
     */
    Vector<ISynset> getChildren(int depth);

    //TODO refactor\remove?
    /**
     * Checks whether the synset holds a synset inside.
     *
     * @return whether the synset holds a synset inside
     */
    boolean isNull();
}
