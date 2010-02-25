package it.unitn.disi.smatch.data;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Vector;

/**
 * The interface to Node datastructure.
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
     * The returned list is orderd from the father node to the root.
     *
     * @return ancestor list
     */
    Vector<INode> getAncestors();

    /**
     * Returns all descendants of the node.
     * The returned list is order as in depth first traversal.
     *
     * @return descendants
     */
    Vector<INode> getDescendants();

    /**
     * Returns count of descendant nodes, including itself.
     * @return count of descendant nodes, including itself
     */
    int getDescendantCount();

    /**
     * Retunrs true if the node is a root in the context and false otherwise.
     *
     * @return true if node is a root
     */
    boolean isRoot();

    /**
     * Returns interface to the parent node.
     *
     * @return interface to the parent node
     */
    INode getParent();

    /**
     * Retunrs node id.
     *
     * @return node id
     */
    String getNodeId();

    /**
     * Returns node label.
     *
     * @return node label
     */
    String getNodeName();

    /**
     * Returns children of the node.
     *
     * @return node children
     */
    Vector<INode> getChildren();

    /**
     * Removes child of the node.
     *
     * @param child child node to remove
     */
    void removeChild(INode child);

    /**
     * Returns interface to node metadata.
     *
     * @return interface to node metadata
     */
    public INodeData getNodeData();
}
