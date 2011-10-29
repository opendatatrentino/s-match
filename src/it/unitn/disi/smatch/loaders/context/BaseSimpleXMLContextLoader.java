package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.smatch.data.trees.BaseContext;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IBaseNode;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Logger;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Properties;

/**
 * Base class for SimpleXML loaders.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
@SuppressWarnings({"unchecked"})
public class BaseSimpleXMLContextLoader<E extends IBaseContext<? extends IBaseNode>> extends BaseFileContextLoader<E> implements IBaseContextLoader<E>, ContentHandler {

    private static final Logger log = Logger.getLogger(BaseSimpleXMLContextLoader.class);

    private static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    protected XMLReader parser;

    // variables used in parsing
    // context being loaded
    protected E ctx;
    // to collect all content in case parser processes element content in several passes
    protected StringBuilder content;
    // path to the root node
    protected Deque<IBaseNode> pathToRoot;

    // flag to output the label being translated in logs
    private final static String UNIQUE_STRINGS_KEY = "uniqueStrings";
    protected boolean uniqueStrings = false;
    protected final HashMap<String, String> unique = new HashMap<String, String>();

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(UNIQUE_STRINGS_KEY)) {
                uniqueStrings = Boolean.parseBoolean(newProperties.getProperty(UNIQUE_STRINGS_KEY));
            }
        }
        return result;
    }

    public BaseSimpleXMLContextLoader() throws ContextLoaderException {
        try {
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
            parser.setContentHandler(this);
            parser.setProperty("http://apache.org/xml/properties/input-buffer-size", 8196);
            pathToRoot = new ArrayDeque<IBaseNode>();
        } catch (SAXException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        }
    }

    @Override
    protected void createIds(E result) {
        //ids should be already in XML
    }

    @Override
    protected E process(BufferedReader input) throws IOException, ContextLoaderException {
        try {
            InputSource is = new InputSource(input);
            parser.parse(is);
            unique.clear();
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

    //org.xml.sax.ContentHandler methods re-implementation start

    public void startDocument() {
        ctx = (E) new BaseContext();
        nodesParsed = 0;
    }

    public void startElement(String namespace, String localName, String qName, Attributes atts) {
        if ("node".equals(localName)) {
            IBaseNode node;
            if (null == ctx.getRoot()) {
                node = ctx.createRoot();
            } else {
                if (0 < pathToRoot.size()) {
                    node = pathToRoot.getLast().createChild();
                } else {
                    // looks like there are multiple roots
                    IBaseNode oldRoot = ctx.getRoot();
                    IBaseNode newRoot = ctx.createRoot("Top");
                    newRoot.addChild(oldRoot);
                    node = newRoot.createChild();
                }
            }
            node.getNodeData().setId(atts.getValue("id"));
            pathToRoot.addLast(node);
        } else {
            content = new StringBuilder();
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if ("name".equals(localName)) {
            pathToRoot.getLast().getNodeData().setName(makeUnique(content.toString()));
        } else if ("node".equals(localName)) {
            pathToRoot.removeLast();

            nodesParsed++;
            if (0 == (nodesParsed % 1000)) {
                log.info("nodes parsed: " + nodesParsed);
            }
        }
    }

    protected String makeUnique(String s) {
        if (uniqueStrings) {
            String result = unique.get(s);
            if (null == result) {
                unique.put(s, s);
                result = s;
            }
            return result;
        } else {
            return s;
        }
    }

    public void characters(char[] ch, int start, int length) {
        content.append(ch, start, length);
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

    public void endDocument() {
        unique.clear();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        //nop
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        //nop
    }

    //org.xml.sax.ContentHandler methods re-implementation end

    public String getDescription() {
        return ILoader.XML_FILES;
    }
}