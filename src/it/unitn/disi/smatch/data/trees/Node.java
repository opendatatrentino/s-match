package it.unitn.disi.smatch.data.trees;

import it.unitn.disi.smatch.data.ling.AtomicConceptOfLabel;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.matrices.IndexedObject;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * This class represents a node in the hierarchy. It contains logical (cNode and cLab formulas),
 * linguistic (WN senses, tokens) and structural information (parent and children of a node).
 * <p/>
 * Many things are modeled after DefaultMutableTreeNode.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Node extends IndexedObject implements INode, INodeData {

    private INode parent;
    private List<INode> children;

    // id is needed to store cNodeFormulas correctly.
    // cNodeFormula is made of cLabFormulas, each of which refers to tokens and tokens should have unique id
    // within a context. This is achieved by using node id + token id for each token 
    private String id;
    private String name;
    private String cLabFormula;
    private String cNodeFormula;
    // might be better implemented for a whole context via BitSet
    private boolean source;
    private Object userObject;

    private List<IAtomicConceptOfLabel> acols;

    // node counter to set unique node id during creation
    private static long countNode = 0;

    // iterator for nodes which have no children
    private static final Iterator<INode> EMPTY_NODE_ITERATOR = Collections.<INode>emptyList().iterator();
    // iterator for nodes which have no acols
    private static final Iterator<IAtomicConceptOfLabel> EMPTY_ACOL_ITERATOR = Collections.<IAtomicConceptOfLabel>emptyList().iterator();

    // iterator which iterates over all parent nodes

    private static final class Ancestors implements Iterator<INode> {
        private INode current;

        public Ancestors(INode start) {
            if (null == start) {
                throw new IllegalArgumentException("argument is null");
            }
            this.current = start;
        }

        public boolean hasNext() {
            return current.hasParent();
        }

        public INode next() {
            current = current.getParent();
            return current;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // start with a start node and then iterates over nodes from iterator i

    private static final class StartIterator implements Iterator<INode> {
        private INode start;
        private Iterator<INode> i;

        public StartIterator(INode start, Iterator<INode> i) {
            if (null == start) {
                throw new IllegalArgumentException("argument is null");
            }
            this.start = start;
            this.i = i;
        }

        public boolean hasNext() {
            return (null != start || i.hasNext());
        }

        public INode next() {
            INode result = start;
            if (null != start) {
                start = null;
            } else {
                result = i.next();
            }
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class BreadthFirstSearch implements Iterator<INode> {
        private Deque<INode> queue;

        public BreadthFirstSearch(INode start) {
            if (null == start) {
                throw new IllegalArgumentException("argument is null");
            }
            queue = new ArrayDeque<INode>();
            queue.addFirst(start);
            next();
        }

        public boolean hasNext() {
            return !queue.isEmpty();
        }

        public INode next() {
            INode current = queue.removeFirst();
            for (Iterator<INode> i = current.getChildren(); i.hasNext();) {
                queue.add(i.next());
            }
            return current;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // iterates over acols of a node and all acols of all parent nodes

    private static final class NodeMatchingTaskACoLs implements Iterator<IAtomicConceptOfLabel> {
        private Iterator<INode> ancestors;
        private Iterator<IAtomicConceptOfLabel> current;

        public NodeMatchingTaskACoLs(INode start) {
            if (null == start) {
                throw new IllegalArgumentException("argument is null");
            }
            ancestors = start.getAncestors();
            current = start.getNodeData().getACoLs();
        }

        public boolean hasNext() {
            boolean result = current.hasNext();
            while (!result && ancestors.hasNext()) {
                current = ancestors.next().getNodeData().getACoLs();
                result = current.hasNext();
            }
            return result;
        }

        public IAtomicConceptOfLabel next() {
            return current.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Node() {
        parent = null;
        children = null;

        source = false;
        // need to set node id to keep track of acols in c@node formulas
        // synchronized to make counts unique within JVM and decrease the chance of creating the same id
        synchronized (Node.class) {
            id = "n" + countNode + "_" + ((System.currentTimeMillis() / 1000) % (365 * 24 * 3600));
            countNode++;
        }
        name = "";
        cLabFormula = "";
        cNodeFormula = "";
        acols = null;
        index = -1;
    }

    /**
     * Constructor class which sets the node name.
     *
     * @param name the name of the node
     */
    public Node(String name) {
        this();
        this.name = name;
    }

    public INode getChildAt(int index) {
        if (children == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        return children.get(index);
    }

    public int getChildCount() {
        if (children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    public int getChildIndex(INode child) {
        if (null == child) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!isNodeChild(child)) {
            return -1;
        }
        return children.indexOf(child);
    }

    public Iterator<INode> getChildren() {
        if (null == children) {
            return EMPTY_NODE_ITERATOR;
        } else {
            return children.iterator();
        }
    }

    public INode createChild() {
        INode child = new Node();
        addChild(child);
        return child;
    }

    public INode createChild(String name) {
        INode child = new Node(name);
        addChild(child);
        return child;
    }

    public void addChild(INode child) {
        addChild(getChildCount(), child);
    }

    public void addChild(int index, INode child) {
        if (null == child) {
            throw new IllegalArgumentException("new child is null");
        } else if (isNodeAncestor(child)) {
            throw new IllegalArgumentException("new child is an ancestor");
        }

        INode oldParent = child.getParent();

        if (null != oldParent) {
            oldParent.removeChild(child);
        }

        child.setParent(this);
        if (null == children) {
            children = new ArrayList<INode>();
        }
        children.add(index, child);
    }

    public void removeChild(int index) {
        INode child = getChildAt(index);
        children.remove(index);
        child.setParent(null);
    }

    public void removeChild(INode child) {
        if (null == child) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!isNodeChild(child)) {
            removeChild(getChildIndex(child));
        }
    }

    public INode getParent() {
        return parent;
    }

    public void setParent(INode newParent) {
        removeFromParent();
        parent = newParent;
    }

    public boolean hasParent() {
        return null != parent;
    }

    public void removeFromParent() {
        if (null != parent) {
            parent.removeChild(this);
            parent = null;
        }
    }

    public boolean isLeaf() {
        return 0 == getChildCount();
    }

    public int getAncestorCount() {
        INode parent;
        int levels = 0;

        parent = this;
        while ((parent = parent.getParent()) != null) {
            levels++;
        }

        return levels;
    }

    public Iterator<INode> getAncestors() {
        return new Ancestors(this);
    }

    public Iterator<INode> getSupertree() {
        return new StartIterator(this, getAncestors());
    }

    public int getLevel() {
        return getAncestorCount();
    }

    public int getDescendantCount() {
        int result = 0;
        for (Iterator<INode> i = getDescendants(); i.hasNext();) {
            i.next();
            result++;
        }
        return result;
    }

    public Iterator<INode> getDescendants() {
        return new BreadthFirstSearch(this);
    }

    public Iterator<INode> getSubtree() {
        return new StartIterator(this, getDescendants());
    }

    public INodeData getNodeData() {
        return this;
    }

    private boolean isNodeAncestor(INode anotherNode) {
        if (null == anotherNode) {
            return false;
        }

        INode ancestor = this;

        do {
            if (ancestor == anotherNode) {
                return true;
            }
        } while ((ancestor = ancestor.getParent()) != null);

        return false;
    }

    private boolean isNodeChild(INode node) {
        if (null == node) {
            return false;
        } else {
            if (getChildCount() == 0) {
                return false;
            } else {
                return (node.getParent() == this);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getId() {
        return id;
    }

    public void setId(String newId) {
        id = newId;
    }

    public String getcLabFormula() {
        return cLabFormula;
    }

    public void setcLabFormula(String cLabFormula) {
        this.cLabFormula = cLabFormula;
    }

    public String getcNodeFormula() {
        return cNodeFormula;
    }

    public void setcNodeFormula(String cNodeFormula) {
        this.cNodeFormula = cNodeFormula;
    }

    public boolean getSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public IAtomicConceptOfLabel getACoLAt(int index) {
        if (null == acols) {
            throw new ArrayIndexOutOfBoundsException("node has no ACoLs");
        }
        return acols.get(index);
    }

    public int getACoLCount() {
        if (acols == null) {
            return 0;
        } else {
            return acols.size();
        }
    }

    public int getACoLIndex(IAtomicConceptOfLabel acol) {
        if (null == acol) {
            throw new IllegalArgumentException("argument is null");
        }

        return acols.indexOf(acol);
    }

    public Iterator<IAtomicConceptOfLabel> getACoLs() {
        if (null == acols) {
            return EMPTY_ACOL_ITERATOR;
        } else {
            return acols.iterator();
        }
    }

    public IAtomicConceptOfLabel createACoL() {
        return new AtomicConceptOfLabel();
    }

    public void addACoL(IAtomicConceptOfLabel acol) {
        addACoL(getACoLCount(), acol);
    }

    public void addACoL(int index, IAtomicConceptOfLabel acol) {
        if (null == acol) {
            throw new IllegalArgumentException("new acol is null");
        }

        if (null == acols) {
            acols = new ArrayList<IAtomicConceptOfLabel>();
        }
        acols.add(index, acol);
    }

    public void removeACoL(int index) {
        acols.remove(index);
    }

    public void removeACoL(IAtomicConceptOfLabel acol) {
        acols.remove(acol);
    }

    public Iterator<IAtomicConceptOfLabel> getNodeMatchingTaskACoLs() {
        return new NodeMatchingTaskACoLs(this);
    }

    public void setUserObject(Object object) {
        userObject = object;
    }

    public Object getUserObject() {
        return userObject;
    }


    public String toString() {
        return name;
    }

    public int getIndex(TreeNode node) {
        if (node instanceof INode) {
            return getChildIndex((INode) node);
        } else {
            return -1;
        }
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }

    public void insert(MutableTreeNode child, int index) {
        if (child instanceof INode) {
            addChild(index, (INode) child);
        }
    }

    public void remove(int index) {
        removeChild(index);
    }

    public void remove(MutableTreeNode node) {
        if (node instanceof INode) {
            removeChild((INode) node);
        }
    }

    public void setParent(MutableTreeNode newParent) {
        if (newParent instanceof INode) {
            setParent((Node) newParent);
        }
    }
}