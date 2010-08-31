package it.unitn.disi.smatch.gui;

import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.INode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A tree model that includes the mapping. Supports coalescing nodes.
 * There can be one range of coalesced nodes among node's children. For example, let these be some node's children
 * 111
 * 222
 * 333
 * 444
 * <p/>
 * if children 1->3 became coalesced
 * <p/>
 * ...  -> 111,222,333
 * 444
 * <p/>
 * Coalesce operation hides the nodes by not reporting them to the tree.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MappingTreeModel extends NodeTreeModel {

    protected INode root;

    //whether this tree is a source tree of a mapping
    private boolean isSource;

    private IContextMapping<INode> mapping;

    public class Coalesce {
        public Point range;
        public DefaultMutableTreeNode sub;
        public INode parent;

        private Coalesce(Point range, DefaultMutableTreeNode sub, INode parent) {
            this.range = range;
            this.sub = sub;
            this.parent = parent;
        }
    }
    // for each node keep an inclusive range of its coalesced children plus a substitute node with ellipsis
    private HashMap<INode, Coalesce> coalesce = new HashMap<INode, Coalesce>();

    public MappingTreeModel(INode root, boolean isSource, IContextMapping<INode> mapping) {
        super(root);
        this.root = root;
        this.isSource = isSource;
        this.mapping = mapping;
    }

    /**
     * Coalesces the <code>parent</code>'s children from <code>start</code> to <code>end</code> (inclusive).
     *
     * @param parent the node with children to coalesce
     * @param start  starting index
     * @param end    ending index
     */
    public void coalesce(INode parent, int start, int end) {
        Coalesce c = coalesce.get(parent);
        if (null != c) {
            uncoalesce(parent);
        }
        if (0 <= start && end < parent.getChildCount() && start < end) {
            DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode();
            c = new Coalesce(new Point(start, end), dmtn, parent);
            dmtn.setUserObject(c);
            coalesce.put(parent, c);

            int[] childIndices = new int[end - start + 1];
            Object[] removedChildren = new Object[end - start + 1];
            for (int i = 0; i < childIndices.length; i++) {
                childIndices[i] = start + i;
                removedChildren[i] = parent.getChildAt(start + i);
            }
            //signal the "removal" of a range
            nodesWereRemoved(parent, childIndices, removedChildren);
            //signal the insertion of a sub
            nodesWereInserted(parent, new int[]{start});
        }
    }

    /**
     * Expands coalesced children.
     *
     * @param parent node to expand coalesced children.
     */
    public void uncoalesce(INode parent) {
        Coalesce c = coalesce.get(parent);
        if (null != c) {
            coalesce.remove(parent);

            int[] childIndices = new int[c.range.y - c.range.x + 1];
            for (int i = 0; i < childIndices.length; i++) {
                childIndices[i] = c.range.x + i;
            }
            //signal the deletion of a sub
            nodesWereRemoved(parent, new int[]{c.range.x}, new Object[]{c.sub});
            //signal the "insertion" of a range
            nodesWereInserted(parent, childIndices);
        }
    }

    /**
     * Expands all coalesced nodes.
     */
    public void uncoalesceAll() {
        List<INode> parents = new ArrayList<INode>(coalesce.keySet());
        while (0 < parents.size()) {
            uncoalesce(parents.get(0));
            parents.remove(0);
        }
        coalesce.clear();        
    }


    /**
     * Expands coalesced children in parent nodes until the node becomes visible.
     *
     * @param node to make visible
     */
    public void uncoalesceParents(final INode node) {
        INode curNode = node;
        while (null != curNode && isCoalescedInAnyParent(curNode)) {
            if (isCoalesced(curNode)) {
                uncoalesce(curNode.getParent());
            }
            curNode = curNode.getParent();
        }
    }


    /**
     * Returns whether the <code>node</code> is coalesced.
     *
     * @param node node to check
     * @return whether the node is coalesced
     */
    public boolean isCoalesced(INode node) {
        boolean result = false;
        INode parent = node.getParent();
        if (null != parent) {
            Coalesce c = coalesce.get(parent);
            if (null != c) {
                int idx = parent.getChildIndex(node);
                result = c.range.x <= idx && idx <= c.range.y;
            }
        }
        return result;
    }

    /**
     * Returns whether any of the <code>node</code>'s parents is coalesced.
     *
     * @param node node to check
     * @return whether any of the node's parents is coalesced
     */
    public boolean isCoalescedInAnyParent(INode node) {
        boolean result = false;
        INode curNode = node;
        while (null != curNode && !result) {
            result = isCoalesced(curNode);
            curNode = curNode.getParent();
        }
        return result;
    }

    /**
     * Returns whether there is a coalesced node in this model.
     *
     * @return whether there is a coalesced node in this model
     */
    public boolean hasCoalescedNode() {
        return !coalesce.isEmpty();
    }

    @Override
    public Object getRoot() {
        if (null == root.getNodeData().getUserObject()) {
            updateUserObject(root);
        }

        return root;
    }

    private List<DefaultMutableTreeNode> updateUserObject(final INode node) {
        List<DefaultMutableTreeNode> result = new ArrayList<DefaultMutableTreeNode>();
        List<IMappingElement<INode>> links;
        if (isSource) {
            links = mapping.getSources(node);
        } else {
            links = mapping.getTargets(node);
        }
        for (IMappingElement<INode> me : links) {
            result.add(new DefaultMutableTreeNode(me));
        }
        node.getNodeData().setUserObject(result);
        return result;
    }

    @Override
    public Object getChild(Object parent, int index) {
        Object result = null;
        if (parent instanceof INode) {
            INode parentNode = (INode) parent;
            Coalesce c = coalesce.get(parentNode);
            if (null == c) {
                if (0 <= index && index < parentNode.getChildCount()) {
                    result = parentNode.getChildAt(index);
                } else {
                    @SuppressWarnings("unchecked")
                    List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) parentNode.getNodeData().getUserObject();
                    if (null == linkNodes) {
                        linkNodes = updateUserObject(parentNode);
                    }
                    if (parentNode.getChildCount() <= index && index < (parentNode.getChildCount() + linkNodes.size())) {
                        result = linkNodes.get(index - parentNode.getChildCount());
                    }
                }
            } else {
                final int coalescedLength = c.range.y - c.range.x;
                final int coalescedIdx = parentNode.getChildCount() - coalescedLength;
                if (0 <= index && index < coalescedIdx) {
                    if (index == c.range.x) {
                        result = c.sub;
                    } else {
                        if (index < c.range.x) {
                            result = parentNode.getChildAt(index);
                        } else {
                            //index > c.range.x
                            result = parentNode.getChildAt(index + coalescedLength);
                        }
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) parentNode.getNodeData().getUserObject();
                    if (null == linkNodes) {
                        linkNodes = updateUserObject(parentNode);
                    }
                    if (coalescedIdx <= index && index < (coalescedIdx + linkNodes.size())) {
                        result = linkNodes.get(index - coalescedIdx);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int getChildCount(Object parent) {
        int result = 0;
        if (parent instanceof INode) {
            INode parentNode = (INode) parent;
            @SuppressWarnings("unchecked")
            List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) parentNode.getNodeData().getUserObject();
            if (null == linkNodes) {
                linkNodes = updateUserObject(parentNode);
            }
            Coalesce c = coalesce.get(parentNode);
            if (null == c) {
                result = parentNode.getChildCount() + linkNodes.size();
            } else {
                final int coalescedLength = c.range.y - c.range.x;
                result = parentNode.getChildCount() + linkNodes.size() - coalescedLength;
            }
        }
        return result;
    }

    @Override
    public boolean isLeaf(Object node) {
        boolean result = true;
        if (node instanceof INode) {
            INode iNode = (INode) node;
            @SuppressWarnings("unchecked")
            List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) iNode.getNodeData().getUserObject();
            if (null == linkNodes) {
                linkNodes = updateUserObject(iNode);
            }

            result = 0 == iNode.getChildCount() && 0 == linkNodes.size();
        }
        return result;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        Object o = path.getLastPathComponent();
        if (o instanceof INode) {
            super.valueForPathChanged(path, newValue);
        } else if (o instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) o;
            if (newValue instanceof Character && dmtn.getUserObject() instanceof IMappingElement) {
                Character rel = (Character) newValue;
                @SuppressWarnings("unchecked")
                IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                mapping.setRelation(me.getSource(), me.getTarget(), rel);
            }
        }
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int result = -1;
        if (null != parent && null != child) {
            if (parent instanceof INode) {
                INode pNode = (INode) parent;
                Coalesce c = coalesce.get(pNode);
                if (null == c) {
                    if (child instanceof INode) {
                        INode cNode = (INode) child;
                        result = pNode.getChildIndex(cNode);
                    } else {
                        if (child instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) child;
                            @SuppressWarnings("unchecked")
                            List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) pNode.getNodeData().getUserObject();
                            if (null == linkNodes) {
                                linkNodes = updateUserObject(pNode);
                            }
                            result = pNode.getChildCount() + linkNodes.indexOf(dmtn);
                        }
                    }
                } else {
                    final int coalescedLength = c.range.y - c.range.x;
                    if (child instanceof INode) {
                        INode cNode = (INode) child;
                        result = pNode.getChildIndex(cNode);
                        if (c.range.x <= result && result <= c.range.y) {
                            //should not get here
                        } else if (c.range.y < result) {
                            result = result - coalescedLength;
                        }
                    } else {
                        if (child instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) child;
                            if (dmtn.getUserObject() instanceof String) {
                                //sub node
                                result = c.range.x;
                            } else {
                                @SuppressWarnings("unchecked")
                                List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) pNode.getNodeData().getUserObject();
                                if (null == linkNodes) {
                                    linkNodes = updateUserObject(pNode);
                                }
                                result = (pNode.getChildCount() - coalescedLength) + linkNodes.indexOf(dmtn);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void nodesWereInserted(TreeNode node, int[] childIndices) {
        if (listenerList != null && node != null && childIndices != null && childIndices.length > 0) {
            int cCount = childIndices.length;
            Object[] newChildren = new Object[cCount];

            for (int counter = 0; counter < cCount; counter++) {
                newChildren[counter] = getChild(node, childIndices[counter]);
            }
            fireTreeNodesInserted(this, getPathToRoot(node), childIndices, newChildren);
        }
    }

    @Override
    public void nodesChanged(TreeNode node, int[] childIndices) {
        if (node != null) {
            if (childIndices != null) {
                int cCount = childIndices.length;

                if (cCount > 0) {
                    Object[] cChildren = new Object[cCount];

                    for (int counter = 0; counter < cCount; counter++) {
                        cChildren[counter] = getChild(node, childIndices[counter]);
                    }
                    fireTreeNodesChanged(this, getPathToRoot(node), childIndices, cChildren);
                }
            } else if (node == getRoot()) {
                fireTreeNodesChanged(this, getPathToRoot(node), null, null);
            }
        }
    }
}