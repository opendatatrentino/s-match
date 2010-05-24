package it.unitn.disi.smatch.loaders.mapping;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.IMappingFactory;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * Loads the mapping as written by {@link it.unitn.disi.smatch.renderers.mapping.PlainMappingRenderer}.
 * <p/>
 * Needs mappingFactory configuration parameter, which should point to an instance of a class implementing
 * {@link it.unitn.disi.smatch.data.mappings.IMappingFactory} interface.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PlainMappingLoader extends Configurable implements IMappingLoader {

    private static final Logger log = Logger.getLogger(PlainMappingLoader.class);

    private static final String MAPPING_FACTORY_KEY = "mappingFactory";
    protected IMappingFactory mappingFactory = null;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(MAPPING_FACTORY_KEY)) {
                mappingFactory = (IMappingFactory) configureComponent(mappingFactory, oldProperties, newProperties, "mapping factory", MAPPING_FACTORY_KEY, IMappingFactory.class);
            } else {
                final String errMessage = "Cannot find configuration key " + MAPPING_FACTORY_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }

    public IContextMapping<INode> loadMapping(IContext source, IContext target, String fileName) throws MappingLoaderException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading mapping: " + fileName);
        }

        IContextMapping<INode> mapping = mappingFactory.getContextMappingInstance(source, target);

        HashMap<String, INode> sNodes = createHash(source);
        HashMap<String, INode> tNodes = createHash(target);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
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

                INode sourceNode;
                INode targetNode;
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

                    sourceNode = sNodes.get(tokens[0]);
                    if (null == sourceNode) {
                        if (log.isEnabledFor(Level.WARN)) {
                            log.warn("Could not find source node: " + tokens[0]);
                        }
                    }

                    targetNode = tNodes.get(tokens[2]);
                    if (!tNodes.containsKey(tokens[2])) {
                        if (log.isEnabledFor(Level.WARN)) {
                            log.warn("Could not find target node: " + tokens[2]);
                        }
                    }

                    if ((null != sourceNode) && (null != targetNode)) {
                        mapping.setRelation(sourceNode, targetNode, rel);
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
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MappingLoaderException(errMessage, e);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                }
            }
        }

        return mapping;
    }

    /**
     * Gets the path of a node from root for hash mapping.
     *
     * @param node the interface of data structure of input node
     * @return the string of the path from root to node
     */
    private String getNodePathToRoot(INode node) {
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

    /**
     * Creates hash map for nodes which contains path from root to node for each node.
     *
     * @param context a context
     * @return a hash table which contains path from root to node for each node
     */
    private HashMap<String, INode> createHash(IContext context) {
        HashMap<String, INode> result = new HashMap<String, INode>();

        int nodeCount = 0;
        for (Iterator<INode> i = context.getRoot().getSubtree(); i.hasNext();) {
            INode node = i.next();
            result.put(getNodePathToRoot(node), node);
            nodeCount++;
        }

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Created hash for " + nodeCount + " nodes...");
        }

        return result;
    }
}
