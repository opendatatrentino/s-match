package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a context in a tab-indented file.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
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
                    //adding to the top of the queue
                    List<INode> childList = curNode.getChildrenList();
                    for (int i = childList.size() - 1; i >= 0; i--) {
                        nodeQ.add(0, childList.get(i));
                    }
                }
            }
        }
        reportStats(context);
    }
}