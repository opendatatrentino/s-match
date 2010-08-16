package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;



import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeAccessor;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeSequenceAccessor;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.NamedTreeNodeComparator;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * This is the abstract base class for all tree accessors that allows a specific
 * tree accessor to implement basic tree operations.
 */
public abstract class AbstractTreeAccessor implements ITreeAccessor,
        ITreeSequenceAccessor {

    /**
     * The tree node this tree accessor provides access to.
     */
    protected ITreeNode tree;

    public ITreeNode getRoot() {
        return tree;
    }

    public List<Object> getPreorderSequence() {
        ArrayList<Object> preorderSequenceOfTree = new ArrayList<Object>();
        preorder(getRoot(), preorderSequenceOfTree);
        return preorderSequenceOfTree;
    }

    public List<Object> getPostorderSequence() {
        ArrayList<Object> postorderSequenceOfTree = new ArrayList<Object>();
        postorder(getRoot(), postorderSequenceOfTree);
        return postorderSequenceOfTree;
    }

    public List<Object> getSequence() {
        return getPreorderSequence();
    }

    public ITreeNode getMostRecentCommonAncestor(ITreeNode node1,
                                                 ITreeNode node2) throws InvalidElementException {

        DefaultMutableTreeNode n1 = null, n2 = null;

        n1 = getNode(node1);
        n2 = getNode(node2);

        if (n1 == null) {
            throw new InvalidElementException("Node " + node1.toString()
                    + " is invalid");
        } else if (n2 == null) {
            throw new InvalidElementException("Node " + node2.toString()
                    + " is invalid");
        }

        return (TreeNode) n1.getSharedAncestor(n2);
    }

    public Set<ITreeNode> getDescendants(ITreeNode node)
            throws InvalidElementException {

        TreeSet<ITreeNode> descendants = new TreeSet<ITreeNode>(new NamedTreeNodeComparator());
        DefaultMutableTreeNode n = getNode(node);

        if (n != null) {
            Enumeration en = n.preorderEnumeration();
            // skip first node (is the passed not itself)
            en.nextElement();
            while (en.hasMoreElements()) {
                descendants.add(new TreeNode(((DefaultMutableTreeNode) en
                        .nextElement()).toString()));
            }
            return descendants;
        } else {
            throw new InvalidElementException("Node " + node.toString()
                    + " is invalid");
        }
    }

    public Set<ITreeNode> getAncestors(ITreeNode node)
            throws InvalidElementException {

        TreeSet<ITreeNode> ancestors = new TreeSet<ITreeNode>(new NamedTreeNodeComparator());
        DefaultMutableTreeNode n = getNode(node);

        if (n != null) {
            Enumeration en = n.pathFromAncestorEnumeration(getRoot());
            while (en.hasMoreElements()) {
                ancestors.add(new TreeNode(((DefaultMutableTreeNode) en
                        .nextElement()).toString()));
            }
            // remove source node
            ancestors.remove(node);
            return ancestors;
        } else {
            throw new InvalidElementException("Node " + node.toString()
                    + " is invalid");
        }
    }

    public Set<ITreeNode> getChildren(ITreeNode node)
            throws InvalidElementException {
        TreeSet<ITreeNode> children = new TreeSet<ITreeNode>(new NamedTreeNodeComparator());
        DefaultMutableTreeNode n = getNode(node);

        if (n != null) {
            Enumeration en = n.children();
            while (en.hasMoreElements()) {
                children.add(new TreeNode(((DefaultMutableTreeNode) en
                        .nextElement()).toString()));
            }
            return children;
        } else {
            throw new InvalidElementException("Node " + node.toString()
                    + " is invalid");
        }
    }

    public Set<ITreeNode> getParents(ITreeNode node)
            throws InvalidElementException {
        TreeSet<ITreeNode> parents = new TreeSet<ITreeNode>(new NamedTreeNodeComparator());
        DefaultMutableTreeNode n = getNode(node);

        if (n != null) {
            parents.add(new TreeNode(n.getParent().toString()));
            return parents;
        } else {
            throw new InvalidElementException("Node " + node.toString()
                    + " is invalid");
        }
    }

    public boolean contains(ITreeNode node) {
        DefaultMutableTreeNode n = getNode(node);
        if (n == null) {
            return false;
        } else {
            return ((TreeNode) tree).isNodeDescendant(n);
        }
    }

    public int size() {
        int size = 0;
        Enumeration en = ((TreeNode) tree).breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            en.nextElement();
            size++;
        }
        return size;
    }

    /**
     * This methods finds and return the tree node in the tree that is equal to
     * the user-passed tree node <code>node</code>.
     *
     * @param node user-passed tree node to be found in the tree
     * @return the node that is equal to <code>node</code>
     */
    private final DefaultMutableTreeNode getNode(ITreeNode node) {
        Enumeration en = ((TreeNode) tree).preorderEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode o = (DefaultMutableTreeNode) en
                    .nextElement();
            if (o.toString().equals(node.getUserObject().toString())) {
                return o;
            }
        }
        return null;
    }

    /**
     * Traverses node in preorder manner, i.e, adds node to
     * preorderSequenceOfTree before processing the node's children in the same
     * manner.
     *
     * @param node                   tree node to be processed in preorder manner
     * @param preorderSequenceOfTree data structure to store preorder tree traversal sequence
     */
    private final void preorder(ITreeNode node,
                                ArrayList<Object> preorderSequenceOfTree) {
        if (node != null) {
            preorderSequenceOfTree.add(node);
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                ITreeNode child = (ITreeNode) e.nextElement();
                preorder(child, preorderSequenceOfTree);
            }
        } else {
            return;
        }
    }

    /**
     * Traverses node in postorder manner, i.e, processes the node's children
     * before adding the node to postorderSequenceOfTree.
     *
     * @param node                    tree node to be processed in postorder manner
     * @param postorderSequenceOfTree data structure to store postorder tree traversal sequence
     */
    private final void postorder(ITreeNode node,
                                 ArrayList<Object> postorderSequenceOfTree) {
        if (node != null) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                ITreeNode child = (ITreeNode) e.nextElement();
                postorder(child, postorderSequenceOfTree);
            }
            postorderSequenceOfTree.add(node);
        } else {
            return;
        }
    }
}