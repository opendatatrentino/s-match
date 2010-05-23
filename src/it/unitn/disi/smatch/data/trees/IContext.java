package it.unitn.disi.smatch.data.trees;

/**
 * The interface to data structure of context.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
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
}