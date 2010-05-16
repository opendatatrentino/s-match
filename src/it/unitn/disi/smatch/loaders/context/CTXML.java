package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.data.*;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Parses CTXML files and serves as a file filter for .xml files.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CTXML extends DefaultHandler {

    private static final Logger log = Logger.getLogger(CTXMLContextLoader.class);

    protected IContext ctx;

    protected XMLReader parser = null;
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    //variables used in parsing
    private StringBuilder content;
    private INode node;
    private List<INode> conceptsList;
    private IAtomicConceptOfLabel sense;

    private int elementsParsed = 0;

    private HashMap<INode, INode> parentNodes = new HashMap<INode, INode>();
    private HashMap<INode, ArrayList<INode>> childNodes = new HashMap<INode, ArrayList<INode>>();

    public CTXML() throws ContextLoaderException {
        try {
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
            parser.setContentHandler(this);
            parser.setProperty("http://apache.org/xml/properties/input-buffer-size", 8196);
        } catch (SAXException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        }
    }

    public IContext parseAndLoadContext(String inputFileName) throws ContextLoaderException {
        try {
            BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF-8"));
            InputSource is = new InputSource(inputFile);
            parser.parse(is);
        } catch (SAXException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        } catch (FileNotFoundException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        } catch (UnsupportedEncodingException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        }
        return ctx;
    }

    //org.xml.sax.helpers.DefaultHandler methods re-implementation start 

    public void startDocument() {
        ctx = Context.getInstance();
        conceptsList = new ArrayList<INode>();
        elementsParsed = 1;
    }

    public void startElement(String namespace, String localName, String qName, Attributes atts) {
        elementsParsed++;
        if (elementsParsed % 10000 == 0) {
            log.info("elements parsed: " + elementsParsed);
        }

        content = new StringBuilder();

        if ("complexType-Concept".equals(localName)) {
            String nodeName = atts.getValue("name");
            node = Node.getInstance();
            node.getNodeData().setNodeUniqueName(nodeName);
        } else if ("sense".equals(localName)) {
            sense = AtomicConceptOfLabel.getInstance();
        } else if ("extension".equals(localName)) {
            String parentName = atts.getValue("base");
            INode parentNode = Node.getInstance();
            parentNode.getNodeData().setNodeUniqueName(parentName);
            node.getNodeData().setParent(parentNode);
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if (0 < content.length()) {
            if ("logicalFormulaRepresentation".equals(localName)) {
                node.getNodeData().setcNodeFormula(content.toString());
            } else if ("cLabFormula".equals(localName)) {
                node.getNodeData().setcLabFormula(content.toString());
            } else if ("idToken".equals(localName)) {
                sense.setIdToken(Integer.parseInt(content.toString()));
            } else if ("token".equals(localName)) {
                sense.setToken(content.toString());
            } else if ("lemma".equals(localName)) {
                sense.setLemma(content.toString());
            } else if ("wSenses".equals(localName)) {
                if (-1 < content.indexOf("#")) {
                    sense.addSenses(new ArrayList<String>(Arrays.asList(content.toString().trim().split(" "))));
                }
            }
            content = new StringBuilder();
        }
        if ("sense".equals(localName)) {
            node.getNodeData().addAtomicConceptOfLabel(sense);
        }
        if ("complexType-Concept".equals(localName)) {
            if (null != node) {
                conceptsList.add(node);
            }
        }
    }

    public void characters(char[] ch, int start, int length) {
        content.append(ch, start, length);
    }

    public void endDocument() {
        // Creation of the context data from the list of concepts stored
        // in conceptsList. We must merge the information about the concepts interconnection
        // with the information about the attributes/setOfSenses of the
        // different concepts.

        log.debug("Finding root...");
        INode root = findRoot();
        if (null == root) {
            final String errMessage = "Context root is not found";
            log.error(errMessage);
            ctx = null;
            //throw new ContextLoaderException(errMessage);
        } else {
            ctx.setRoot(root);
            log.debug("Filling hashes...");

            for (INode node : conceptsList) {
                INode parentNode = node.getParent();
                if (null != parentNode) {
                    parentNodes.put(node, parentNode);
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
            log.debug("Building hierarchy structure...");
            buildHierarchyStructure(root);
            log.debug("Traversing hierarchy...");
            traverseHierarchy(root);
            parentNodes = new HashMap<INode, INode>();
            childNodes = new HashMap<INode, ArrayList<INode>>();
        }
    }
    //org.xml.sax.helpers.DefaultHandler methods re-implementation end 

    private INode findRoot() {
        for (int i = 0; i < conceptsList.size(); i++) {
            INode node = conceptsList.get(i);
            if (node.isRoot()) {
                boolean realRoot = true;
                for (int j = 0; j < conceptsList.size(); j++) {
                    if (i != j) {
                        INode node2 = (conceptsList.get(j));
                        if (node.equals(node2) && !node2.isRoot()) {
                            realRoot = false;
                            break;
                        }
                    }
                }
                if (realRoot) {
                    return node;
                }
            }
        }
        return null;
    }

    private void traverseHierarchy(INode node) {
        List<INode> children = node.getChildren();
        if (null == children) {
            return;
        }
        for (INode child : children) {
            buildHierarchyStructure(child);
            traverseHierarchy(child);
        }
    }

    private void buildHierarchyStructure(INode node) {
        List<INode> children = childNodes.get(node);
        if (null != children) {
            for (INode child : children) {
                node.addChild(child);
                child.getNodeData().setParent(node);
            }
        }
    }
}