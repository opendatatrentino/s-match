package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * Base class for loaders.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class BaseContextLoader extends Configurable implements IContextLoader {

    protected int nodesParsed = 0;

    protected void createIds(IContext result) {
        nodesParsed = 0;
        for (INode node : result.getNodesList()) {
            node.getNodeData().setId("n" + Integer.toString(nodesParsed));
            nodesParsed++;
        }
    }

}
