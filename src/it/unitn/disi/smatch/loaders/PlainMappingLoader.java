package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

/**
 * Loads only mapping part, as written by PlainRenderer.java
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PlainMappingLoader implements IMappingLoader {

    private static final Logger log = Logger.getLogger(PlainMappingLoader.class);

    public IMatchMatrix loadMapping(IContext ctxSource, IContext ctxTarget, String fileName) throws IOException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading mapping: " + fileName);
        }

        Vector<INode> sourceNodes = ctxSource.getAllNodes();
        Vector<INode> targetNodes = ctxTarget.getAllNodes();

        if (log.isEnabledFor(Level.INFO)) {
            log.info(sourceNodes.size() + " x " + targetNodes.size() + " nodes");
        }

        IMatchMatrix cNodeMatrix = MatrixFactory.getInstance(sourceNodes.size(), targetNodes.size());

        HashMap<String, Integer> sNodes = createHash(sourceNodes);
        HashMap<String, Integer> tNodes = createHash(targetNodes);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        String line;
        int cnt = 0;
        int cntLoaded = 0;
        int lg = 0;
        int mg = 0;
        int eq = 0;
        int dj = 0;
        while ((line = reader.readLine()) != null &&
                !line.startsWith("#") &&
                !line.equals("")) {

            int sourceIdx;
            int targetIdx;
            char rel;

            String[] tokens = line.split("\t");
            if (3 != tokens.length) {
                if (log.isEnabledFor(Level.WARN)) {
                    log.warn("Unrecognized mapping format: " + line);
                }
            } else {
                //tokens = left \t relation \t right
                rel = tokens[1].toCharArray()[0];
                switch (rel) {
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

                sourceIdx = -1;
                if (!sNodes.containsKey(tokens[0])) {
                    if (log.isEnabledFor(Level.WARN)) {
                        log.warn("Could not find source node: " + tokens[0]);
                    }
                } else {
                    sourceIdx = sNodes.get(tokens[0]);
                }

                targetIdx = -1;
                if (!tNodes.containsKey(tokens[2])) {
                    if (log.isEnabledFor(Level.WARN)) {
                        log.warn("Could not find target node: " + tokens[2]);
                    }
                } else {
                    targetIdx = tNodes.get(tokens[2]);
                }

                if ((-1 != sourceIdx) && (-1 != targetIdx)) {
                    cNodeMatrix.setElement(sourceIdx, targetIdx, rel);
                    cntLoaded++;
                } else {
                    if (log.isEnabledFor(Level.WARN)) {
                        log.warn("Could not find mapping: " + line);
                    }
                }
            }
            cnt++;
            if (0 == (cnt % 1000)) {
                if (log.isEnabledFor(Level.INFO)) {
                    log.info("Loaded links: " + cnt);
                }
            }
        }

        if (log.isEnabledFor(Level.INFO)) {
            log.info(cnt);
            log.info("Loading mapping finished. Loaded " + cntLoaded + " relations");
            log.info("LG: " + lg);
            log.info("MG: " + mg);
            log.info("EQ: " + eq);
            log.info("DJ: " + dj);
        }
        return cNodeMatrix;
    }

    public String getNodePath(INode node) {
        String result = "";
        INode sparent = node;
        while (null != sparent) {
            result = "\\" + sparent.getNodeName().replaceAll("\\\\", "/") + result;
            sparent = sparent.getParent();
        }

        return result;
    }

    public HashMap<String, Integer> createHash(Vector<INode> nodes) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Creating hash for " + nodes.size() + " nodes...");
        }

        HashMap<String, Integer> result = new HashMap<String, Integer>(nodes.size());

        for (int i = 0; i < nodes.size(); i++) {
            result.put(getNodePath(nodes.get(i)), i);
        }

        return result;
    }
}
