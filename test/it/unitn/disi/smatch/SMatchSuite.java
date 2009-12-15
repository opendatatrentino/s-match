package it.unitn.disi.smatch;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SMatch tests.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
        it.unitn.disi.smatch.data.TestSparseMatrixChar.class,
        it.unitn.disi.smatch.data.TestJavaSparseArray.class
})

public class SMatchSuite {
}