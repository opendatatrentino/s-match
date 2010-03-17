package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.util.*;

/**
 * Parses CTXML files and serves as a file filter for .xml files.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CTXML extends DefaultHandler implements java.io.FileFilter {

    private static final Logger log = Logger.getLogger(CTXMLLoader.class);

    //context
    protected IContext ctx = null;
    protected XMLReader parser = null;

    /**
     * Default parser name.
     */
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    //variables used in parsing
    private String content = "";
    private String lNodeName = "";
    private String rNodeName = "";
    private boolean pof = false;
    private boolean readElementContent = false;
    private boolean baseNode = false;
    private String elementName = null;
    private INode node = null;
    private INode pofNode = null;
    private Vector<INode> conceptsList = new Vector<INode>();
    private IAtomicConceptOfLabel sense = AtomicConceptOfLabel.getInstance();

    //replacements for valid xml
    //TODO should be done by parser?
    private final static String e_commercial = "&amp;";
    private final static String a_grave = "&#224;";
    private final static String e_grave = "&#232;";
    private final static String i_grave = "&#236;";
    private final static String o_grave = "&#242;";
    private final static String u_grave = "&#249;";
    private final static String a_GRAVE = "&#192;";
    private final static String e_GRAVE = "&#200;";
    private final static String minor = "&lt;";

    private int elementsParsed = 0;

    private Hashtable<INode, INode> parentNodes = new Hashtable<INode, INode>();
    private Hashtable<INode, Vector<INode>> childNodes = new Hashtable<INode, Vector<INode>>();
    int debug = 0;

    private final static HashSet<String> allowedElements = new HashSet<String>();

    static {
        allowedElements.add("cLabFormula");
        allowedElements.add("logicalFormulaRepresentation");
        allowedElements.add("idToken");
        allowedElements.add("token");
        allowedElements.add("lemma");
        allowedElements.add("PoS");
        allowedElements.add("wSenses");
        allowedElements.add("relationSource");
        allowedElements.add("relationTarget");
        allowedElements.add("relationTargetNotRecursive");
    }

    public static CTXML getInstance() {
        return new CTXML();
    }

    public CTXML() {
        // SAX parser initialization and creation
        try {
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);

            // Tells to the parser to validate with respect to an XML-Schema instead
            // of a DTD
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);

            // Tells to the parser to validate the XML instance files before parsing
            //parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://xml.org/sax/features/validation", false);

        } catch (SAXException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("SAXException: " + e.getMessage(), e);
            }
        }
    }

    // Starting of re implementation of method from org.xml.sax.helpers.DefaulHandler
    public void startDocument() {
        ctx = Context.getInstance();
        elementsParsed = 1;
    }

    public void startElement(String namespace, String localName, String qName, Attributes atts) {
        elementsParsed++;
        if (elementsParsed % 10000 == 0) {
            log.info("elements parsed: " + elementsParsed);
        }

        //context tag found
        if (localName.equals("context")) {
            for (int i = 0; i < atts.getLength(); i++) {
                String attributeName = atts.getLocalName(i);
                if (attributeName.equals("noNamespaceSchemaLocation"))
                    ctx.getContextData().setSchemaLocation(atts.getValue(i));
            }
        }

        //header tag found
        if (localName.equals("ctxHeader")) {
            for (int i = 0; i < atts.getLength(); i++) {
                String attributeName = atts.getLocalName(i);
                if (attributeName.equals("ctxId"))
                    try {
                        ctx.getContextData().setCtxId(atts.getValue(i));
                    } catch (Exception e) {
                        if (log.isEnabledFor(Level.ERROR)) {
                            log.error("Exception: " + e.getMessage(), e);
                        }
                    }
                if (attributeName.equals("label")) {
                    ctx.getContextData().setLabel(atts.getValue(i));
                }
                if (attributeName.equals("status"))
                    ctx.getContextData().setStatus(atts.getValue(i));
            }
        }

        //metadata handling
        if (localName.equals("agentId") ||
                localName.equals("groupId") ||
                localName.equals("accessRights") ||
                localName.equals("encription") ||
                localName.equals("description") ||
                isTableOfSenseElement(localName)) {
            elementName = localName;
            readElementContent = true;
        }

        //content tag found
        if (localName.equals("ctxContent")) {
            try {
                ctx.getContextData().setLanguage(atts.getValue("language"));
            } catch (Exception e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Exception: " + e.getMessage(), e);
                }
            }
        }

        //schema tag found
        if (localName.equals("schema")) {
            ctx.getContextData().setNamespace(atts.getValue("reservedNameSpaceTag--ctxTag"));
        }

        //Concept tag found
        if (localName.equals("complexType-Concept")) {
            rNodeName = atts.getValue("name");
            if (!rNodeName.equals(Context.BASE_NODE)) {
                node = Node.getInstance();
                rNodeName = xmlTagDecode(rNodeName);
                node.getNodeData().setNodeUniqueName(rNodeName);
            } else
                baseNode = true;
        }

        //sense tag found
        if (localName.equals("sense")) {
            sense = AtomicConceptOfLabel.getInstance();
        }
        if (localName.equals("extension")) {
            lNodeName = atts.getValue("base");
            lNodeName = xmlTagDecode(lNodeName);
            if (!lNodeName.equals(Context.BASE_NODE)) {
                INode parentNode = Node.getInstance();
                parentNode.getNodeData().setNodeUniqueName(lNodeName);
                node.getNodeData().setParent(parentNode);
            }
        }
    }

    //dealing with the closing tags
    public void endElement(String uri, String localName, String qName) {
        if (content != null && !content.equals("")) {
            processElementContent();
            content = "";
        }
        if (localName.equals("sense")) {
            node.getNodeData().addAtomicConceptOfLabel(sense);
            sense = AtomicConceptOfLabel.getInstance();
        }
        if (localName.equals("element") && pof) {
            conceptsList.add(pofNode);
            pofNode = Node.getInstance();
            pof = false;
        }
        if (!pof && (localName.equals("complexType-Concept") || localName.equals("element"))) {
            if (baseNode)
                baseNode = false;
            else {
                if (node != null)
                    conceptsList.add(node);
            }
            rNodeName = "";
            lNodeName = "";
            node = Node.getInstance();
            node = null;
        }
    }

    public void characters(char[] ch, int start, int length) {
        String localContent = new String(ch, start, length);
        content += localContent;
    }

    //filling context object with information
    private void processElementContent() {
        content = content.trim();
        if (content.equals(" "))
            content = "";
        if (null != elementName && readElementContent && null != content) {
            /// Context attributes
            if (elementName.equals("agentId"))
                ctx.getContextData().setOwner(content);
            if (elementName.equals("groupId"))
                ctx.getContextData().setGroup(content);
            if (elementName.equals("accessRights"))
                ctx.getContextData().setSecurityAccessRights(content);
            if (elementName.equals("encription"))
                ctx.getContextData().setSecurityEncription(content);
            if (elementName.equals("description")) {
                content = xmlTagDecode(content);
                ctx.getContextData().setDescription(content);
            }
            /// Table of senses elements
            if (elementName.equals("logicalFormulaRepresentation")) {
                node.getNodeData().setcNodeFormula(xmlTagDecode(content.trim()));
            }
            if (elementName.equals("cLabFormula")) {
                node.getNodeData().setcLabFormula(xmlTagDecode(content.trim()));
            }
            if (elementName.equals("idToken")) {
                sense.setIdToken(Integer.parseInt(content));
            }
            if (elementName.equals("token"))
                sense.setToken(content);
            if (elementName.equals("lemma"))
                sense.setLemma(content);
            if (elementName.equals("PoS"))
                sense.setPos(content);
            if (elementName.equals("wSenses")) {
                if (-1 < content.indexOf("#")) {
                    sense.addSenses(extractSenseList(content.trim()));
                }
            }
            readElementContent = false;
            elementName = "";
        }
    }

    //Building senses set
    private static Vector<String> extractSenseList(String list) {
        list = list.trim();
        Vector<String> result = new Vector<String>();
        StringTokenizer extractSenses = new StringTokenizer(list, " ");
        while (extractSenses.hasMoreTokens()) {
            String wSense = extractSenses.nextToken();
            //TODO added by autayeu to allow icon25.xml to run
            if ("navr".indexOf(wSense.charAt(0)) >= 0 && wSense.charAt(1) == '#') {
                result.add(wSense);
            }
        }
        return result;
    }

    public void endDocument() {
        // Creation of the context data from the list of concepts stored
        // in conceptsList. We must merge the information about the concepts interconnection
        // with the information about the attributes/setOfSenses of the
        // different concepts.

        log.debug("Finding root...");
        INode root = findRoot();
        if (null == root) {
            log.warn("Context root is not found");
            return;
        }

        ctx.setRoot(root);
        log.debug("Filling hashes...");

        //----------------------------------------------------------
        for (INode nodeInList : conceptsList) {
            INode parentNode = nodeInList.getParent();
            if (null != parentNode) {
                parentNodes.put(nodeInList, parentNode);
            }
        }

        Enumeration keys = parentNodes.keys();
        while (keys.hasMoreElements()) {
            INode child = (INode) keys.nextElement();
            INode parentNode = parentNodes.get(child);
            Vector<INode> vec = childNodes.get(parentNode);
            if (vec == null) {
                vec = new Vector<INode>();
                vec.add(child);
                childNodes.put(parentNode, vec);
            } else {
                vec.add(child);
                childNodes.put(parentNode, vec);
            }
        }
        log.debug("Building hierarchy structure...");
        //-----------------------------------------------------
        buildHierarchyStructure(root);
        log.debug("Traversing hierarchy...");
        traverseHierarchy(root);
        parentNodes = new Hashtable<INode, INode>();
        childNodes = new Hashtable<INode, Vector<INode>>();
    }

    // End of reimplementation of method from org.xml.sax.helpers.DefaulHandler
    protected void resetGlobalElements() {
        content = "";
        lNodeName = "";
        rNodeName = "";
        pof = false;
        readElementContent = false;
        baseNode = false;
        elementName = null;
        node = null;
        pofNode = null;
        conceptsList = new Vector<INode>();
        sense = AtomicConceptOfLabel.getInstance();
        this.ctx = Context.getInstance();
    }

    //create context object from ctxml file
    public IContext parseAndLoadContext(String inputFileName) throws SMatchException {
        resetGlobalElements();
        try {
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
            parser.setContentHandler(this);
            BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF-8"));
            InputSource is = new InputSource(inputFile);
            parser.parse(is);
        } catch (SAXException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        } catch (FileNotFoundException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        } catch (UnsupportedEncodingException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        } catch (IOException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
        return ctx;
    }

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

    //consolidating knowledge about concept
    private void traverseHierarchy(INode node) {
        Vector<INode> children = node.getChildren();
        if (null == children) {
            return;
        }
        for (INode child : children) {
            buildHierarchyStructure(child);
            traverseHierarchy(child);
        }
    }

    private void buildHierarchyStructure(INode node) {
        Vector<INode> vec = childNodes.get(node);
        if (null != vec) {
            for (INode child : vec) {
                node.addChild(child);
                child.getNodeData().setParent(node);
            }
        }
    }

    private static boolean isTableOfSenseElement(String attribute) {
        return allowedElements.contains(attribute);
    }

    public static String xmlTagEncode(String xmlToEncode) {
        if (null == xmlToEncode) {
            return null;
        }
        StringBuilder out = new StringBuilder(xmlToEncode.length());
        for (int t = 0; t < xmlToEncode.length(); t++) {
            char c = xmlToEncode.charAt(t);
            switch (c) {
                case ('&'):
                    out.append(e_commercial);
                    break;
                case ('à'):
                    out.append(a_grave);
                    break;
                case ('À'):
                    out.append(a_GRAVE);
                    break;
                case ('è'):
                    out.append(e_grave);
                    break;
                case ('È'):
                    out.append(e_GRAVE);
                    break;
                case ('ò'):
                    out.append(o_grave);
                    break;
                case ('ù'):
                    out.append(u_grave);
                    break;
                case ('ì'):
                    out.append(i_grave);
                    break;
                case ('<'):
                    out.append(minor);
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    private static String xmlTagDecode(String xmlToDecode) {
        if (xmlToDecode == null) {
            return null;
        }
        String out = xmlToDecode;
        out = out.replaceAll(e_commercial, "&");
        out = out.replaceAll(a_grave, "à");
        out = out.replaceAll(a_GRAVE, "À");
        out = out.replaceAll(e_grave, "è");
        out = out.replaceAll(e_GRAVE, "È");
        out = out.replaceAll(o_grave, "ò");
        out = out.replaceAll(u_grave, "ù");
        out = out.replaceAll(i_grave, "ì");
        out = out.replaceAll(minor, "<");
        return out;
    }

    //Xml File Filter routine
    public boolean accept(File f) {
        return null != f.getName() && f.getName().endsWith(".xml");
    }
}