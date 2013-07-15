package it.unitn.disi.annotation.data;

import it.unitn.disi.smatch.data.trees.BaseContext;
import it.unitn.disi.smatch.data.trees.IBaseTreeStructureChangedListener;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NLPContext extends BaseContext<INLPNode> implements INLPContext, IBaseTreeStructureChangedListener<INLPNode> {

    @Override
    public INLPNode createNode() {
        return new NLPNode();
    }

    @Override
    public INLPNode createNode(String name) {
        return new NLPNode(name);
    }

    @Override
    public INLPNode createRoot() {
        root = new NLPNode();
        root.addTreeStructureChangedListener(this);
        return root;
    }

    @Override
    public INLPNode createRoot(String name) {
        INLPNode result = createRoot();
        result.getNodeData().setName(name);
        return result;
    }
}
