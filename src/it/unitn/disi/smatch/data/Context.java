package it.unitn.disi.smatch.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A Context that contains data structure of ctxml file and some methods
 * that applied to concept as whole.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Context implements IMatchingContext, IContext {

    //root concept
    private INode root;

    //Constructor

    public Context() {
        //root = Node.getInstance();
    }

    public static IContext getInstance() {
        return new Context();
    }

    public IMatchingContext getMatchingContext() {
        return this;
    }

    /**
     * Retrieves the list of nodes which are in depth first traversal.
     *
     * @return the list of interfaces of nodes which is depth first traversal orders.
     */
    public List<INode> getAllNodes() {
//        if (allNodes == null) {
        INode root = getRoot();
        List<INode> list = new ArrayList<INode>();
        list.add(root);
        list.addAll(root.getDescendants());

        for (int i = 0; i < list.size(); i++) {
            list.get(i).getNodeData().setIndex(i);
        }

        return list;
//        } else
//            return allNodes;
    }

    public String getAllNodeNames(String separator) {
        separator = " " + separator + " ";
        StringBuilder allConceptNames = new StringBuilder();
        INode root = getRoot();
        allConceptNames.append(root.getNodeName()).append(separator);
        List<INode> descendants = root.getDescendants();
        for (INode c : descendants) {
            allConceptNames.append(c.getNodeName()).append(separator);
        }
        allConceptNames.setLength(allConceptNames.length() - 3);
        return allConceptNames.toString();
    }

    public List<IAtomicConceptOfLabel> getAllContextACoLs() {
        INode root = getRoot();
        List<IAtomicConceptOfLabel> result = new ArrayList<IAtomicConceptOfLabel>();
        result = fillACoLsList(root, result);
        return result;
    }

    private List<IAtomicConceptOfLabel> fillACoLsList(INode cpt, List<IAtomicConceptOfLabel> partialResult) {
        List<IAtomicConceptOfLabel> table = cpt.getNodeData().getACoLs();
        for (IAtomicConceptOfLabel acol : table) {
            int tmpInt = partialResult.size();
            partialResult.add(acol);
            acol.setIndex(tmpInt);
        }
        if (cpt.getChildren().size() > 0) {
            for (int i = 0; i < cpt.getChildren().size(); i++) {
                INode child = cpt.getChildren().get(i);
                this.fillACoLsList(child, partialResult);
            }
        }
        return partialResult;
    }

    public void resetOldPreprocessing() {
        List<INode> allNodes = new ArrayList<INode>(root.getDescendants());
        allNodes.add(root);
        for (INode allNode : allNodes) {
            INodeData c = allNode.getNodeData();
            c.resetLogicalFormula();
            c.resetSetOfSenses();
        }
    }

    public void setRoot(INode root) {
        this.root = root;
    }

    public INode getRoot() {
        return root;
    }

    public String newNode(String NodeLabel, String fatherId) {
        String newNodeId = this.getNewNodeId();
        String newRootNodeId = newNodeId;
        INode node = Node.getInstance(NodeLabel, newNodeId);
        boolean firstRoot = false;
        // The specified input node is the root of the context
        if (fatherId == null) {
            INode oldRoot = this.getRoot();
            this.setRoot(node);
            // The specified node is the new root of the context, so we need to
            // save the old root and made the new root father of the old one.
            if (oldRoot != null && oldRoot.getNodeId() != null && !oldRoot.getNodeId().equals("")) {
                oldRoot.getNodeData().setParent(node);
                this.addNode(oldRoot);
            } else {
                firstRoot = true;
            }
        } else {
            // The specified input node is not a root
            INode father = this.getNode(fatherId);
            node.getNodeData().setParent(father);
            this.addNode(node);
        }

        /// we must change the ids of all the context
        if (fatherId == null && !firstRoot) {
            List<INode> toChangeIds = node.getDescendants();
            for (INode change : toChangeIds) {
                newNodeId = this.getNewNodeId();
                change.getNodeData().setNodeId(newNodeId);
                change.getNodeData().setNodeUniqueName();
            }
        }
        if (fatherId == null) {
            return newRootNodeId;
        }
        return newNodeId;
    }

    public String renameNode(String NodeId, String newLabel) {
        INodeData node = this.getNode(NodeId).getNodeData();
        String newNodeId = this.getNewNodeId();
        node.setNodeName(newLabel);
        node.setNodeId(newNodeId);
        node.setNodeUniqueName();
        node.resetLogicalFormula();
        node.resetSetOfSenses();
        return newNodeId;
    }

    public void moveNode(String NodeId, String newFatherNodeId) {
        /// first remove the Node from its actual position
        INode toBeMoved = this.getNode(NodeId);
        List<INode> nodesToBeMoved;
        if (newFatherNodeId != null) {
            nodesToBeMoved = toBeMoved.getDescendants();
        } else {
            nodesToBeMoved = this.getAllNodes();
        }
        this.removeNode(NodeId);
        /// now insert the Node in its new position in the hierarchy
        String newNodeId = this.getNewNodeId();
        toBeMoved.getNodeData().setNodeId(newNodeId);
        toBeMoved.getNodeData().setNodeUniqueName();
        if (newFatherNodeId != null) {
            INode father = this.getNode(newFatherNodeId);
            toBeMoved.getNodeData().setParent(father);
            /// put the new Node in the hierarchy
            this.addNode(toBeMoved);
        } else {
            INode oldRoot = this.getRoot();
            /// put the new root in the hierarchy
            toBeMoved.getNodeData().setParent(null);
            this.setRoot(toBeMoved);
            oldRoot.getNodeData().setParent(toBeMoved);
            /// put the old root in the hierarchy
            this.addNode(oldRoot);
        }
        /// change the ids of all the moved Nodes
        for (INode move : nodesToBeMoved) {
            newNodeId = this.getNewNodeId();
            move.getNodeData().setNodeId(newNodeId);
            move.getNodeData().setNodeUniqueName();
        }
    }

    //Context staff

    /**
     * This method adds a given Node to the Node hierarchy.
     *
     * @param Node The interface of the node to be added
     */
    private void addNode(INode Node) {
        INode father = Node.getParent();
        if (father != null) {
            father.addChild(Node);
        }
    }

    /**
     * This method can be used to remove a given Node from the Node hierarchy.
     * Note that if you remove a node that is not a leaf, all its children will be
     * removed from the hierarchy. So, if this method is called from the editor, the
     * editor must "conserve" the children node and if the user decides to connect one
     * of them (for example C) to another node of the hierarchy the editor must
     * re-add the Node C to the Node hierarchy (using the addNode(C) method)
     * otherwise the Node will be lost.
     *
     * @param NodeId The identifier of the Node to be removed
     */
    public void removeNode(String NodeId) {
        INode toBeRemoved = getNode(NodeId);
        INode father = toBeRemoved.getParent();
        father.removeChild(toBeRemoved);
    }

    private int countNode = 1;

    private String getNewNodeId() {
        //Get the time at which the object has been created, expressed in milliseconds
        long now = System.currentTimeMillis();
        String id = "c" + countNode + "_" + (now % (365 * 24 * 3600));
        countNode++;
        return id;
    }

    /**
     * This method can be used to find a concept in the hierarchy using its Concept Id.
     */
    public INode getNode(String conceptId) {
        INode result = getNode(conceptId, root);
        if (result == null) {
            System.out.println("Required concept not in the hiearchy");
        }
        return result;
    }

    /**
     * This method finds a concept in the sub-hierarchy starting
     * form the specified node, by the using its Node Id.
     *
     * @param nodeId The Id of the concept to be returned
     * @param node   The root of the sub-hierarchy
     * @return The concept if present in the context, null if the concept is not
     *         found
     */
    private INode getNode(String nodeId, INode node) {
        INode result = null;
        String localId = node.getNodeId();
        if (localId.equals(nodeId)) {
            return node;
        }
        List<INode> children = node.getChildren();
        for (INode child : children) {
            result = getNode(nodeId, child);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public void updateNodeIds() {
        int oldCountNode = countNode;
        countNode = 1;
        List<INode> allNodes = new ArrayList<INode>(root.getDescendants());
        allNodes.add(root);
        for (INode node : allNodes) {
            node.getNodeData().setNodeId(getNewNodeId());
            node.getNodeData().setNodeUniqueName();
        }
        countNode = oldCountNode;
    }
}
