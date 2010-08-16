package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl;

/*
 * $Id:org.openk.core.module.matcher.tree_matcher.utils.impl.NamedTreeNodeComparator.java 425 2006-04-21 15:23:43Z ddis $
 *
 * Created on Oct 21, 2005
 *
 * See LICENSE for more information about licensing and warranties.
 */


import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;

import java.util.Comparator;

/**
 * Compares two tree nodes for equality. Checking on equality of user object's
 * unique name if object is of FAMIX instance Entity or .toString otherwise.
 */
public class NamedTreeNodeComparator implements Comparator<ITreeNode> {

    /**
     * Constructor.
     */
    public NamedTreeNodeComparator() {
        super();
    }

    public int compare(ITreeNode node1, ITreeNode node2) {
        if (node1 == null || node2 == null) {
            throw new NullPointerException();
        } else {
            Object o1 = node1.getUserObject();
            Object o2 = node2.getUserObject();
            return o1.toString().compareTo(o2.toString());
        }
    }
}
