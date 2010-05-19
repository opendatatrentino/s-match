package it.unitn.disi.smatch.data;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * This class represents a node in the hierarchy.
 * It contains logical (cNode and cLab formulas);
 * linguistic (WN senses,tokens) and structural information
 * (parent and children of Node).
 */
public class Node implements INodeData, INode {
    //ConceptID
    private String nodeId;
    //Sentence associated with the concept
    private String nodeName;
    //unique name
    private String nodeUniqueName;

    private String cLabFormula = "";
    private String cNodeFormula = "";

    private List<IAtomicConceptOfLabel> setOfSenses;
    private List<IAtomicConceptOfLabel> nodeMatchingTaskACols;
    private HashMap<String, IAtomicConceptOfLabel> nodeMatchingTaskAColsHash;
    private INode parent;
    //private String parentRelationType;

    private List<INode> children;
    private List<INode> descendants = null;
    private List<INode> ancestors = null;

    private int index;
    //might be better implemented for a whole context via BitSet
    private boolean source;

    private Object userObject;

    private static final Comparator<INode> nodeComparator = new Comparator<INode>() {
        //no safety checks - it should be run properly :-)

        public int compare(INode e1, INode e2) {
            return e1.getNodeName().compareTo(e2.getNodeName());
        }
    };

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<String> getSynonyms() {
        return new ArrayList<String>();
    }

    public List<String> getAlternativeLabels() {
        return new ArrayList<String>();
    }

    public double getWeight() {
        return -1;
    }

    public String getcLabFormula() {
        return cLabFormula;
    }

    public void setcLabFormula(String cLabFormula) {
        this.cLabFormula = cLabFormula;
    }

    public void setcNodeFormula(String cNodeFormula) {
        this.cNodeFormula = cNodeFormula;
    }

    public Node() {
        source = false;
        index = -1;
        nodeId = "";
        nodeName = "";
        nodeUniqueName = "";
        parent = null;
        children = new ArrayList<INode>();
        //parentRelationType = null;
        cLabFormula = "";
        setOfSenses = new ArrayList<IAtomicConceptOfLabel>();
    }

    /**
     * Constructor class which sets the node name and id. Also sets unique node name with combination of node name and id.
     *
     * @param nodeName the name of the node
     * @param nodeId   the id of the node
     */
    public Node(String nodeName, String nodeId) {
        this();
        this.nodeName = nodeName.trim();
        this.nodeId = nodeId.trim();
        setNodeUniqueName();
    }

    public static INode getInstance() {
        return new Node();
    }

    public INodeData getNodeData() {
        return this;
    }

    public void resetLogicalFormula() {
        cLabFormula = "";
        cNodeFormula = "";
    }

    public void resetSetOfSenses() {
        setOfSenses = new ArrayList<IAtomicConceptOfLabel>();
    }

    public void addAtomicConceptOfLabel(IAtomicConceptOfLabel sense) {
        sense.setTokenUID(this.getNodeId() + "." + sense.getIdToken());
        setOfSenses.add(sense);
    }

    public void addChild(INode child) {
        children.add(child);
    }

    public List<INode> getAncestors() {
        List<INode> result = ancestors;
        if (null == result) {
            result = new ArrayList<INode>();
            if (parent != null) {
                result.add(parent);
                result.addAll(parent.getAncestors());
            }
            ancestors = result;
        }
        return result;
    }

    public List<INode> getDescendants() {
        List<INode> result = descendants;
        if (result == null) {
            result = new ArrayList<INode>();
            for (INode child : getChildren()) {
                result.add(child);
                result.addAll(child.getDescendants());
            }
            descendants = result;
        }

        return result;
    }

    public int getDescendantCount() {
        int result = 1;
        for (INode child : children) {
            result = result + child.getDescendantCount();
        }
        return result;

    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof INode)) {
            return false;
        }

        final Node node = (Node) o;

        if (!nodeId.trim().equals(node.nodeId.trim())) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return nodeId.hashCode();
    }

    //equals
