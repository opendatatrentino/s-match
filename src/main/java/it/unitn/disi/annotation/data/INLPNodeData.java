package it.unitn.disi.annotation.data;

import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.smatch.data.trees.IBaseNodeData;

/**
 * Interface to nodes with labels.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface INLPNodeData extends IBaseNodeData {

    /**
     * Returns the label for this node.
     *
     * @return the label for this node
     */
    ILabel getLabel();

    /**
     * Sets the label for this node.
     *
     * @param label the label for this node
     */
    void setLabel(ILabel label);
}
