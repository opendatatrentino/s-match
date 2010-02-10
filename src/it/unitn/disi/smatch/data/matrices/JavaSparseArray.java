package it.unitn.disi.smatch.data.matrices;

import it.unitn.disi.smatch.MatchManager;

import java.util.Arrays;

/**
 * Implements a Java Sparse Array (see 10.1.1.13.7544.pdf).
 * To be used with minimal matchers, because CRS does work for them.
 * MM might walk a tree in "unpredictable" manner and does not signal end of row.
 * Does not have max density limit.
 * No boundary checks.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class JavaSparseArray implements IMatchMatrix {

    private int rows;
    private int cols;

    private char[][] value;
    //indexes of values. keep indexes sorted.
    private int[][] index;

    public void init(int x, int y) {
        rows = x;
        cols = y;
        value = new char[x][];
        index = new int[x][];
    }

    public void init(int x, int y, int num_nz) {
        init(x, y);
    }

    public void endOfRow() {
        //it is not needed.
    }

    public char getElement(int x, int y) {
        char result = MatchManager.IDK_RELATION;
        if (null != index[x]) {
            int idx = Arrays.binarySearch(index[x], y);
            if (-1 < idx) {
                result = value[x][idx];
            }
        }
        return result;
    }

    public void setElement(int x, int y, char aValue) {
        if (MatchManager.IDK_RELATION != aValue) {
            if (null != index[x]) {//row exists
                int idx = Arrays.binarySearch(index[x], y);
                if (-1 < idx) {//element exists
                    value[x][idx] = aValue;
                } else { //element does not exist
                    int[] newIndexRow = new int[index[x].length + 1];
                    char[] newValueRow = new char[index[x].length + 1];

                    //keep index sorted
                    int insertAt = -(idx + 1);
                    System.arraycopy(index[x], 0, newIndexRow, 0, insertAt);
                    newIndexRow[insertAt] = y;
                    System.arraycopy(value[x], 0, newValueRow, 0, insertAt);
                    newValueRow[insertAt] = aValue;
                    if (index[x].length > insertAt) {
                        System.arraycopy(index[x], insertAt, newIndexRow, insertAt + 1, index[x].length - insertAt);
                        System.arraycopy(value[x], insertAt, newValueRow, insertAt + 1, index[x].length - insertAt);
                    }

                    index[x] = newIndexRow;
                    value[x] = newValueRow;
                }
            } else {
                index[x] = new int[1];
                index[x][0] = y;
                value[x] = new char[1];
                value[x][0] = aValue;
            }
        } else {
            //check and remove it 
            if (null != index[x]) {//row exists
                int idx = Arrays.binarySearch(index[x], y);
                if (-1 < idx) {//element exists
                    if (1 < index[x].length) {
                        //remove element
                        int[] newIndexRow = new int[index[x].length - 1];
                        char[] newValueRow = new char[index[x].length - 1];
                        //before element
                        if (0 < idx) {
                            System.arraycopy(index[x], 0, newIndexRow, 0, idx);
                            System.arraycopy(value[x], 0, newValueRow, 0, idx);
                        }
                        //after element
                        if (idx < (index[x].length - 1)) {
                            System.arraycopy(index[x], idx + 1, newIndexRow, idx, index[x].length - 1 - idx);
                            System.arraycopy(value[x], idx + 1, newValueRow, idx, index[x].length - 1 - idx);
                        }
                        index[x] = newIndexRow;
                        value[x] = newValueRow;
                    } else {
                        if (1 == index[x].length) { 
                            //remove entire row
                            index[x] = null;
                            value[x] = null;
                        }
                    }
                }
            }
        }
    }

    public int getX() {
        return rows;
    }

    public int getY() {
        return cols;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.append(getElement(i, j)).append(" ");
            }
            result.append("\n");
        }
        result.append("\n");
        return result.toString();
    }
}