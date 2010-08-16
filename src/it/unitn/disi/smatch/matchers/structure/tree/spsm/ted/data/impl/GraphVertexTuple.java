package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl;



public class GraphVertexTuple extends GenericTuple {

    private TreeNodeTuple treeNodeTuple;

    /**
     * Constructor.
     * <p/>
     * Takes two Integer objects which represent the objects of this tuple.
     *
     * @param left  left Integer object
     * @param right right Integer object
     */
    public GraphVertexTuple(Integer left, Integer right) {
        super(left, right);
    }

    /**
     * Constructor.
     * <p/>
     * Takes two integer values which represent the objects of this tuple.
     *
     * @param left  left integer object
     * @param right right integer object
     */
    public GraphVertexTuple(int left, int right) {
        super(new Integer(left), new Integer(right));
    }

    public String toString() {
        return getLeft().toString() + ":" + getRight().toString();
    }

    public Integer getLeft() {
        return (Integer) super.getLeft();
    }

    public Integer getRight() {
        return (Integer) super.getRight();
    }

    /**
     * Return this object tuple.
     *
     * @return this tuple.
     */
    public TreeNodeTuple getTreeNodeTuple() {
        return treeNodeTuple;
    }

    /**
     * Sets this object tuple to <code>treeNodeTuple</code>.
     *
     * @param treeNodeTuple the treeNodeTuple to be set
     */
    public void setTreeNodeTuple(TreeNodeTuple treeNodeTuple) {
        this.treeNodeTuple = treeNodeTuple;
    }
}
