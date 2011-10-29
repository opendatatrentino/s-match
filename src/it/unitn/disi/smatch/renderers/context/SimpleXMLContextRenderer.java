package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.data.trees.IBaseNode;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.trees.INodeData;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.sax.TransformerHandler;
import java.util.Iterator;

/**
 * Renders a context into an XML file.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SimpleXMLContextRenderer extends BaseSimpleXMLContextRenderer<IContext> implements IContextRenderer{

    private final static String preprocessedFlag = Boolean.toString(true);

    protected void renderNodeAttributes(IBaseNode curNode, AttributesImpl atts) {
        INodeData curNodeData = ((INode) curNode).getNodeData();
        if (curNodeData.getIsPreprocessed()) {
            atts.addAttribute("", "", "preprocessed", "CDATA", preprocessedFlag);
        }
    }

    protected void renderNodeContents(IBaseNode curNode, TransformerHandler hd) throws SAXException {
        INodeData curNodeData = ((INode) curNode).getNodeData();
        renderString(hd, new AttributesImpl(), "label-formula", curNodeData.getcLabFormula());
        renderString(hd, new AttributesImpl(), "node-formula", curNodeData.getcNodeFormula());
        renderString(hd, new AttributesImpl(), "provenance", curNodeData.getProvenance());

        // senses
        if (0 < curNodeData.getACoLCount()) {
            hd.startElement("", "", "tokens", new AttributesImpl());
            for (Iterator<IAtomicConceptOfLabel> it = curNodeData.getACoLs(); it.hasNext(); ) {
                IAtomicConceptOfLabel acol = it.next();
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "", "id", "CDATA", Integer.toString(acol.getId()));
                hd.startElement("", "", "token", atts);

                renderString(hd, new AttributesImpl(), "text", acol.getToken());
                renderString(hd, new AttributesImpl(), "lemma", acol.getLemma());

                hd.startElement("", "", "senses", new AttributesImpl());
                for (Iterator<ISense> i = acol.getSenses(); i.hasNext(); ) {
                    ISense sense = i.next();
                    atts = new AttributesImpl();
                    atts.addAttribute("", "", "id", "CDATA", sense.getId());
                    hd.startElement("", "", "sense", atts);
                    hd.endElement("", "", "sense");
                }
                hd.endElement("", "", "senses");

                hd.endElement("", "", "token");
            }
            hd.endElement("", "", "tokens");
        }
    }

}