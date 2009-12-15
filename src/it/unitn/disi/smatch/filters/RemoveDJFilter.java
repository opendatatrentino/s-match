package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * Removes DJ links from the mapping.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RemoveDJFilter implements IFilter {

    private static final Logger log = Logger.getLogger(RedundantFilter.class);

    public IMatchMatrix filter(Vector args) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering started...");
        }
        long start = System.currentTimeMillis();

        //String fileName = (String) args.get(0);
        IMatchMatrix CnodMatrix = (IMatchMatrix) args.get(1);
        //IMatchMatrix ClabMatrix = (IMatchMatrix) args.get(2);
        IContext sourceContext = (IContext) args.get(3);
        IContext targetContext = (IContext) args.get(4);

        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();

        long counter = 0;
        long total = (long) sourceNodes.size() * (long) targetNodes.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //check each mapping
        for (int i = 0; i < CnodMatrix.getX(); i++) {
            for (int j = 0; j < CnodMatrix.getY(); j++) {
                if (MatchManager.OPPOSITE_MEANING == CnodMatrix.getElement(i, j)) {
                    CnodMatrix.setElement(i, j, MatchManager.IDK_RELATION);
                }

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
            }
        }

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering finished: " + (System.currentTimeMillis() - start) + " ms");
        }
        return CnodMatrix;
    }
}
