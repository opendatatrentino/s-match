package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.trees.*;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Base renderer for SimpleXML.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
@SuppressWarnings({"unchecked"})
public class BaseSimpleXMLContextRenderer<E extends IBaseContext<? extends IBaseNode>> extends BaseXMLContextRenderer<E> implements IBaseContextRenderer<E> {

    private static final Logger log = Logger.getLogger(BaseSimpleXMLContextRenderer.class);

    protected void process(E context, BufferedWriter out) throws IOException, ContextRendererException {
        try {
            StreamResult streamResult = new StreamResult(out);
            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler hd = tf.newTransformerHandler();
            Transformer serializer = hd.getTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            hd.setResult(streamResult);
            hd.startDocument();
            hd.startElement("", "", "context", new AttributesImpl());

            if (null == context.getRoot()) {
                final String errMessage = "Cannot render context without root node";
                log.error(errMessage);
                throw new ContextRendererException(errMessage);
            }

            renderNode(hd, context.getRoot());

            hd.endElement("", "", "context");
            hd.endDocument();
        } catch (SAXException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextRendererException(errMessage, e);
        } catch (TransformerConfigurationException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextRendererException(errMessage, e);
        }
    }

    protected void renderNode(TransformerHandler hd, IBaseNode curNode) throws SAXException {
        // render current node
        IBaseNodeData curNodeData = curNode.getNodeData();
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", "id", "CDATA", curNodeData.getId());
        if (curNode.hasParent()) {
            atts.addAttribute("", "", "parent-id", "CDATA", curNode.getParent().getNodeData().getId());
        }
        renderNodeAttributes(curNode, atts);
        hd.startElement("", "", "node", atts);

        renderString(hd, new AttributesImpl(), "name", curNodeData.getName());

        renderNodeContents(curNode, hd);

        if (0 < curNode.getChildCount()) {
            hd.startElement("", "", "children", new AttributesImpl());
            Iterator<INode> children;
            if (sort) {
                ArrayList<INode> childrenList = new ArrayList<INode>(curNode.getChildrenList());
                Collections.sort(childrenList, Node.NODE_NAME_COMPARATOR);
                children = childrenList.iterator();
            } else {
                children = curNode.getChildren();
            }
            while (children.hasNext()) {
                renderNode(hd, children.next());
            }
            hd.endElement("", "", "children");
        }

        hd.endElement("", "", "node");
        reportProgress();
    }

    protected void renderNodeContents(IBaseNode curNode, TransformerHandler hd) throws SAXException {
    }

    protected void renderNodeAttributes(IBaseNode curNode, AttributesImpl atts) {
    }

    public String getDescription() {
        return ILoader.XML_FILES;
    }
}
