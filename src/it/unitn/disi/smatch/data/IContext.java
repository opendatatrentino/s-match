package it.unitn.disi.smatch.data;

import java.util.Vector;

/**
 * The interface to data structure of context.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IContext {

	/**
     * Returns an interface to context of metadata.
     *
     * @return an interface to context of metadata
     */
    IContextData getContextData();

    /**
     * Returns interface to context level functionalities
     * of matching engine.
     *
     * @return interface to context level
     */
    IMatchingContext getMatchingContext();

    /**
     * Returns all the nodes in the tree.
     * The returned list is ordered as in depth first traversal.
     *
     * @return all the nodes in the tree
     */
    Vector<INode> getAllNodes();

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
     * Inserts a new node in the context.
     *
     * @param NodeLabel label of the node
     * @param fatherId  id of the father node
     * @return a new node
     */
    String newNode(String NodeLabel, String fatherId);

    /**
     * changes the name of the node with given id.
     *
     * @param NodeId the id of the node which needs to change
     * @param newLabel the new name of the node
     * @return new name
     */
    String renameNode(String NodeId, String newLabel);

    /**
     * Moves node to the other place in the tree.
     *
     * @param NodeId the id of the node which need to be moved
     * @param newFatherNodeId the father id of the node where it needs to be removed
     */
    void moveNode(String NodeId, String newFatherNodeId);

    /**
     * This method can be used to remove a given Node from the Node hierarchy.
     * Note that if you remove a node that is not a leaf, all its children will be
     * removed from the hierarchy.
     *
     * @param NodeId The identifier of the Node to be removed
     */
    void removeNode(String NodeId);

    /**
     * This method can be used to find a concept in the hierarchy using its Concept Id.
     */
    INode getNode(String conceptId);

    /**
     * Returns string of the labels of all the nodes in the context.
     * Labels are separated by separator.
     * The returned string is ordered as in depth first traversal.
     * Each name is followed by the specified separator.
     *
     * @param separator the string which separates the node names
     * @return string of the labels of all the nodes in the context
     */
    String getAllNodeNames(String separator);

}
