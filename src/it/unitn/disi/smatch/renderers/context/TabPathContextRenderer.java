package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.loaders.ILoader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Renders a context in a tab-separated file, one line per path to root.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TabPathContextRenderer extends BaseFileContextRenderer {

    protected void process(IContext context, BufferedWriter out) throws IOException, ContextRendererException {
        ArrayList<INode> nodeQ = new ArrayList<INode>();
        nodeQ.add(context.getRoot());
        INode curNode;
        while (!nodeQ.isEmpty()) {
            curNode = nodeQ.remove(0);
            if (0 == curNode.getChildCount()) {
                out.write(getPathToRoot(curNode));
            }
            reportProgress();
            nodeQ.addAll(curNode.getChildrenList());
        }
        reportStats(context);
    }

    private String getPathToRoot(INode node) {
        StringBuilder result = new StringBuilder();
        ArrayList<String> path = new ArrayList<String>();
        INode curNode = node;
        while (null != curNode) {
            path.add(0, curNode.getNodeData().getName());
            curNode = curNode.getParent();
        }
        for (int i = 0; i < path.size(); i++) {
            if (0 == i) {
                result.append(path.get(i));
            } else {
                result.append("\t").append(path.get(i));
            }
        }
        result.append("\n");
        return result.toString();
    }

    public String getDescription() {
        return ILoader.TXT_FILES;
    }
}