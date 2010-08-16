package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data;

import java.util.List;

//TODO Juan code duplication with existing tree structures

/**
 * This interface specifies methods to retrieve the nodes of a tree in different
 * sequence orderings, i.e., in pre- or postorder.
 */
public interface ITreeSequenceAccessor <E> {

    /**
     * Returns the tree traversal ordering of the tree which is accessed by this
     * tree accessor in preorder.
     *
     * @return preorder tree traversal sequence
     */
    public List<Object> getPreorderSequence();

    /**
     * Returns the tree traversal ordering of the tree which is accessed by this
     * tree accessor in postorder.
     *
     * @return postorder tree traversal sequence
     */
    public List<Object> getPostorderSequence();

    /**
     * Returns a sequence of {@link java.lang.Object}. Will be overridden by
     * implementing class.
     *
     * @return sequence of <code>Object</code>
     */
    public List<E> getSequence();

    /**
     * Returns a string representation of the sequence wrapped by the sequence
     * accessor.
     *
     * @return string representing the sequence
     */
    public String toString();
}
