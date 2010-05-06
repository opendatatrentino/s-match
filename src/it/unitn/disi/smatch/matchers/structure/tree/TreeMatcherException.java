package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Tree Matchers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TreeMatcherException extends SMatchException {

    public TreeMatcherException(String errorDescription) {
        super(errorDescription);
    }

    public TreeMatcherException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
