package it.unitn.disi.smatch.data;

import it.unitn.disi.smatch.data.matrices.JavaSparseArray;
import org.junit.Test;

/**
 * Tests for JavaSparseArray.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TestJavaSparseArray extends TestMatrix {

    @Test
    public void testMatrix() {
        JavaSparseArray sm = new JavaSparseArray();
        testMatrix(sm);
    }

    @Test
    public void testReverseMatrix() {
        JavaSparseArray sm = new JavaSparseArray();
        testReverseMatrix(sm);
    }

    @Test
    public void testRow1() {
        JavaSparseArray sm = new JavaSparseArray();
        testRow1(sm);
    }

    @Test
    public void testRow2() {
        JavaSparseArray sm = new JavaSparseArray();
        testRow2(sm);
    }

    @Test
    public void testMtxUnordered() {
        JavaSparseArray sm = new JavaSparseArray();
        testMtx("../data/tests/matrices/bcsstk32.mtx.gz", sm);
    }

    @Test
    public void testMtxByRow() {
        JavaSparseArray sm = new JavaSparseArray();
        testMtx("../data/tests/matrices/bcsstk32.sorted.mtx.gz", sm);
    }

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("it.unitn.disi.smatch.data.TestJavaSparseArray");
    }
}