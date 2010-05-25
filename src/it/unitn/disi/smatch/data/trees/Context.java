package it.unitn.disi.smatch.data.trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A Context that contains tree data structure.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Context implements IContext {

    private INode root;
    private ArrayList<INode> nodes;

    public Context() {
        root = null;
        nodes = null;
    }

    public void setRoot(INode root) {
        this.root = root;
    }

    public INode getRoot() {
        return root;
    }

    public boolean hasRoot() {
        return null != root;
    }

    public INode createNode() {
        return new Node();
    }

    public INode createNode(String name) {
        return new Node(name);
    }

    public INode createRoot() {
        root = new Node();
        return root;
    }

    public INode createRoot(String name) {
        root = new Node(name);
        return root;
    }

    public Iterator<INode> getNodes() {
        if (hasRoot()) {
            return new Node.StartIterator(root, root.getChildren());
        } else {
            return Collections.<INode>emptyList().iterator();
        }
    }

    public List<INode> getNodesList() {
        if (null != nodes) {
            return Collections.unmodifiableList(nodes);
        } else {
            if (hasRoot()) {
                nodes = new ArrayList<INode>();
                nodes.add(root);
                nodes.addAll(root.getDescendantsList());
                return Collections.unmodifiableList(nodes);
            } else {
                return Collections.emptyList();
            }
        }
    }

    public void trim() {
        if (root instanceof Node) {
            ((Node) root).trim();
        }
    }
}