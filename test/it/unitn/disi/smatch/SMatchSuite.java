package it.unitn.disi.smatch;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SMatch tests.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 * @author Juan Pane pane@disi.unitn.it
 */

@RunWith(Suite.class)

@Suite.SuiteClasses({
	it.unitn.disi.smatch.matcher.structure.tree.spsm.SPSMTest.class
})

public class SMatchSuite {
}