package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.trees.INodeData;
import it.unitn.disi.smatch.data.trees.Node;
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
 * Renders a context into an XML file.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SimpleXMLContextRenderer extends BaseXMLContextRenderer {

    private static final Logger log = Logger.getLogger(SimpleXMLContextRenderer.class);

    protected void process(IContext context, BufferedWriter out) throws IOException, ContextRendererException {
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

    private void renderNode(TransformerHandler hd, INode curNode) throws SAXException {
        // render current node
        INodeData curNodeData = curNode.getNodeData();
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", "id", "CDATA", curNodeData.getId());
        if (curNode.hasParent()) {
            atts.addAttribute("", "", "parent-id", "CDATA", curNode.getParent().getNodeData().getId());
        }
        final String preprocessedFlag = Boolean.toString(true);
        if (curNodeData.getIsPreprocessed()) {
            atts.addAttribute("", "", "preprocessed", "CDATA", preprocessedFlag);
        }
        hd.startElement("", "", "node", atts);

        renderString(hd, new AttributesImpl(), "name", curNodeData.getName());
        renderString(hd, new AttributesImpl(), "label-formula", curNodeData.getcLabFormula());
        renderString(hd, new AttributesImpl(), "node-formula", curNodeData.getcNodeFormula());
        renderString(hd, new AttributesImpl(), "provenance", curNodeData.getProvenance());

        // senses
        if (0 < curNodeData.getACoLCount()) {
            hd.startElement("", "", "tokens", new AttributesImpl());
            for (Iterator<IAtomicConceptOfLabel> it = curNodeData.getACoLs(); it.hasNext();) {
                IAtomicConceptOfLabel acol = it.next();
                atts = new AttributesImpl();
                atts.addAttribute("", "", "id", "CDATA", Integer.toString(acol.getId()));
                hd.startElement("", "", "token", atts);

                renderString(hd, new AttributesImpl(), "text", acol.getToken());
                renderString(hd, new AttributesImpl(), "lemma", acol.getLemma());

                hd.startElement("", "", "senses", new AttributesImpl());
                for (Iterator<ISense> i = acol.getSenses(); i.hasNext();) {
                    ISense sense = i.next();
                    atts = new AttributesImpl();
                    atts.addAttribute("", "", "pos", "CDATA", Character.toString(sense.getPos()));
                    atts.addAttribute("", "", "id", "CDATA", Long.toString(sense.getId()));
                    hd.startElement("", "", "sense", atts);
                    hd.endElement("", "", "sense");
                }
                hd.endElement("", "", "senses");

                hd.endElement("", "", "token");
            }
            hd.endElement("", "", "tokens");
        }

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

    public String getDescription() {
        return ILoader.XML_FILES;
    }
}