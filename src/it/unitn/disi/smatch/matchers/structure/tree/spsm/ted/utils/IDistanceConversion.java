package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils;

/**
 * This is the general interface for a distance-to-similarity conversion
 * functions.
 */
public interface IDistanceConversion {
    /**
     * Returns the convert distance value, i.e., returns a similarity value.
     *
     * @param distance distance
     * @return convert distance (dissimilarity) value
     */
    public double convert(double distance);
}
