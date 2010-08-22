package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl;

import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.INode;

import java.util.Comparator;

public class MatchedTreeNodeComparator implements Comparator<INode> {

    private IMapping<INode> mappings = null;

    public MatchedTreeNodeComparator(IMapping<INode> mappings) {
        this.mappings = mappings;
    }

    public int compare(INode source, INode target) {
        if (null == source || null == target) {
            throw new NullPointerException();
        } else {
            if (mappings.getRelation(source, target) == IMappingElement.EQUIVALENCE) {
                return 0;
            }
            if (mappings.getRelation(source, target) == IMappingElement.LESS_GENERAL) {
                return 1;
            }
            if (mappings.getRelation(source, target) == IMappingElement.MORE_GENERAL) {
                return 2;
            }
            if (mappings.getRelation(source, target) == IMappingElement.DISJOINT) {
                return 3;
            }
            return -1;
        }
    }
}