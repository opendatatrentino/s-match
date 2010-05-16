package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.*;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Renders a context into a CTXML file.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CTXMLContextRenderer extends Configurable implements IContextRenderer {

    private static final Logger log = Logger.getLogger(CTXMLContextRenderer.class);

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

                // node queue for BFS traversal
                ArrayList<INode> nodeQ = new ArrayList<INode>();
                if (null == context.getRoot()) {
                    final String errMessage = "Cannot render context without root node";
                    log.error(errMessage);
                    throw new ContextRendererException(errMessage);
                }

                context.getRoot().getNodeData().sort();
                
                nodeQ.add(context.getRoot());
                INode curNode;
                // BFS traversal
                while (!nodeQ.isEmpty()) {
                    curNode = nodeQ.remove(0);
                    for (INode child : curNode.getChildren()) {
                        nodeQ.add(child);
                    }

                    // render current node
                    INodeData curNodeData = curNode.getNodeData();
                    AttributesImpl atts = new AttributesImpl();
                    atts.addAttribute("", "", "name", "CDATA", curNodeData.getNodeUniqueName());
                    hd.startElement("", "", "complexType-Concept", atts);

                    hd.startElement("", "", "cLabFormula", new AttributesImpl());
                    hd.characters(curNodeData.getcLabFormula().toCharArray(), 0, curNodeData.getcLabFormula().length());
                    hd.endElement("", "", "cLabFormula");

                    hd.startElement("", "", "logicalFormulaRepresentation", new AttributesImpl());
                    hd.characters(curNodeData.getCNodeFormula().toCharArray(), 0, curNodeData.getCNodeFormula().length());
                    hd.endElement("", "", "logicalFormulaRepresentation");

                    // senses
                    List<IAtomicConceptOfLabel> setOfSenses = curNodeData.getACoLs();
                    if (0 < setOfSenses.size()) {
                        hd.startElement("", "", "setOfSenses", new AttributesImpl());
                        for (IAtomicConceptOfLabel sense : setOfSenses) {
                            hd.startElement("", "", "sense", new AttributesImpl());

                            hd.startElement("", "", "idToken", new AttributesImpl());
                            String idToken = Integer.toString(sense.getIdToken());
                            hd.characters(idToken.toCharArray(), 0, idToken.length());
                            hd.endElement("", "", "idToken");

                            hd.startElement("", "", "token", new AttributesImpl());
                            hd.characters(sense.getToken().toCharArray(), 0, sense.getToken().length());
                            hd.endElement("", "", "token");

                            hd.startElement("", "", "lemma", new AttributesImpl());
                            hd.characters(sense.getLemma().toCharArray(), 0, sense.getLemma().length());
                            hd.endElement("", "", "lemma");

                            hd.startElement("", "", "wSenses", new AttributesImpl());
                            List<String> wnSenses = sense.getSenses().getSenseList();
                            StringBuilder sensesBuilder = new StringBuilder();
                            for (String senseId : wnSenses) {
                                sensesBuilder.append(senseId).append(" ");
                            }
                            String sensesString = sensesBuilder.toString().trim();
                            hd.characters(sensesString.toCharArray(), 0, sensesString.length());
                            hd.endElement("", "", "wSenses");

                            hd.endElement("", "", "sense");
                        }
                        hd.endElement("", "", "setOfSenses");
                    }

                    if (!curNode.isRoot()) {
                        atts = new AttributesImpl();
                        atts.addAttribute("", "", "base", "CDATA", curNode.getParent().getNodeData().getNodeUniqueName());
                        hd.startElement("", "", "extension", atts);
                        hd.endElement("", "", "extension");
                    }
                    hd.endElement("", "", "complexType-Concept");
                }

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
}