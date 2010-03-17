package it.unitn.disi.smatch.data.matrices;

import it.unitn.disi.smatch.MatchManager;

import java.util.Arrays;

/**
 * Default matrix for matching results.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatchMatrix implements IMatchMatrix {

    private int x = 0;
    private int y = 0;
    private char[][] matrix = null;

    public void init(int x, int y) {
        matrix = new char[x][y];
        this.x = x;
        this.y = y;

        for (char[] row : matrix) {
            Arrays.fill(row, MatchManager.IDK_RELATION);
        }
    }

    public void init(int x, int y, int num_nz) {
        init(x, y);
    }

    public void endOfRow() {
    }

    public char getElement(int x, int y) {
        return matrix[x][y];
    }

    public void setElement(int x, int y, char value) {
        matrix[x][y] = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                result.append(matrix[i][j]).append(" ");
            }
            result.append("\n");
        }
        result.append("\n");
        return result.toString();
    }
}