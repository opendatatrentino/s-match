package it.unitn.disi.smatch.data.trees;

import java.util.Iterator;
import java.util.List;

/**
 * An interface for the context data structure. A context is basically a tree made of nodes with natural language
 * labels, organized into a hierarchy with mostly (assumed) subsumption and is-a relations between the nodes.
 *
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public interface IContext {

    /**
     * Sets a new root for the context.
     *
     * @param root a new root
     */
    void setRoot(INode root);

    /**
     * Returns the root of the context.
     *
     * @return the root of the context
     */
    INode getRoot();

    /**
     * Returns true if the context has a root node.
     *
     * @return true if the context has a root node
     */
    boolean hasRoot();

    /**
     * Creates a node.
     *
     * @return a node.
     */
    INode createNode();

    /**
     * Creates a node with a name.
     *
     * @param name a name for a node
     * @return a node.
     */
    INode createNode(String name);

    /**
     * Creates a root node.
     *
     * @return a root node.
     */
    INode createRoot();

    /**
     * Creates a root node with a name.
     *
     * @param name a name for the root
     * @return the root node
     */
    INode createRoot(String name);

    /**
     * Returns iterator over all context nodes.
     *
     * @return iterator over all context nodes
     */
    Iterator<INode> getNodes();

    /**
     * Returns unmodifiable list of all context nodes.
     *
     * @return unmodifiable list of all context nodes
     */
    List<INode> getNodesList();
}