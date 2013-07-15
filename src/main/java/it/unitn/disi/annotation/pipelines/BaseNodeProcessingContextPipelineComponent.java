package it.unitn.disi.annotation.pipelines;

import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IBaseNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Component which processes contexts node by node in DFS order.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class BaseNodeProcessingContextPipelineComponent extends BaseContextPipelineComponent<IBaseNode> {

    @SuppressWarnings("unchecked")
    public void process(IBaseContext<IBaseNode> instance) throws PipelineComponentException {
        //go DFS, processing node-by-node, keeping path-to-root as context
        ArrayList<IBaseNode> queue = new ArrayList<IBaseNode>();
        ArrayList<IBaseNode> pathToRoot = new ArrayList<IBaseNode>();
        queue.add(instance.getRoot());

        while (!queue.isEmpty()) {
            IBaseNode currentNode = queue.remove(0);
            if (null == currentNode) {
                pathToRoot.remove(pathToRoot.size() - 1);
            } else {
                processNode(currentNode, pathToRoot);

                List<IBaseNode> children = currentNode.getChildrenList();
                if (0 < children.size()) {
                    queue.add(0, null);
                    pathToRoot.add(currentNode);
                }
                for (int i = children.size() - 1; i >= 0; i--) {
                    queue.add(0, children.get(i));
                }
            }
        }
    }

    protected abstract void processNode(IBaseNode currentNode, ArrayList<IBaseNode> pathToRoot);
}