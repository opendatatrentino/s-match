package it.unitn.disi.smatch.data.matrices;

/**
 * Objects which could be referenced by index. Used to store relations in the matrices.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IIndexedObject {
    /**
     * Gets the index of the node in the matrix.
     *
     * @return the index of the node in the matrix.
     */
    int getIndex();

    /**
     * Sets the index of the node in the matrix.
     *
     * @param index index
     */
    void setIndex(int index);
}
