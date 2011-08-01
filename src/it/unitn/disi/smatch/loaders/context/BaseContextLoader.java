package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Logger;

/**
 * Base class for loaders.
 *
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public abstract class BaseContextLoader extends Configurable implements IContextLoader {

    private static final Logger log = Logger.getLogger(BaseContextLoader.class);

    protected int nodesParsed = 0;

    protected void createIds(IContext result) {
        log.debug("Creating ids for context...");
        nodesParsed = 0;
        for (INode node : result.getNodesList()) {
            node.getNodeData().setId("n" + Integer.toString(nodesParsed));
            nodesParsed++;
        }
    }

}
