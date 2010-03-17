package it.unitn.disi.smatch.data.matrices;

/**
 * An interface to a matrix with matching results.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatchMatrix {

    /**
     * Initializes a matrix x rows per y columns.
     *
     * @param x rows count
     * @param y column count
     */
    void init(int x, int y);

    /**
     * Initializes a matrix x rows per y columns with a max of num_nz non-zero elements.
     *
     * @param x      rows count
     * @param y      column count
     * @param num_nz amount of non-zero elements
     */
    void init(int x, int y, int num_nz);

    /**
     * Signals end of row for a CRS scheme.
     */
    void endOfRow();

    /**
     * Returns an element form a matrix.
     *
     * @param x row
     * @param y column
     * @return an element value
     */
    char getElement(int x, int y);

    /**
     * Sets an element to a matrix.
     *
     * @param x     row
     * @param y     column
     * @param value a new element value
     */
    void setElement(int x, int y, char value);

    /**
     * Returns row count.
     *
     * @return number of row
     */
    int getX();

    /**
     * Returns column count.
     *
     * @return number of column
     */
    int getY();
}