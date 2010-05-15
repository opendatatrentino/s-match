package it.unitn.disi.smatch.data.matrices;

import it.unitn.disi.smatch.data.mappings.IMappingElement;

import java.util.Arrays;

/**
 * A sparse implementation using compressed row storage (CRS) scheme.
 * Needs row-wise traversal and signaling the row end.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SparseMatrixChar implements IMatchMatrix {

    private int[] rowPointer = null;
    private int[] columnIndex = null;
    private char[] data = null;
    private char[] buffer = null;

    private int x = 0;
    private int y = 0;

    private int counter = 0;
    private int columnIndexCounter = 0;
    private int rowCounter = 0;

    public SparseMatrixChar() {
    }

    public void init(int x, int y) {
        init(x, y, 1000000);
    }

    public void init(int rows, int columns, int num_nz) {
        counter = num_nz;
        this.x = rows;
        this.y = columns;

        data = new char[counter];
        columnIndex = new int[counter];
        rowPointer = new int[x + 1];
        buffer = new char[columns];
        Arrays.fill(buffer, IMappingElement.IDK);
    }

    public char getElement(int x, int y) {
        return get(x, y);
    }

    public void setElement(int x, int y, char value) {
        setElement(y, value);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public SparseMatrixChar(int x, int y, char[][] in) {
        this.x = x;
        this.y = y;
        for (char[] chars : in) {
            for (char aShort : chars) {
                if (aShort != IMappingElement.IDK) {
                    counter++;
                }
            }
        }
        data = new char[counter];
        columnIndex = new int[counter];
        rowPointer = new int[x + 1];

        for (int i = 0; i < in.length; i++) {
            char[] chars = in[i];
            rowPointer[i] = columnIndexCounter;
            for (int j = 0; j < chars.length; j++) {
                char aShort = chars[j];
                if (aShort != IMappingElement.IDK) {
                    columnIndex[columnIndexCounter] = j;
                    data[columnIndexCounter] = aShort;
                    columnIndexCounter++;
                }
            }
        }
        rowPointer[in.length] = columnIndexCounter;
    }

    public SparseMatrixChar(int rows, int columns, int num_nz) {
        counter = num_nz;
        this.x = rows;
        this.y = columns;

        data = new char[counter];
        columnIndex = new int[counter];
        rowPointer = new int[x + 1];
        buffer = new char[columns];
        Arrays.fill(buffer, IMappingElement.IDK);
    }

    private void setElement(int pos, char data) {
        buffer[pos] = data;
    }

    public void endOfRow() {
        addRow(buffer);
        Arrays.fill(buffer, IMappingElement.IDK);
    }

    private void addRow(char[] chars) {
        rowPointer[rowCounter] = columnIndexCounter;
        for (int j = 0; j < chars.length; j++) {
            char aShort = chars[j];
            if (aShort != IMappingElement.IDK) {
                columnIndex[columnIndexCounter] = j;
                data[columnIndexCounter] = aShort;
                columnIndexCounter++;
            }
        }

        rowCounter++;
        rowPointer[rowCounter] = columnIndexCounter;
    }

    private char get(int row, int column) {
        int index = binarySearch(columnIndex,
                column, rowPointer[row], rowPointer[row + 1]);

        if (index >= 0) {
            return data[index];
        } else {
            return IMappingElement.IDK;
        }
    }

    private char[] getVector(int row) {
        char[] out = new char[y];
        Arrays.fill(out, IMappingElement.IDK);
        return getVector(row, out);
    }

    private char[] getVector(int row, char[] out) {
        for (int i = rowPointer[row]; i < rowPointer[row + 1]; i++) {
            out[columnIndex[i]] = data[i];
        }
        return out;
    }

    private static int binarySearch(int[] index, int key, int begin, int end) {
        end--;

        while (begin <= end) {
            int mid = (end + begin) >> 1;

            if (index[mid] < key)
                begin = mid + 1;
            else if (index[mid] > key)
                end = mid - 1;
            else
                return mid;
        }

        return -1;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < x; i++) {
            sb.append(Arrays.toString(getVector(i))).append("\n");
        }
        return sb.toString();
    }
}