package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;

/**
 * Implements a tuple (a pair of objects). One object occurs on
 * the left and the other object on the right.
 */
public class GenericTuple<E> {

    private E left;

    private E right;

    /**
     * Constructor.
     * <p/>
     * Takes the left and the right object of this object tuple.
     *
     * @param left  the left object
     * @param right the right object
     */
    public GenericTuple(E left, E right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericTuple)) return false;

        GenericTuple that = (GenericTuple) o;

        if (left != null ? !left.equals(that.left) : that.left != null) return false;
        if (right != null ? !right.equals(that.right) : that.right != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    public String toString() {
        return getLeft().toString() + ":" + getRight().toString();
    }

    /**
     * Return the left object.
     *
     * @return left object.
     */
    public E getLeft() {
        return left;
    }

    /**
     * Return the right object.
     *
     * @return right object
     */
    public E getRight() {
        return right;
    }
}
