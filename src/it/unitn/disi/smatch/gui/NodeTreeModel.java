package it.unitn.disi.smatch.gui;

import it.unitn.disi.smatch.data.trees.INode;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * A tree model for contexts.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class NodeTreeModel extends DefaultTreeModel {

    public NodeTreeModel(TreeNode root) {
        super(root, false);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        Object o = path.getLastPathComponent();
        if (o instanceof INode) {
            INode node = (INode) o;
            if (newValue instanceof String) {
                String text = (String) newValue;
                if (!node.getNodeData().getName().equals(text)) {
                    node.getNodeData().setName(text);
                    node.getNodeData().setIsPreprocessed(false);
                }
            }
        }
    }
}
