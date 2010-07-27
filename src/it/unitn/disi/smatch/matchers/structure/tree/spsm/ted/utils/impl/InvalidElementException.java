package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl;

/**
 * This generic exception is thrown if an invalid element is accessed. What
 * makes an element invalid is case-specific.
 */
public class InvalidElementException extends Exception {

    private static final long serialVersionUID = -4532977213617532680L;

    /**
     * Constructor.
     */
    public InvalidElementException() {
        super("Element is invalid");
    }

    /**
     * Constructor.
     * <p/>
     * Takes a message to be displayed when exception is thrown.
     *
     * @param message displayed message
     */
    public InvalidElementException(String message) {
        super(message);
    }
}
