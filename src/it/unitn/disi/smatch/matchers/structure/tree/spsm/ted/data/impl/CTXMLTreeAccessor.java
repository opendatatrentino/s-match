package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;

import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

import java.util.List;
import java.util.Vector;

public class CTXMLTreeAccessor extends AbstractTreeAccessor {

    ITreeNode[] path = new ITreeNode[100];
    IContext context = null;


    public CTXMLTreeAccessor(IContext c) {
        context = c;
    }

    void addNode(INode node, int depth) {
//        TreePath toAdd = null;
        path[depth] = new TreeNode(node/*.getNodeName()*/, true);
        if (depth > 0) {
            path[depth - 1].add(path[depth]);
        }
        List<INode> children = node.getChildrenList();
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                INode child = (INode) children.get(i);
                addNode(child, depth + 1);
            }
        }
    }

    public ITreeNode getRoot() {
        // if tree has not yet been built, build it, return old tree otherwise
        if (tree == null) {
            addNode(context.getRoot(), 0);
            tree = path[0];
        }
        return tree;
    }


    /**
     * Return the length of the longest directed path in the graph
     *
     * @return length of longest directed path
     */
    public double getMaximumDirectedPathLength() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Return the length of the shortest path connecting <code>nodeA</code>
     * and <code>nodeB</code>.
     *
     * @param nodeA the first node
     * @param nodeB the second node
     * @return length of the shortest path
     */
    public double getShortestPath(ITreeNode nodeA, ITreeNode nodeB) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
