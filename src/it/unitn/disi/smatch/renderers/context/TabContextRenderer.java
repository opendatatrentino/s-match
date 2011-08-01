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
 * Renders a context in a tab-indented file.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TabContextRenderer extends BaseFileContextRenderer {

    protected void process(IContext context, BufferedWriter out) throws IOException, ContextRendererException {
        ArrayList<INode> nodeQ = new ArrayList<INode>();
        String level = "";
        nodeQ.add(context.getRoot());
        INode curNode;
        String line;
        while (!nodeQ.isEmpty()) {
            curNode = nodeQ.remove(0);
            if (null == curNode) {
                level = level.substring(1);
            } else {
                line = level + curNode.getNodeData().getName() + "\n";
                out.write(line);
                reportProgress();

                if (curNode.getChildCount() > 0) {
                    level = level + "\t";
                    nodeQ.add(0, null);
                    Iterator<INode> children;
                    if (sort) {
                        ArrayList<INode> childrenList = new ArrayList<INode>(curNode.getChildrenList());
                        Collections.sort(childrenList, Node.NODE_NAME_COMPARATOR);
                        children = childrenList.iterator();
                    } else {
                        children = curNode.getChildren();
                    }
                    int idx = 0;
                    //adding to the top of the queue
                    while (children.hasNext()) {
                        nodeQ.add(idx, children.next());
                        idx++;
                    }
                }
            }
        }
        reportStats(context);
    }

    public String getDescription() {
        return ILoader.TXT_FILES;
    }
}