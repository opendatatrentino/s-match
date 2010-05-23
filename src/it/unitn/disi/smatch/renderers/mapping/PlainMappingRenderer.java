package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Renders the mapping in a plain text file.
 * Format: source-node tab relation target-node.
 * Source and target nodes are rendered with \ separating path to root levels.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PlainMappingRenderer extends Configurable implements IMappingRenderer {

    private static final Logger log = Logger.getLogger(PlainMappingRenderer.class);

    public void render(IContextMapping<INode> mapping, String outputFile) throws MappingRendererException {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

            int lg = 0;
            int mg = 0;
            int eq = 0;
            int dj = 0;

            long counter = 0;
            long total = mapping.size();
            long reportInt = (total / 20) + 1;//i.e. report every 5%

            for (IMappingElement<INode> mappingElement : mapping) {
                String sourceConceptName = getNodePathToRoot(mappingElement.getSource());
                String targetConceptName = getNodePathToRoot(mappingElement.getTarget());
                char relation = mappingElement.getRelation();

                out.write(sourceConceptName + "\t" + relation + "\t" + targetConceptName + "\n");
                switch (relation) {
                    case IMappingElement.LESS_GENERAL: {
                        lg++;
                        break;
                    }
                    case IMappingElement.MORE_GENERAL: {
                        mg++;
                        break;
                    }
                    case IMappingElement.EQUIVALENCE: {
                        eq++;
                        break;
                    }
                    case IMappingElement.DISJOINT: {
                        dj++;
                        break;
                    }
                    default:
                        break;
                }

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
            }
            out.close();
            if (log.isEnabledFor(Level.INFO)) {
                log.info("rendered links: " + mapping.size());
                log.info("LG: " + lg);
                log.info("MG: " + mg);
                log.info("EQ: " + eq);
                log.info("DJ: " + dj);
            }

        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MappingRendererException(errMessage, e);
        }
    }

    protected String getNodePathToRoot(INode node) {
        StringBuilder sb = new StringBuilder();
        INode parent = node;
        while (null != parent) {
            if (parent.getNodeData().getName().contains("\\")) {
                log.debug("source: replacing \\ in: " + parent.getNodeData().getName());
                sb.insert(0, "\\" + parent.getNodeData().getName().replaceAll("\\\\", "/"));
            } else {
                sb.insert(0, "\\" + parent.getNodeData().getName());
            }
            parent = parent.getParent();
        }
        return sb.toString();
    }
}
