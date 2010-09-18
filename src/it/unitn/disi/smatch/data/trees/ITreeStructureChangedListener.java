package it.unitn.disi.smatch.data.trees;

import java.util.EventListener;

/**
 * Listener for the tree structure changed event.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ITreeStructureChangedListener extends EventListener {

    /**
     * Receives the signal that the structure of the <code>node</code> has been changed. That is, a child node has been
     * added or deleted.
     * @param node the root of the changed tree
     */
    void treeStructureChanged(INode node);
}
