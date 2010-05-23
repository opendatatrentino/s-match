package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.trees.INodeData;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

/**
 * Renders a context into a XML file.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SimpleXMLContextRenderer extends Configurable implements IContextRenderer {

    private static final Logger log = Logger.getLogger(SimpleXMLContextRenderer.class);

    private int nodesRendered;

    public void render(IContext context, String fileName) throws ContextRendererException {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
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

                nodesRendered = 0;
                renderNode(hd, context.getRoot());
                log.info("Rendered nodes: " + nodesRendered);

                hd.endElement("", "", "context");
                hd.endDocument();
            } finally {
                out.close();
            }
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextRendererException(errMessage, e);
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
        hd.startElement("", "", "node", atts);

        renderString(hd, "name", curNodeData.getName());
        renderString(hd, "label-formula", curNodeData.getcLabFormula());
        renderString(hd, "node-formula", curNodeData.getcNodeFormula());

        // senses
        if (0 < curNodeData.getACoLCount()) {
            hd.startElement("", "", "tokens", new AttributesImpl());
            for (Iterator<IAtomicConceptOfLabel> it = curNodeData.getACoLs(); it.hasNext();) {
                IAtomicConceptOfLabel acol = it.next();
                atts = new AttributesImpl();
                atts.addAttribute("", "", "id", "CDATA", Integer.toString(acol.getId()));
                hd.startElement("", "", "token", atts);

                renderString(hd, "text", acol.getToken());
                renderString(hd, "lemma", acol.getLemma());

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
            for (Iterator<INode> i = curNode.getChildren(); i.hasNext();) {
                renderNode(hd, i.next());
            }
            hd.endElement("", "", "children");
        }

        hd.endElement("", "", "node");
        nodesRendered++;
    }

    private static void renderString(TransformerHandler hd, final String tagName, final String tagValue) throws SAXException {
        if (null != tagValue && 0 < tagValue.length()) {
            hd.startElement("", "", tagName, new AttributesImpl());
            hd.characters(tagValue.toCharArray(), 0, tagValue.length());
            hd.endElement("", "", tagName);
        }
    }
}