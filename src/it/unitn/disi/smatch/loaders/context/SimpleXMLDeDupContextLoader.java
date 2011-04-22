package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.trees.Node;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loader for XML format. Check whether there are duplicates among siblings and leaves only ones of them
 * consolidating children.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SimpleXMLDeDupContextLoader extends SimpleXMLContextLoader {

    private static final Logger log = Logger.getLogger(SimpleXMLDeDupContextLoader.class);

    private long counter;
    private long total;
    private long reportInt;

    public SimpleXMLDeDupContextLoader() throws ContextLoaderException {
        super();
    }

    @Override
    protected IContext process(BufferedReader input) throws IOException, ContextLoaderException {
        IContext result = super.process(input);

        {
            log.info("Checking sibling duplicates...");
            //checking for duplicates among siblings
            int duplicatesRemoved = 0;

            counter = 0;
            total = result.getNodesList().size();
            reportInt = (total / 20) + 1;//i.e. report every 5%

            ArrayList<INode> nodeQ = new ArrayList<INode>();
            nodeQ.add(result.getRoot());
            INode curNode;
            while (!nodeQ.isEmpty()) {
                curNode = nodeQ.remove(0);
                if (null == curNode) {
                    //go up
                } else {
                    List<INode> children = new ArrayList<INode>(curNode.getChildrenList());
                    Collections.sort(children, Node.NODE_NAME_COMPARATOR);
                    int idx = 1;
                    while (idx < children.size()) {
                        if (children.get(idx - 1).getNodeData().getName().equals(children.get(idx).getNodeData().getName())) {
                            log.info("Found duplicate:\t" + children.get(idx).getNodeData().getName());
                            moveChildren(children.get(idx), children.get(idx - 1));
                            curNode.removeChild(children.get(idx));
                            children.remove(idx);
                            duplicatesRemoved++;
                        } else {
                            idx++;
                        }
                    }
                    reportProgress();

                    if (curNode.getChildCount() > 0) {
                        //go down
                        nodeQ.add(0, null);
                        //adding to the top of the queue
                        List<INode> childList = curNode.getChildrenList();
                        for (int i = childList.size() - 1; i >= 0; i--) {
                            nodeQ.add(0, childList.get(i));
                        }
                    }
                }
            }

            log.info("Duplicates removed:\t" + duplicatesRemoved);
        }

        return result;
    }

    /**
     * Move children from <var>source</var> to <var>target</var>.
     *
     * @param source source node
     * @param target target node
     */
    private void moveChildren(INode source, INode target) {
        List<INode> children = new ArrayList<INode>(source.getChildrenList());
        while (0 < children.size()) {
            INode child = children.remove(0);
            int idx = target.getChildIndex(child);
            if (-1 == idx) {
                target.addChild(child);
            } else {
                moveChildren(child, target.getChildAt(idx));
            }
        }
    }

    protected void reportProgress() {
        counter++;
        if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
            log.info(100 * counter / total + "%");
        }
    }

}
