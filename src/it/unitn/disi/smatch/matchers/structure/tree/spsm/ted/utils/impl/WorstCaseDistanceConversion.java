package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl;

import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.IDistanceConversion;

/**
 * This class implements a variation of the common distance conversion, such
 * that it takes into account a worst case distance between two entities
 * (strings, sequences, etc).
 */
public class WorstCaseDistanceConversion implements IDistanceConversion {

    private double worstCaseDistance;

    /**
     * Constructor.
     */
    public WorstCaseDistanceConversion() {
    }

    /**
     * Constructor.
     * <p/>
     * Takes the worst case distance between two entities (strings, set, etc.)
     * to be used to convert the distance between the entities to a similarity
     * value.
     *
     * @param worstCaseDistance worst case distance used to convert distance to similarity
     */
    public WorstCaseDistanceConversion(double worstCaseDistance) {
        super();
        this.worstCaseDistance = worstCaseDistance;
    }

    public double convert(double d) {
        return (worstCaseDistance - d) / worstCaseDistance;
    }

    /**
     * @return Returns the worstCaseDistance.
     */
    public double getWorstCaseDistance() {
        return worstCaseDistance;
    }

    /**
     * @param worstCaseDistance The worstCaseDistance to set.
     */
    public void setWorstCaseDistance(double worstCaseDistance) {
        this.worstCaseDistance = worstCaseDistance;
    }

}
