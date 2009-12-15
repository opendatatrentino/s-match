package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

/**
 * Writes only mapping part, that is cNod matrix. Sorts it by relation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PlainSortedRenderer implements IMappingRenderer {

    private static final Logger log = Logger.getLogger(PlainSortedRenderer.class);

    public IMapping render(Vector args) {
        String fileName = (String) args.get(0);
        IMatchMatrix CnodMatrix = (IMatchMatrix) args.get(1);
        IMatchMatrix ClabMatrix = (IMatchMatrix) args.get(2);
        IContext sourceContext = (IContext) args.get(3);
        IContext targetContext = (IContext) args.get(4);

        //get all ACoLs in contexts
        Vector<IAtomicConceptOfLabel> sourceACoLs = sourceContext.getMatchingContext().getAllContextACoLs();
        Vector<IAtomicConceptOfLabel> targetACoLs = targetContext.getMatchingContext().getAllContextACoLs();

        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();
        printMatricesToFile(fileName, ClabMatrix, CnodMatrix,
                sourceACoLs, targetACoLs, sourceNodes, targetNodes);
        return null;
    }

    /**
     * Prints cLab and cNode Matrices into file
     *
     * @param fileName   File name
     * @param ClabMatrix cLabMatrix
     * @param CnodMatrix cNodeMatrix
     */
    protected void printMatricesToFile(String fileName, IMatchMatrix ClabMatrix, IMatchMatrix CnodMatrix,
                                       Vector<IAtomicConceptOfLabel> sourceACoLs, Vector<IAtomicConceptOfLabel> targetACoLs,
                                       Vector<INode> sourceNodes, Vector<INode> targetNodes) {


        try {
            //Output file
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
            /*
            if (null != ClabMatrix) {
				out.write("#Matrix of semantic relations between atomic concepts of labels\n");
				for (int i = 0; i < sourceACoLs.size(); i++) {
					for (int j = 0; j < targetACoLs.size(); j++) {
						if ((ClabMatrix.getElement(i, j) != MatchManager.IDK_RELATION)) {
							out.write(sourceACoLs.get(i).getText()
									+ "\t" + ClabMatrix.getElement(i, j) + "\t"
									+ targetACoLs.get(j).getText()
									+ "\n");
						}
					}
				}
			}
			out.write("\n");
			out.write("#Matrix of semantic relations between concepts at nodes (matching result)\n");
			*/

            int count = 0;
            int lg = 0;
            int mg = 0;
            int eq = 0;
            int dj = 0;


            long total = (long) sourceNodes.size() * (long) targetNodes.size();
            long reportInt = (total / 20) + 1;//i.e. report every 5%

            String sourceConceptName;
            String targetConceptName;
            char[] relations = {MatchManager.OPPOSITE_MEANING, MatchManager.SYNOMYM, MatchManager.LESS_GENERAL_THAN, MatchManager.MORE_GENERAL_THAN};

            for (char relation : relations) {
                long counter = 0;
                boolean newLineNeeded = false;
                if (log.isEnabledFor(Level.INFO)) {
                    log.info("Rendering: " + relation);
                }
                //for every concept in both context
                for (int i = 0; i < sourceNodes.size(); i++) {
                    sourceConceptName = null;

                    for (int j = 0; j < targetNodes.size(); j++) {
                        char resultSat = CnodMatrix.getElement(i, j);
                        if (resultSat != MatchManager.IDK_RELATION && relation == resultSat) {
                            if (null == sourceConceptName) {
                                sourceConceptName = "";
                                INode sparent = sourceNodes.get(i);
                                while (null != sparent) {
                                    if (sparent.getNodeName().contains("\\")) {
                                        log.debug("source: replacing \\ in: " + sparent.getNodeName());
                                        sourceConceptName = "\\" + sparent.getNodeName().replaceAll("\\\\", "/") + sourceConceptName;
                                    } else {
                                        sourceConceptName = "\\" + sparent.getNodeName() + sourceConceptName;
                                    }
                                    sparent = sparent.getParent();
                                }
                            }

                            targetConceptName = "";
                            INode tparent = targetNodes.get(j);
                            while (null != tparent) {
                                if (tparent.getNodeName().contains("\\")) {
                                    log.debug("target: replacing \\ in: " + tparent.getNodeName());
                                    targetConceptName = "\\" + tparent.getNodeName().replaceAll("\\\\", "/") + targetConceptName;
                                } else {
                                    targetConceptName = "\\" + tparent.getNodeName() + targetConceptName;
                                }
                                tparent = tparent.getParent();
                            }

                            out.write(sourceConceptName + "\t" + resultSat + "\t" + targetConceptName + "\n");
                            newLineNeeded = true;
                            count++;
                            switch (resultSat) {
                                case MatchManager.LESS_GENERAL_THAN: {
                                    lg++;
                                    break;
                                }
                                case MatchManager.MORE_GENERAL_THAN: {
                                    mg++;
                                    break;
                                }
                                case MatchManager.SYNOMYM: {
                                    eq++;
                                    break;
                                }
                                case MatchManager.OPPOSITE_MEANING: {
                                    dj++;
                                    break;
                                }
                                default:
                                    break;
                            }
                        }

                        counter++;
                        if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                            log.info(100 * counter / total + "%");
                        }
                    }
                }
                if (newLineNeeded) {
                    out.write("\n");//relation separator
                }
            }
            out.close();
            if (log.isEnabledFor(Level.INFO)) {
                log.info("rendered links: " + count);
                log.info("LG: " + lg);
                log.info("MG: " + mg);
                log.info("EQ: " + eq);
                log.info("DJ: " + dj);
            }
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            //throw new SMatchException(errMessage, e);
        }
    }
}
