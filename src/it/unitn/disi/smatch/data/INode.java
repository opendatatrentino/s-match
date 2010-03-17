package it.unitn.disi.smatch.data;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Vector;

/**
 * The interface to data structure of node.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface INode extends MutableTreeNode {

    /**
     * Adds child to the given node.
     *
     * @param child node to add
     */
    void addChild(INode child);

    /**
     * Returns all ancestors of the given node.
     * The returned list is ordered from the father node to the root.
     *
     * @return list of ancestors
     */
    Vector<INode> getAncestors();

    /**
     * Returns all descendants of the node.
     * The returned list is order as in depth first traversal.
     *
     * @return list of descendants
     */
    Vector<INode> getDescendants();

    /**
     * Returns count of descendant nodes, including itself.
     *
     * @return the number of descendant nodes
     */
    int getDescendantCount();

    /**
     * Returns true if the node is a root in the context and false otherwise.
     *
     * @return true if node is a root
     */
    boolean isRoot();

    /**
     * Returns interface to the parent node.
     */
    INode getParent();

    /**
     * Returns id of a node.
     */
    String getNodeId();

    /**
     * Returns label of node.
     */
    String getNodeName();

    /**
     * Returns children of the node.
     *
     * @return list of children
     */
    Vector<INode> getChildren();

    /**
     * Removes child of the node.
     *
     * @param child the child node which need to be removed
     */
    void removeChild(INode child);

    /**
     * Returns interface to node of metadata.
     */
    public INodeData getNodeData();
}
