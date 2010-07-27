package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;

import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;


public class TreeNodeTuple extends GenericTuple {

    /**
     * Constructor.
     * <p/>
     * Constructs an object tuple which consists of two <code>ITreeNode</code>.
     *
     * @param left  the left ITreeNode
     * @param right the right ITreeNode
     * @see ITreeNode
     */
    public TreeNodeTuple(ITreeNode left, ITreeNode right) {
        super(left, right);
    }

    public ITreeNode getLeft() {
        return (ITreeNode) super.getLeft();
    }

    public ITreeNode getRight() {
        return (ITreeNode) super.getRight();
    }
}
