package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;


import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeNode extends DefaultMutableTreeNode implements ITreeNode {

    private static final long serialVersionUID = -5366774557189363700L;

    /**
     * Constructor.
     */
    public TreeNode() {
        super();
    }

    /**
     * Constructor.
     * <p/>
     * The userObject to be stored in the tree node is passed.
     *
     * @param userObject a user-specified object to be stored in the tree node
     */
    public TreeNode(Object userObject) {
        super(userObject);
    }

    /**
     * Constructor.
     * <p/>
     * The userObject to be stored in the tree node is passed. Additionally, the
     * boolean flag allowsChildren specifies if this tree node is allowed to
     * have children.
     *
     * @param userObject     a user-specified object to be stored in the tree node
     * @param allowsChildren if true, this tree node is allowed to have children,
     *                       otherwise, no children must be added
     */
    public TreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public ITreeNode getRoot() {
        return (ITreeNode) super.getRoot();
    }

    public ITreeNode getFirstChild() {
        return (ITreeNode) super.getFirstChild();
    }

    public ITreeNode getChildAfter(ITreeNode v1) {
        return (ITreeNode) super.getChildAfter(v1);
    }

    public void add(ITreeNode clone) {
        super.add(clone);
    }

    public ITreeNode getLastChild() {
        return (ITreeNode) super.getLastChild();
    }

    public TreeNode getPreviousSibling() {
        return (TreeNode) super.getPreviousSibling();
    }

    public String toString() {
        if (userObject != null) {
            return userObject.toString();
        } else {
            return "";
        }
    }
}
