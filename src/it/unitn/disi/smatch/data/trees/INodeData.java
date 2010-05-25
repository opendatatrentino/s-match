package it.unitn.disi.smatch.data.trees;

import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;

import java.util.Iterator;

/**
 * An interface to the data part of the node.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface INodeData {

    /**
     * Returns the node name, that is a natural language label associated with it.
     *
     * @return the node name
     */
    String getName();

    /**
     * Sets the node name.
     *
     * @param newName new node name
     */
    void setName(String newName);

    /**
     * Returns the concept of a label formula. Concept of a label is a logical representation of the
     * natural language label associated with a node.
     *
     * @return the concept of a label formula
     */
    String getcLabFormula();

    /**
     * Sets the concept of a label formula.
     *
     * @param cLabFormula the concept of a label formula
     */
    void setcLabFormula(String cLabFormula);

    /**
     * Returns the concept at node formula. Concept at node formula is a logical representation of the concept
     * of a node located in a certain part of the tree.
     *
     * @return the concept at node formula
     */
    String getcNodeFormula();

    /**
     * Sets the concept at node formula.
     *
     * @param cNodeFormula the concept at node formula
     */
    void setcNodeFormula(String cNodeFormula);

    /**
     * Returns the id of the node. The id of the node should be unique within a context.
     *
     * @return the id of the node
     */
    String getId();

    /**
     * Sets the id of the node.
     *
     * @param newId the new id of the node
     */
    void setId(String newId);

    /**
     * Indicates whether this node belongs to the source context.
     * This is needed for new algorithms which sometimes swap order of the nodes during tree traversal.
     *
     * @return true if the node belongs to the source context
     */
    boolean getSource();

    /**
     * Sets the source flag.
     *
     * @param source the source flag
     */
    void setSource(boolean source);

    /**
     * Returns the acol at index index.
     */
    IAtomicConceptOfLabel getACoLAt(int index);

    /**
     * Returns the number of acols.
     */
    int getACoLCount();

    /**
     * Returns the index of acol in the receivers acols. If the receiver does not contain acol, -1 will be
     * returned.
     *
     * @param acol an acol to search for
     * @return the index of acol in the receivers acols
     */
    int getACoLIndex(IAtomicConceptOfLabel acol);

    /**
     * Returns atomic concepts of labels associated with the given node.
     *
     * @return atomic concepts of labels
     */
    Iterator<IAtomicConceptOfLabel> getACoLs();

    /**
     * Creates an instance of an ACoL.
     *
     * @return an instance of an ACoL
     */
    IAtomicConceptOfLabel createACoL();

    /**
     * Adds atomic concept of label to the node acols.
     *
     * @param acol atomic concept of label
     */
    void addACoL(IAtomicConceptOfLabel acol);

    /**
     * Adds acol to the receiver acols at index.
     *
     * @param index index where the acol will be added
     * @param acol  acol to add
     */
    void addACoL(int index, IAtomicConceptOfLabel acol);

    /**
     * Removes the acol at index from the receiver acols.
     *
     * @param index index of an acol to remove
     */
    void removeACoL(int index);

    /**
     * Removes acol from the receiver acols.
     *
     * @param acol acol to remove
     */
    void removeACoL(IAtomicConceptOfLabel acol);

    /**
     * Returns list of atomic concepts for the node matching task. Basically, it returns all concepts from this node
     * and all concepts from all parent nodes.
     *
     * @return atomic concepts for the node matching task
     */
    Iterator<IAtomicConceptOfLabel> getNodeMatchingTaskACoLs();

    Object getUserObject();

    void setUserObject(Object newObject);
}
