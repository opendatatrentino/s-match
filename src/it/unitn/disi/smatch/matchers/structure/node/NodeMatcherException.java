package it.unitn.disi.smatch.matchers.structure.node;

import it.unitn.disi.smatch.matchers.structure.tree.TreeMatcherException;

/**
 * Exception for Node Matchers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class NodeMatcherException extends TreeMatcherException {

    public NodeMatcherException(String errorDescription) {
        super(errorDescription);
    }

    public NodeMatcherException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
