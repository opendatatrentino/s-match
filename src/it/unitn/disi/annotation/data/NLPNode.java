package it.unitn.disi.annotation.data;

import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.smatch.data.trees.BaseNode;

/**
 * A node with a label.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NLPNode extends BaseNode<INLPNode, INLPNodeData> implements INLPNode, INLPNodeData {

    protected ILabel label;

    public NLPNode() {
        super();
        label = null;
    }

    public NLPNode(String name) {
        super(name);
    }

    public ILabel getLabel() {
        return label;
    }

    public void setLabel(ILabel label) {
        this.label = label;
    }

    @Override
    public INLPNode createChild() {
        INLPNode child = new NLPNode();
        addChild(child);
        return child;
    }

    @Override
    public INLPNode createChild(String name) {
        INLPNode child = new NLPNode(name);
        addChild(child);
        return child;
    }
}
