package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.smatch.data.trees.Context;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.File;
import java.util.Properties;
import java.util.Set;

/**
 * Loads a context from an ontology OWL API and HermiT reasoner.
 * Takes in a topClass parameter, which defines the starting class. If not specified, the Thing will be used.
 * Takes in an excludeNothing parameter, which specifies whether to exclude Nothing class. Default true.
 * Takes in a replaceUnderscore parameter, which specifies whether to replace _ in class names. Default true.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class OWLContextLoader extends BaseContextLoader<IContext> implements IContextLoader {

    private static final Logger log = Logger.getLogger(OWLContextLoader.class);

    // class to start from, if not specified, a Thing will be used
    private static final String TOP_CLASS_KEY = "topClass";
    private String topClass = null;

    // whether to exclude Nothing class
    private static final String EXCLUDE_NOTHING_KEY = "excludeNothing";
    private boolean excludeNothing = true;

    // whether to replace _
    private static final String REPLACE_UNDERSCORE_KEY = "replaceUnderscore";
    private boolean replaceUnderscore = true;


    private static OWLClass NOTHING_CLASS = OWLManager.getOWLDataFactory().getOWLClass(OWLRDFVocabulary.OWL_NOTHING.getIRI());

    /**
     * <p>Simple visitor that grabs any labels on an entity.</p>
     * <p/>
     * Author: Sean Bechhofer<br>
     * The University Of Manchester<br>
     * Information Management Group<br>
     * Date: 17-03-2007<br>
     * <br>
     */
    private class LabelExtractor implements OWLAnnotationObjectVisitor {

        String result;

        public LabelExtractor() {
            result = null;
        }

        public void visit(OWLAnonymousIndividual individual) {
        }

        public void visit(IRI iri) {
        }

        public void visit(OWLLiteral literal) {
        }

        public void visit(OWLAnnotation annotation) {
            /*
            * If it's a label, grab it as the result. Note that if there are
            * multiple labels, the last one will be used.
            */
            if (annotation.getProperty().isLabel()) {
                OWLLiteral c = (OWLLiteral) annotation.getValue();
                result = c.getLiteral();
            }

        }

        public void visit(OWLAnnotationAssertionAxiom axiom) {
        }

        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        }

        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        }

        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        }

        public void visit(OWLAnnotationProperty property) {
        }

        public void visit(OWLAnnotationValue value) {
        }


        public String getResult() {
            return result;
        }
    }


    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(TOP_CLASS_KEY)) {
                topClass = newProperties.getProperty(TOP_CLASS_KEY);
            }

            if (newProperties.containsKey(EXCLUDE_NOTHING_KEY)) {
                excludeNothing = Boolean.parseBoolean(newProperties.getProperty(EXCLUDE_NOTHING_KEY));
            }

            if (newProperties.containsKey(REPLACE_UNDERSCORE_KEY)) {
                replaceUnderscore = Boolean.parseBoolean(newProperties.getProperty(REPLACE_UNDERSCORE_KEY));
            }
        }
        return result;
    }

    public void buildHierarchy(OWLReasoner reasoner, OWLOntology o, IContext c, INode root, OWLClass clazz) throws OWLException {
        if (reasoner.isSatisfiable(clazz)) {
            if (1 < reasoner.getSuperClasses(clazz, true).getFlattened().size()) {
                if (log.isEnabledFor(Level.WARN)) {
                    log.warn("Multiple superclasses:\t" + clazz.toStringID());
                }
            }
            for (OWLClass childClass : reasoner.getSubClasses(clazz, true).getFlattened()) {
                if (!excludeNothing || !NOTHING_CLASS.equals(childClass)) {
                    if (!childClass.equals(clazz)) {
                        INode childNode = c.createNode(labelFor(o, childClass));
                        childNode.getNodeData().setProvenance(childClass.getIRI().toString());
                        root.addChild(childNode);
                        buildHierarchy(reasoner, o, c, childNode, childClass);
                    } else {
                        if (log.isEnabledFor(Level.WARN)) {
                            log.warn("Subclass equal to class:\t" + clazz.toStringID());
                        }
                    }
                }
            }
        }
    }

    public IContext loadContext(String fileName) throws ContextLoaderException {
        IContext result = new Context();
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            //check fileName whether it is URL or not
            if (!fileName.startsWith("http://") && !fileName.startsWith("file://")) {
                File f = new File(fileName);
                fileName = "file:///" + f.getAbsolutePath().replace('\\', '/');
            }
            IRI iri = IRI.create(fileName);
            OWLOntology o = manager.loadOntologyFromOntologyDocument(iri);

            OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
            OWLReasoner reasoner = reasonerFactory.createReasoner(o);
            reasoner.precomputeInferences();

            OWLClass top = null;
            if (null != topClass) {
                if (-1 == topClass.indexOf('#')) {
                    top = manager.getOWLDataFactory().getOWLClass(IRI.create(o.getOntologyID().getOntologyIRI() + "#" + topClass));
                } else {
                    top = manager.getOWLDataFactory().getOWLClass(IRI.create(topClass));
                }
            }
            if (null == top) {
                IRI classIRI = OWLRDFVocabulary.OWL_THING.getIRI();
                top = manager.getOWLDataFactory().getOWLClass(classIRI);
            }

            buildHierarchy(reasoner, o, result, result.createRoot(labelFor(o, top)), top);

            /* Now any unsatisfiable classes */
            for (OWLClass cl : o.getClassesInSignature()) {
                if (!reasoner.isSatisfiable(cl)) {
                    INode node = result.createNode(labelFor(o, cl));
                    node.getNodeData().setProvenance(cl.getIRI().toString());
                    result.getRoot().addChild(node);
                }
            }

            createIds(result);
            log.info("Parsed nodes: " + nodesParsed);
        } catch (OWLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        }

        return result;
    }

    public String getDescription() {
        return ILoader.OWL_FILES;
    }

    public ILoader.LoaderType getType() {
        return ILoader.LoaderType.FILE;
    }


    private String labelFor(OWLOntology ontology, OWLClass clazz) {
        String result;
        LabelExtractor le = new LabelExtractor();
        Set<OWLAnnotation> annotations = clazz.getAnnotations(ontology);
        for (OWLAnnotation anno : annotations) {
            anno.accept(le);
        }
        /* Print out the label if there is one. If not, just use the class URI */
        if (le.getResult() != null) {
            result = le.getResult();
        } else {
            if (null != clazz.getIRI().getFragment() && !clazz.getIRI().getFragment().isEmpty()) {
                result = clazz.getIRI().getFragment();
            } else {
                result = clazz.getIRI().toString();
            }
        }
        if (replaceUnderscore) {
            result = result.replaceAll("_", " ");
        }
        return result;
    }
}