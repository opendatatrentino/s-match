package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.Context;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.deciders.ISATSolver;
import it.unitn.disi.smatch.loaders.ILoader;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import org.apache.log4j.Logger;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Loader for old CTXML format, remains for compatibility.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 */
public class CTXMLContextLoader extends BaseFileContextLoader<IContext> implements IContextLoader, ContentHandler {

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

    // node unique name -> node
    private HashMap<String, INode> nodes;

    private final static String LINGUISTIC_ORACLE = "oracle";
    private ILinguisticOracle oracle;

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

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(LINGUISTIC_ORACLE)) {
                oracle = (ILinguisticOracle) configureComponent(oracle, oldProperties, newProperties, "linguistic oracle", LINGUISTIC_ORACLE, ISATSolver.class);
            } else {
                final String errMessage = "Cannot find configuration key " + LINGUISTIC_ORACLE;
                log.warn(errMessage);
                oracle = null;
            }
        }
        return result;
    }


    @Override
    protected void createIds(IContext result) {
        //ids should be already in XML
    }

    @Override
    protected IContext process(BufferedReader input) throws IOException, ContextLoaderException {
        try {
            InputSource is = new InputSource(input);
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
        } finally {
            input.close();
        }
        return ctx;
    }

    //org.xml.sax.helpers.DefaultHandler methods re-implementation start

    public void startDocument() {
        ctx = new Context();
        nodes = new HashMap<String, INode>();
        nodesParsed = 0;
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

                    nodesParsed++;
                    if (nodesParsed % 10000 == 0) {
                        log.info("elements parsed: " + nodesParsed);
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
                if (-1 < content.indexOf("#") && null != oracle) {
                    String[] senses = content.toString().trim().split(" ");
                    for (String s : senses) {
                        try {
                            sense.addSense(oracle.createSense(s));
                        } catch (LinguisticOracleException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
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

    public String getDescription() {
        return ILoader.XML_FILES;
    }
}
