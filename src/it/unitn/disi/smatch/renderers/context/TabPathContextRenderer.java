package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.trees.Node;
import it.unitn.disi.smatch.loaders.ILoader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

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
            if (curNode.getChildCount() > 0) {
                Iterator<INode> children;
                if (sort) {
                    ArrayList<INode> childrenList = new ArrayList<INode>(curNode.getChildrenList());
                    Collections.sort(childrenList, Node.NODE_NAME_COMPARATOR);
                    children = childrenList.iterator();
                } else {
                    children = curNode.getChildren();
                }
                while (children.hasNext()) {
                    nodeQ.add(children.next());
                }
            }
        }
        reportStats(context);
    }

    private String getPathToRoot(INode node) {
        StringBuilder result = new StringBuilder(node.getNodeData().getName());
        INode curNode = node.getParent();
        while (null != curNode) {
            result.insert(0, curNode.getNodeData().getName() + "\t");
            curNode = curNode.getParent();
        }
        result.append("\n");
        return result.toString();
    }

    public String getDescription() {
        return ILoader.TXT_FILES;
    }
}