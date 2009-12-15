package it.unitn.disi.smatch.data;

import java.util.Vector;

/**
 * The interface to Context datastructure
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IContext {
    /**
     * Returns an interface to context metadata
     *
     * @return
     */
    IContextData getContextData();

    /**
     * returns interface to context level functionalities
     * of matching engine
     *
     * @return
     */
    IMatchingContext getMatchingContext();

    /**
     * Returns all the nodes in the tree
     * The returned list is ordered as in depth first traversal
     *
     * @return
     */
    Vector<INode> getAllNodes();

    /**
     * Sets a new root for the context
     *
     * @param root
     */
    void setRoot(INode root);

    /**
     * returns the root of the context
     *
     * @return
     */
    INode getRoot();

    /**
     * inserts a new node in the context
     *
     * @param NodeLabel label of the node
     * @param fatherId  id of the father node
     * @return
     */
    String newNode(String NodeLabel, String fatherId);

    /**
     * change the name of the node with given id
     *
     * @param NodeId
     * @param newLabel
     * @return
     */
    String renameNode(String NodeId, String newLabel);

    /**
     * removes node to the other place in the tree
     *
     * @param NodeId
     * @param newFatherNodeId
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
     * This method can be used to find a concept in the hierarchy using its Concept Id
     */
    INode getNode(String conceptId);

    /**
     * Returns string of the labels of all the nodes in the context.
     * Labels are separated by separator
     * The returned string is ordered as in depth first traversal
     * Each name is followed by the specified separator
     *
     * @param separator
     * @return
     */
    String getAllNodeNames(String separator);

}
