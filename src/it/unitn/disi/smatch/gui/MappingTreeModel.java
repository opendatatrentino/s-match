package it.unitn.disi.smatch.gui;

import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.INode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * A tree model that includes the mapping.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MappingTreeModel extends NodeTreeModel {

    protected INode root;

    //whether this tree is a source tree of a mapping
    private boolean isSource;

    private IContextMapping<INode> mapping;

    public MappingTreeModel(INode root, boolean isSource, IContextMapping<INode> mapping) {
        super(root);
        this.root = root;
        this.isSource = isSource;
        this.mapping = mapping;
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
            result = parentNode.getChildCount() + linkNodes.size();
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
            if (newValue instanceof Character) {
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
