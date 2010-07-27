package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;

/**
 * This class implementes a tuple, i.e., a pair of objects. One object occurs on
 * the left and the other object on the right.
 */
public class GenericTuple {

    private Object left;

    private Object right;

    /**
     * Constructor.
     * <p/>
     * Takes the left and the right object of this object tuple.
     *
     * @param left  the left object
     * @param right the right object
     */
    public GenericTuple(Object left, Object right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Compares the instance to a given object
     *
     * @param t the tuple to be compared to this tuple
     * @return true if the left and right objects match, false otherwise
     */
    public boolean equals(GenericTuple t) {
        if (t == null)
            return false;
        return left.equals(t.getLeft()) && right.equals(t.getRight());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getLeft().toString() + ":" + getRight().toString();
    }

    /**
     * Return the left object.
     *
     * @return left object.
     */
    public Object getLeft() {
        return left;
    }

    /**
     * Return the right object.
     *
     * @return right object
     */
    public Object getRight() {
        return right;
    }
}
