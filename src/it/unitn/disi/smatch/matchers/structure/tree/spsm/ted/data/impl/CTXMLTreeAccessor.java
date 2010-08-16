package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;

import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

import java.util.List;

public class CTXMLTreeAccessor extends AbstractTreeAccessor {
	//TODO try to understand Mikalai's code!
	
    //TODO Juan, do we now need this? And is CTXML a good name for it?
    //TODO Juan, please, check unused commented code, unnecessary casts, 
	//javadocs and comments, constants on the left in comparisons, etc
    //TODO Juan, P.S. applies for other classes as well.

    //TODO Juan, why fixed constant?
    ITreeNode[] path = new ITreeNode[100];
    IContext context = null;


    public CTXMLTreeAccessor(IContext c) {
        context = c;
    }

    void addNode(INode node, int depth) {
        path[depth] = new TreeNode(node, true);
        if (depth > 0) {
            path[depth - 1].add(path[depth]);
        }
        List<INode> children = node.getChildrenList();
        if (children != null) {

            for (INode aChildren : children) {
                INode child =  aChildren;
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
        return 0;  //TODO, check whether we actually need to compute this, if used somewhere else
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
        return 0;  //TODO, check whether we actually need to compute this, if used somewhere else
    }
}
