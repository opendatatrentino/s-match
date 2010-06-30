package it.unitn.disi.smatch.alignapi;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
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

                IContextMapping<INode> result = mm.match(c1, c2);

                // convert mapping
                for (IMappingElement<INode> e : result) {
                    Object o1 = e.getSource().getNodeData().getUserObject();
                    Object o2 = e.getTarget().getNodeData().getUserObject();
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

        // create a node for each class
        for (Object o : ontology.getClasses()) {
            String nodeName = ontology.getEntityName(o);
            Set<String> nodeNames = ontology.getEntityNames(o);//this includes the labels
            if (0 < nodeNames.size()) {
                nodeName = nodeNames.iterator().next();//get the first name
            }
            INode node = result.createNode(nodeName);
            node.setUserObject(o);
            classNode.put(o, node);
        }

        if (ontology instanceof HeavyLoadedOntology) {
            HeavyLoadedOntology hlo = (HeavyLoadedOntology) ontology;

            // create hierarchy
            for (Map.Entry<Object, INode> e : classNode.entrySet()) {
                Object o = e.getKey();
                INode n = e.getValue();
                Set parents = hlo.getSuperClasses(o, OntologyFactory.GLOBAL, OntologyFactory.INHERITED, OntologyFactory.DIRECT);
                if (1 < parents.size()) {
                    throw new AlignmentException("Multiple super classes are unsupported.");
                }
                if (parents.iterator().hasNext()) {
                    INode parent = classNode.get(parents.iterator().next());
                    parent.addChild(n);
                }
            }

            // check multiple roots
            Set<INode> roots = new HashSet<INode>();
            for (Object o : hlo.getClasses()) {
                INode node = classNode.get(o);
                if (!node.hasParent()) {
                    roots.add(node);
                }
            }

            if (1 < roots.size()) {
                // create artificial Top root
                INode root = result.createRoot("Top");
                // put every other top one under it
                for (INode r : roots) {
                    root.addChild(r);
                }
            } else {
                if (1 == roots.size()) {
                    result.setRoot(roots.iterator().next());
                } else {
                    throw new AlignmentException("Cannot find even one root class.");
                }
            }

        } else {
            // create artificial Top and put all nodes under it
            INode root = result.createRoot("Top");
            for (Object o : ontology.getClasses()) {
                INode n = classNode.get(o);
                root.addChild(n);
            }
        }
        return result;
    }
}
