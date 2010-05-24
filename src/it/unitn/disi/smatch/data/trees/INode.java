package it.unitn.disi.smatch.data.trees;

import it.unitn.disi.smatch.data.matrices.IIndexedObject;

import javax.swing.tree.MutableTreeNode;
import java.util.Iterator;

/**
 * An interface to a node.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface INode extends MutableTreeNode, IIndexedObject {

    /**
     * Returns the child node at index childIndex.
     */
    INode getChildAt(int childIndex);

    /**
     * Returns the number of children INodes.
     */
    int getChildCount();

    /**
     * Returns the index of node in the receivers children. If the receiver does not contain node, -1 will be
     * returned.
     *
     * @param child a node to search for
     * @return the index of node in the receivers children
     */
    int getChildIndex(INode child);

    /**
     * Returns the iterator over the children of the receiver.
     *
     * @return the iterator over the children of the receiver
     */
    Iterator<INode> getChildren();

    /**
     * Creates a child to the given node as the last child.
     *
     * @return a newly created child
     */
    INode createChild();

    /**
     * Creates a child with a name to the given node as the last child.
     *
     * @param name a name for a new child
     * @return a newly created child
     */
    INode createChild(String name);

    /**
     * Adds a child to the given node as the last child.
     *
     * @param child node to add
     */
    void addChild(INode child);

    /**
     * Adds child to the receiver at index.
     *
     * @param index index where the child will be added
     * @param child node to add
     */
    void addChild(int index, INode child);

    /**
     * Removes the child at index from the receiver.
     *
     * @param index index of a child to remove
     */
    void removeChild(int index);

    /**
     * Removes node from the receiver.
     *
     * @param node child to remove
     */
    void removeChild(INode node);

    /**
     * Returns the parent of the receiver.
     */
    INode getParent();

    /**
     * Sets the parent of the receiver to newParent.
     *
     * @param newParent new parent
     */
    void setParent(INode newParent);

    /**
     * Returns true if the receiver has a parent and false otherwise.
     *
     * @return true if the receiver has a parent and false otherwise
     */
    boolean hasParent();

    /**
     * Removes the subtree rooted at this node from the tree, giving this node a null parent.
     * Does nothing if this node is the root of its tree.
     */
    void removeFromParent();

    /**
     * Returns true if the receiver is a leaf.
     *
     * @return true if the receiver is a leaf
     */
    boolean isLeaf();

    /**
     * Returns the count of ancestor nodes.
     *
     * @return the count of ancestor nodes
     */
    int getAncestorCount();

    /**
     * Returns ancestors of the receiver. The returned list is ordered from the parent node to the root.
     *
     * @return ancestors of the receiver
     */
    Iterator<INode> getAncestors();

    /**
     * Returns ancestors of the receiver including receiver. The returned list is ordered from the parent node
     * to the root.
     *
     * @return ancestors of the receiver including receiver
     */
    Iterator<INode> getSupertree();

    /**
     * Returns the number of levels above this node -- the distance from
     * the root to this node.  If this node is the root, returns 0.
     *
     * @return the number of levels above this node
     */
    int getLevel();

    /**
     * Returns the count of descendant nodes.
     *
     * @return the count of descendant nodes
     */
    int getDescendantCount();

    /**
     * Returns descendants of the receiver. The descendants are ordered breadth first.
     *
     * @return descendants of the receiver
     */
    Iterator<INode> getDescendants();

    /**
     * Returns descendants of the receiver including receiver. The descendants are ordered breadth first.
     *
     * @return descendants of the receiver including receiver
     */
    Iterator<INode> getSubtree();

    /**
     * Returns interface to the node metadata.
     *
     * @return interface to the node metadata
     */
    public INodeData getNodeData();
}
