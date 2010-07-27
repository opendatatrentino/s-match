package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data;


import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

/**
 * This interface defines the methods of a tree node.
 * <p/>
 * Note that such a tree node is meant to be a simple node and the root of a
 * tree at the same time (therefore "mutable").
 *
 */
public interface ITreeNode extends MutableTreeNode, TreeNode, Cloneable {

    /**
     * Returns the postorder sequence of the tree starting at this tree node.
     *
     * @return postorder enumeration
     */
    public Enumeration postorderEnumeration();

    /**
     * Returns the preorder sequence of the tree starting at this tree node.
     *
     * @return postorder enumeration
     */
    public Enumeration preorderEnumeration();

    /**
     * Returns the root of the tree of this tree node.
     *
     * @return root of the tree
     */
    public ITreeNode getRoot();

    /**
     * Returns the first child of this tree node.
     *
     * @return first child
     */
    public ITreeNode getFirstChild();

    /**
     * Returns the child in this tree node's child array that immediately
     * follows v1, which must be a child of this tree node.
     *
     * @param v1 child node of this tree node
     * @return child node immediately following v1
     */
    public ITreeNode getChildAfter(ITreeNode v1);

    /**
     * Return the user object which is stored in this tree node.
     *
     * @return user object
     */
    public Object getUserObject();

    /**
     * Returns a shallow copy of this tree node.
     *
     * @return copy of this tree node
     */
    public Object clone();

    /**
     * Removes clone from its parent and makes it a child of this tree node by
     * adding it to the end of this tree node's child array.
     *
     * @param clone tree node
     */
    public void add(ITreeNode clone);

    /**
     * Checks if this tree node is a tree root, i.e., has no parents.
     *
     * @return true if this tree node is a tree root, false otherwise
     */
    public boolean isRoot();

    /**
     * Return the last child in this tree node's child array.
     *
     * @return last child
     */
    public ITreeNode getLastChild();

    /**
     * Returns this tree node's previous sibling, i.e., sibling immediately to
     * the left (with same tree depth level).
     *
     * @return previous sibling of this tree node
     */
    public ITreeNode getPreviousSibling();
}
