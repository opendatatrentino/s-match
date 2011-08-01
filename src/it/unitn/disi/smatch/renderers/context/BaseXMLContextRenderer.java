package it.unitn.disi.smatch.renderers.context;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.sax.TransformerHandler;

/**
 * Base class for xml context renderers.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class BaseXMLContextRenderer extends BaseFileContextRenderer {

    protected static void renderString(TransformerHandler hd, AttributesImpl atts, final String tagName, final String tagValue) throws SAXException {
        if (null != tagValue && 0 < tagValue.length()) {
            hd.startElement("", "", tagName, atts);
            hd.characters(tagValue.toCharArray(), 0, tagValue.length());
            hd.endElement("", "", tagName);
        }
    }
}
