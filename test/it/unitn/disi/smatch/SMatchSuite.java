package it.unitn.disi.smatch;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SMatch tests.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 * @author Juan Pane pane@disi.unitn.it
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
	it.unitn.disi.smatch.matcher.structure.tree.spsm.SPSMTest.class
})

public class SMatchSuite {
}