package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.Context;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Logger;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Loader for old CTXML format, remains for compatibility.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 */
public class CTXMLContextLoader extends Configurable implements IContextLoader, ContentHandler {

    private static final Logger log = Logger.getLogger(CTXMLContextLoader.class);

    protected IContext ctx;

    protected XMLReader parser;
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    //default name of the base node
    private static final String BASE_NODE = "ctxBaseNode$c0";

    //variables used in parsing
    private StringBuilder content;
    private INode node;
    private IAtomicConceptOfLabel sense;

    private int nodesLoaded = 0;

    // node unique name -> node
    private HashMap<String, INode> nodes;

    public CTXMLContextLoader() throws ContextLoaderException {
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

    public IContext loadContext(String fileName) throws ContextLoaderException {
        try {
            BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            InputSource is = new InputSource(inputFile);
            parser.parse(is);
            log.info("Parsed nodes: " + nodesLoaded);
            if (null == ctx.getRoot()) {
                final String errMessage = "Context root is not found";
                log.error(errMessage);
                throw new ContextLoaderException(errMessage);
            }
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
        ctx = new Context();
        nodes = new HashMap<String, INode>();
        nodesLoaded = 0;
    }

    public void startElement(String namespace, String localName, String qName, Attributes atts) {
        content = new StringBuilder();

        if ("complexType-Concept".equals(localName)) {
            String nodeName = atts.getValue("name");
            if (!nodeName.equals(BASE_NODE)) {
                INode n = nodes.get(nodeName);
                if (null == n) {
                    node = ctx.createNode();
                    setNodeUniqueName(node, nodeName);
                    nodes.put(nodeName, node);

                    nodesLoaded++;
                    if (nodesLoaded % 10000 == 0) {
                        log.info("elements parsed: " + nodesLoaded);
                    }
                }
            }
        } else if ("sense".equals(localName)) {
            sense = node.getNodeData().createACoL();
        } else if ("extension".equals(localName)) {
            String parentName = atts.getValue("base");
            INode parentNode = nodes.get(parentName);
            if (null == parentNode) {
                parentNode = ctx.createNode();
                setNodeUniqueName(parentNode, parentName);
                nodes.put(parentName, parentNode);
            }
            parentNode.addChild(node);
        }
    }

    private void setNodeUniqueName(INode node, String nodeName) {
        StringTokenizer idName = new StringTokenizer(nodeName, "$");
        node.getNodeData().setName(idName.nextToken());
        node.getNodeData().setId(idName.nextToken());
    }

    public void endElement(String uri, String localName, String qName) {
        if (0 < content.length()) {
            if ("logicalFormulaRepresentation".equals(localName)) {
                node.getNodeData().setcNodeFormula(content.toString().trim());
            } else if ("cLabFormula".equals(localName)) {
                node.getNodeData().setcLabFormula(content.toString().trim());
            } else if ("idToken".equals(localName)) {
                sense.setId(Integer.parseInt(content.toString()));
            } else if ("token".equals(localName)) {
                sense.setToken(content.toString());
            } else if ("lemma".equals(localName)) {
                sense.setLemma(content.toString());
            } else if ("wSenses".equals(localName)) {
                if (-1 < content.indexOf("#")) {
                    String[] senses = content.toString().trim().split(" ");
                    for (String s : senses) {
                        sense.createSense(s.charAt(0), Long.parseLong(s.substring(2)));
                    }
                }
            }
            content = new StringBuilder();
        }
        if ("sense".equals(localName)) {
            node.getNodeData().addACoL(sense);
        }
    }

    public void characters(char[] ch, int start, int length) {
        content.append(ch, start, length);
    }

    public void endDocument() {
        log.debug("Finding root...");
        INode root = findRoot();
        ctx.setRoot(root);
        nodes.clear();
    }

    public void setDocumentLocator(Locator locator) {
        //nop
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        //nop
    }

    public void processingInstruction(String target, String data) throws SAXException {
        //nop
    }

    public void skippedEntity(String name) throws SAXException {
        //nop
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        //nop
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        //nop
    }
    //org.xml.sax.helpers.DefaultHandler methods re-implementation end

    private INode findRoot() {
        for (INode node : nodes.values()) {
            if (!node.hasParent() && !BASE_NODE.equals(node.getNodeData().getName())) {
                return node;
            }
        }
        return null;
    }
}
