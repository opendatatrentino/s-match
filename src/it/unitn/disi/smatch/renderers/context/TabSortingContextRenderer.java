package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.loaders.ILoader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Renders a context in a tab-indented file sorting the nodes by their labels.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TabSortingContextRenderer extends BaseFileContextRenderer {

    private static final Comparator<INode> nodeComparator = new Comparator<INode>() {
        public int compare(INode e1, INode e2) {
            return e1.getNodeData().getName().compareTo(e2.getNodeData().getName());
        }
    };

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
                    //adding to the top of the queue
                    List<INode> childList = new ArrayList<INode>(curNode.getChildrenList());
                    Collections.sort(childList, nodeComparator);
                    for (int i = childList.size() - 1; i >= 0; i--) {
                        nodeQ.add(0, childList.get(i));
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