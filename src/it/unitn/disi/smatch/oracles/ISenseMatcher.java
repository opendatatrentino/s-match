package it.unitn.disi.smatch.oracles;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;

import java.util.Vector;

/**
 * An interface to sense matchers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISenseMatcher extends IConfigurable {

    /**
     * Returns semantic relation holding between two sets of senses.
     *
     * @param listSenseS source set of senses
     * @param listSenseT target set of senses
     * @return a relation
     */
    public char getRelation(Vector<String> listSenseS, Vector<String> listSenseT);

    /**
     * Returns semantic relations which holds between two ACoLs.
     *
     * @param source source ACoL
     * @param target target ACoL
     * @return a relation
     */
    public char getRelationACoL(IAtomicConceptOfLabel source, IAtomicConceptOfLabel target);

    /**
     * Checks whether the source is more general than target.
     *
     * @param source source synset id
     * @param target target synset id
     * @return whether relation holds
     */
    public boolean isSourceMoreGeneralThanTarget(String source, String target);

    /**
     * Checks whether the source is less general than target.
     *
     * @param source source synset id
     * @param target target synset id
     * @return whether relation holds
     */
    public boolean isSourceLessGeneralThanTarget(String source, String target);

    /**
     * Checks whether the source is a synonym of the target.
     *
     * @param source source synset id
     * @param target target synset id
     * @return whether relation holds
     */
    public boolean isSourceSynonymTarget(String source, String target);

    /**
     * Checks whether the source is disjoint with the target.
     *
     * @param source source synset id
     * @param target target synset id
     * @return whether relation holds
     */
    public boolean isSourceOppositeToTarget(String source, String target);
}
