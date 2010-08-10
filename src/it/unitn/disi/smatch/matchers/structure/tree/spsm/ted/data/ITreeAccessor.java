package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data;

import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException;

import java.util.Set;

//TODO Juan code duplication with existing tree structures
/**
 * Accessor for tree structures. This interface defines methods for basic tree
 * operations.
 */
public interface ITreeAccessor {

    /**
     * Returns the root node of the tree.
     *
     * @return root node of tree
     */
    public ITreeNode getRoot();

    /**
     * Returns the most recent common ancestor (least common subsumer) of two
     * nodes of the tree. Throws an org.openk.core.module.matcher.tree_matcher.utils.impl.InvalidElementException if either
     * <code>node1</code> or <code>node2</code> is invalid.
     *
     * @param node1 first node
     * @param node2 second node
     * @return the most recent common ancestor of <code>node1</code> and
     *         <code>node2</code>
     * @throws InvalidElementException
     */
    public ITreeNode getMostRecentCommonAncestor(ITreeNode node1,
                                                 ITreeNode node2) throws InvalidElementException;

    /**
     * Return the length of the longest directed path in the graph
     *
     * @return length of longest directed path
     */
    public double getMaximumDirectedPathLength();

    /**
     * Returns the set of descendants of a given tree node. This includes its
     * children as well as descendants deeper down in the tree hierarchy. The
     * ordering of the descendants is in preorder. Throws an
     * org.openk.core.module.matcher.tree_matcher.utils.impl.InvalidElementException if <code>node</code> is invalid, i.e., does not
     * occur in the tree for instance.
     *
     * @param node tree node to find its descendants
     * @return set of descendants of the given tree node
     * @throws it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException
     *
     */
    public Set<ITreeNode> getDescendants(ITreeNode node)
            throws InvalidElementException;

    /**
     * Returns the set of ancestors of a given tree node. This includes its
     * parents as well as ancestors of <code>node</code> higher up in the tree
     * hierarchy.
     *
     * @param node tree node to find its ancestors
     * @return set of ancestors of the given tree node
     * @throws InvalidElementException
     */
    public Set<ITreeNode> getAncestors(ITreeNode node)
            throws InvalidElementException;

    /**
     * Returns the set of children of a given tree node. Throws an
     * org.openk.core.module.matcher.tree_matcher.utils.impl.InvalidElementException if <code>node</code> is invalid, i.e., does not
     * occur in the tree for instance.
     *
     * @param node tree node to find its children
     * @return set of children of the given tree node
     * @throws InvalidElementException
     */
    public Set<ITreeNode> getChildren(ITreeNode node)
            throws InvalidElementException;

    /**
     * Returns the set of parents of a given tree node. Throws an
     * org.openk.core.module.matcher.tree_matcher.utils.impl.InvalidElementException if <code>node</code> is invalid, i.e., does not
     * occur in the tree for instance.
     *
     * @param node tree node to find its parents
     * @return set of parents of the given tree node
     * @throws InvalidElementException
     */
    public Set<ITreeNode> getParents(ITreeNode node)
            throws InvalidElementException;

    /**
     * Returns true if <code>node</code> is contained in the tree hierarchy
     * this accessor provides access to.
     *
     * @return true, if <code>node</code> is in tree hierarchy, false
     *         otherwise
     */
    public boolean contains(ITreeNode node);

    /**
     * Returns the size of the tree, i.e., the total number of nodes in this
     * tree.
     *
     * @return size (== number of nodes) of the tree
     */
    public int size();

    /**
     * Return the length of the shortest path connecting <code>nodeA</code>
     * and <code>nodeB</code>.
     *
     * @param nodeA the first node
     * @param nodeB the second node
     * @return length of the shortest path
     */
    public double getShortestPath(ITreeNode nodeA, ITreeNode nodeB);


}