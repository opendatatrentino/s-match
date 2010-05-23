package it.unitn.disi.smatch.data;

/**
 * A Context that contains tree data structure.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Context implements IContext {

    private INode root;

    public Context() {
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
}