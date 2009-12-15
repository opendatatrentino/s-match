package it.unitn.disi.smatch.data;

import it.unitn.disi.smatch.data.matrices.SparseMatrixChar;
import org.junit.Test;

/**
 * Tests for SparseMatrixChar.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TestSparseMatrixChar extends TestMatrix {

    @Test
    public void testMatrix() {
        SparseMatrixChar sm = new SparseMatrixChar();
        testMatrix(sm);
    }

    @Test
    public void testRow1() {
        SparseMatrixChar sm = new SparseMatrixChar();
        testRow1(sm);
    }

    @Test
    public void testRow2() {
        SparseMatrixChar sm = new SparseMatrixChar();
        testRow2(sm);
    }

    @Test
    public void testMtxByRow() {
        SparseMatrixChar sm = new SparseMatrixChar();
        testMtx("../data/tests/matrices/bcsstk32.sorted.mtx.gz", sm);
    }


    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("it.unitn.disi.smatch.data.TestSparseMatrixChar");
    }
}