//    public boolean equals(Node node) {
//        String localId = this.nodeId;
//        localId = localId.trim();
//        String otherId = node.getNodeId();
//        otherId = otherId.trim();
//        if (localId.equals(otherId))
//            return true;
//        return false;
//    }

    //isRoot

    public boolean isRoot() {
        if (/*parentRelationType == null && */parent == null) {
            return true;
        }
        return false;
    }

    public int getDepth() {
        return this.getAncestors().size();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean getSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public void sort() {
        Collections.sort(children, nodeComparator);
        for (INode node : children) {
            node.getNodeData().sort();
        }
    }

    //Get path to root for output

    public String getPathToRootString() {
        INode concept = this;
        StringBuffer path = new StringBuffer("/");
        List<INode> reversePath = concept.getAncestors();
        if (concept.isRoot()) {
            path.append("/");
        }
        for (int i = reversePath.size() - 1; i >= 0; i--) {
            INode c = (reversePath.get(i));
            path.append(c.getNodeName()).append("/");
        }
        path.append(concept.getNodeName());
        return path.toString();
    }

    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public INode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return 0 < getChildCount();
    }

    public boolean isLeaf() {
        return 0 == getChildCount();
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }

    public String getParentRelationType() {
        return null/*parentRelationType*/;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeUniqueName() {
        return nodeUniqueName;
    }

    public List<INode> getChildren() {
        return children;
    }

    public void setParent(INode parent) {
        this.parent = parent;
    }

    public String getCNodeFormula() {
        return cNodeFormula;
    }

    public List<IAtomicConceptOfLabel> getACoLs() {
        return setOfSenses;
    }

    public void setNodeUniqueName(String uniqueName) {
        this.nodeUniqueName = uniqueName;
        StringTokenizer extractIdAndName = new StringTokenizer(uniqueName, "$");
        nodeName = extractIdAndName.nextToken().trim();
        nodeId = extractIdAndName.nextToken().trim();
    }

    public String toString() {
        return nodeName;
    }

    public static INode getInstance(String nodeName, String nodeId) {
        return new Node(nodeName, nodeId);
    }

    public void setNodeUniqueName() {
        this.nodeUniqueName = nodeName + "$" + nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void removeChild(INode child) {
        for (int i = 0; i < children.size(); i++) {
            INode localChild = (children.get(i));
            if (localChild.equals(child)) {
                children.remove(i);
                return;
            }
        }
    }

    public IAtomicConceptOfLabel getNMTAColById(String tokenUID) {
        if (null == nodeMatchingTaskAColsHash) {
            nodeMatchingTaskAColsHash = new HashMap<String, IAtomicConceptOfLabel>();
            for (IAtomicConceptOfLabel acol : getNodeMatchingTaskACols()) {
                nodeMatchingTaskAColsHash.put(acol.getTokenUID(), acol);
            }
        }
        return nodeMatchingTaskAColsHash.get(tokenUID);
    }

    public IAtomicConceptOfLabel getAColById(String tokenUID) {
        IAtomicConceptOfLabel result = null;
        for (IAtomicConceptOfLabel a : setOfSenses) {
            if (tokenUID.equals(a.getTokenUID())) {
                result = a;
                break;
            }
        }
        return result;
    }

    /**
     * Fills and gets the list of all logical formula representations of all concepts.
     */
    public List<IAtomicConceptOfLabel> getNodeMatchingTaskACols() {
        if (null == nodeMatchingTaskACols) {
            nodeMatchingTaskACols = new ArrayList<IAtomicConceptOfLabel>();
            nodeMatchingTaskACols = getNodeMatchingTaskACols(this, nodeMatchingTaskACols);
        }
        return nodeMatchingTaskACols;
    }

    /**
     * Fills the list with Atomic concepts identifiers.
     * They are used as propositional variables in the formula.
     *
     * @param node          the interface of node which acols will be added
     * @param partialResult list of atomic concept of labels which are added so far without current node
     * @return list of atomic concept of label with current node
     */
    private static List<IAtomicConceptOfLabel> getNodeMatchingTaskACols(INode node, List<IAtomicConceptOfLabel> partialResult) {
        if (!node.isRoot()) {
            getNodeMatchingTaskACols(node.getParent(), partialResult);
        }
        List<IAtomicConceptOfLabel> table = node.getNodeData().getACoLs();
        for (IAtomicConceptOfLabel acol : table) {
            partialResult.add(acol);
        }
        return partialResult;
    }

    public void insert(MutableTreeNode child, int index) {
        if (child instanceof Node) {
            children.add(index, (INode) child);
        }
    }

    public void remove(int index) {
        children.remove(index);
    }

    public void remove(MutableTreeNode node) {
        children.remove(node);
    }

    public void setUserObject(Object object) {
        userObject = object;
    }

    public Object getUserObject() {
        return userObject;
    }

    public void removeFromParent() {
        if (null != parent) {
            parent.removeChild(this);
        }
        parent = null;
    }

    public void setParent(MutableTreeNode newParent) {
        if (newParent instanceof Node) {
            if (null != parent) {
                removeFromParent();
            }
            parent = (Node) newParent;
        }
    }
}