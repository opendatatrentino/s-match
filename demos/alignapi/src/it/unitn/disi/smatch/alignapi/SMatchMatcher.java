package it.unitn.disi.smatch.alignapi;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import java.util.*;

/**
 * Integrates S-Match into AlignAPI. Matches class names, using hierarchy of subClassOf if available.
 * Requires a configuration key "config" to point to S-Match configuration file.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SMatchMatcher extends ObjectAlignment implements AlignmentProcess {

    // parameter for S-Match configuration file
    private static final String CONFIG_KEY = "config";

    public void align(Alignment alignment, Properties param) throws AlignmentException {
        try {
            // get S-Match config file
            if (param.containsKey(CONFIG_KEY)) {
                String configFileName = param.getProperty(CONFIG_KEY);
                IMatchManager mm = MatchManager.getInstance();

                // configure matcher
                mm.setProperties(configFileName);

                IContext c1 = importOntology(mm, ontology1());
                IContext c2 = importOntology(mm, ontology2());

                IMapping result = mm.match(c1, c2);

                // convert mapping
                for (IMappingElement e : result) {
                    Object o1 = e.getSourceNode().getNodeData().getUserObject();
                    Object o2 = e.getTargetNode().getNodeData().getUserObject();
                    if (null != o1 && null != o2) {// in case of introduced Top node, which has no respective object
                        addAlignCell(o1, o2, Character.toString(e.getRelation()), 1.0);
                    }
                }
            } else {
                throw new AlignmentException("Configuration parameter " + CONFIG_KEY + " is missing.");
            }
        } catch (ConfigurableException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            System.err.println(errMessage);
            throw new AlignmentException(errMessage, e);
        } catch (OntowrapException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            System.err.println(errMessage);
            throw new AlignmentException(errMessage, e);
        }
    }

    private IContext importOntology(IMatchManager m, LoadedOntology<Object> ontology) throws OntowrapException, AlignmentException {
        IContext result = m.createContext();
        Map<Object, INode> classNode = new HashMap<Object, INode>();
        for (Object o : ontology.getClasses()) {
            String nodeName = ontology.getEntityName(o);
            nodeName = nodeName.replaceAll("_", " ");//fix for webdirs test
            String nodeId = result.newNode(nodeName, null);
            INode node = result.getNode(nodeId);
            node.setUserObject(o);
            classNode.put(o, node);
        }

        if (ontology instanceof HeavyLoadedOntology) {
            HeavyLoadedOntology hlo = (HeavyLoadedOntology) ontology;

            // create hierarchy
            // TODO rewrite... ended up with kind of a mess here...
            Map<INode, INode> parentNodes = new HashMap<INode, INode>();
            Map<INode, ArrayList<INode>> childNodes = new HashMap<INode, ArrayList<INode>>();

            for (Map.Entry<Object, INode> e : classNode.entrySet()) {
                Object o = e.getKey();
                INode n = e.getValue();
                Set parents = hlo.getSuperClasses(o, OntologyFactory.GLOBAL, OntologyFactory.INHERITED, OntologyFactory.DIRECT);
                if (1 < parents.size()) {
                    throw new AlignmentException("Multiple super classes are unsupported.");
                }
                if (parents.iterator().hasNext()) {
                    INode parent = classNode.get(parents.iterator().next());
                    parentNodes.put(n, parent);
                }
            }

            for (INode child : parentNodes.keySet()) {
                INode parentNode = parentNodes.get(child);
                ArrayList<INode> children = childNodes.get(parentNode);
                if (null == children) {
                    children = new ArrayList<INode>();
                    children.add(child);
                    childNodes.put(parentNode, children);
                } else {
                    children.add(child);
                    childNodes.put(parentNode, children);
                }
            }

            // remove all links
            for (Map.Entry<INode, INode> e : parentNodes.entrySet()) {
                INode child = e.getKey();
                INode parent = e.getValue();

                // remove old children
                List<INode> oldChildren = new ArrayList<INode>(child.getChildren());
                for (INode c : oldChildren) {
                    child.removeChild(c);
                }

                oldChildren = new ArrayList<INode>(parent.getChildren());
                for (INode c : oldChildren) {
                    parent.removeChild(c);
                }

                parent.getNodeData().setParent(null);
                child.getNodeData().setParent(null);
            }

            for (Map.Entry<INode, INode> e : parentNodes.entrySet()) {
                INode child = e.getKey();
                INode parent = e.getValue();

                // add new ones
                List<INode> children = childNodes.get(parent);
                if (null != children) {
                    for (INode c : children) {
                        parent.addChild(c);
                        c.getNodeData().setParent(parent);
                    }
                }

                child.getNodeData().setParent(parent);
            }


            // check multiple roots
            Set<INode> roots = new HashSet<INode>();
            for (Object o : hlo.getClasses()) {
                INode node = classNode.get(o);
                if (node.isRoot()) {
                    roots.add(node);
                }
            }

            INode root;
            if (1 < roots.size()) {
                // create artificial Top node
                String topId = result.newNode("Top", null);
                root = result.getNode(topId);
                // put every other top one under it
                for (INode r : roots) {
                    r.getNodeData().setParent(root);
                }
            } else {
                if (1 == roots.size()) {
                    root = roots.iterator().next();
                    result.setRoot(root);
                } else {
                    throw new AlignmentException("Cannot find even one root class.");
                }
            }

        } else {
            // create artificial Top and put all nodes under it
            String topId = result.newNode("Top", null);
            INode root = result.getNode(topId);
            for (Object o : ontology.getClasses()) {
                INode n = classNode.get(o);
                n.getNodeData().setParent(root);
            }
        }
        return result;
    }
}
