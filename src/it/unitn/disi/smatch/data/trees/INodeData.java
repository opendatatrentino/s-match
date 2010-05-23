package it.unitn.disi.smatch.data.trees;

import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;

import java.util.Iterator;

/**
 * Data part of the node.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface INodeData {

    String getName();

    void setName(String newName);

    String getcLabFormula();

    void setcLabFormula(String cLabFormula);

    String getcNodeFormula();

    void setcNodeFormula(String cNodeFormula);

    String getId();

    void setId(String newId);

    /**
     * Indicates whether this node belongs to the source context.
     * This is needed for new algorithms which sometimes swap order.
     *
     * @return whether this node belongs to the source context
     */
    boolean getSource();

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
     * Adds atomic concept of label to the node.
     *
     * @param acol atomic concept of label
     */
    void addACoL(IAtomicConceptOfLabel acol);

    /**
     * Adds acol to the receiver acols at index.
     *
     * @param index index where the acol will be added
     * @param acol acol to add
     */
    void addACoL(int index, IAtomicConceptOfLabel acol);

    /**
     * Removes the acol at index from the receiver.
     *
     * @param index index of an acol to remove
     */
    void removeACoL(int index);

    /**
     * Removes acol from the receiver.
     *
     * @param acol acol to remove
     */
    void removeACoL(IAtomicConceptOfLabel acol);

    /**
     * Returns list of atomic concepts for the node matching task.
     * Basically, all concepts including parent concepts with some filtering.
     * Same filter is used in getAllContextACols, therefore it is needed here.
     *
     * @return atomic concepts for the node matching task
     */
    Iterator<IAtomicConceptOfLabel> getNodeMatchingTaskACoLs();

    Object getUserObject();

    void setUserObject(Object newObject);
}
