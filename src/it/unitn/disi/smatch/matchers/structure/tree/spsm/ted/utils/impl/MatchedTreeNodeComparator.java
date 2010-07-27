package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl;


import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;

import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;


import java.util.Comparator;



public class MatchedTreeNodeComparator implements Comparator<ITreeNode> {
    IMapping<INode> mappings = null;

    /**
     * Constructor.
     */
    public MatchedTreeNodeComparator(IMapping<INode> mappings) {
        super();
        this.mappings = mappings;
    }

    public int compare(ITreeNode node1, ITreeNode node2) {
        if (node1 == null || node2 == null) {
            throw new NullPointerException();
        } else {

            INode source = (INode) node1.getUserObject();
            INode target = (INode) node2.getUserObject();
            if (mappings.getRelation(source, target) == IMappingElement.EQUIVALENCE)
                return 0;
            if (mappings.getRelation(source, target) == IMappingElement.LESS_GENERAL)
                return 1;
            if (mappings.getRelation(source, target) == IMappingElement.MORE_GENERAL)
                return 2;
            if (mappings.getRelation(source, target) == IMappingElement.DISJOINT)
                return 3;
            return -1;
        }
    }

}
