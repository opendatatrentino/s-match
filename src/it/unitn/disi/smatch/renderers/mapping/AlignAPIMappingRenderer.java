package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Level;
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
import java.util.Properties;

/**
 * Renders the mapping in the AlignAPI mapping format.
 *
 * Needs parameters:
 *
 *  onto1URI, onto2URI - URIs of ontologies
 *  onto1Location, onto2Location - locations of ontologies 
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class AlignAPIMappingRenderer extends Configurable implements IMappingRenderer {

    private static final Logger log = Logger.getLogger(PlainMappingRenderer.class);

    private final static String ONTO1_URI_KEY = "onto1URI";
    private String onto1URI = null;

    private final static String ONTO2_URI_KEY = "onto2URI";
    private String onto2URI = null;

    private final static String ONTO1_LOCATION_KEY = "onto1Location";
    private String onto1Location = null;

    private final static String ONTO2_LOCATION_KEY = "onto2Location";
    private String onto2Location = null;

    private final static String MEASURE = "1.0";

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(ONTO1_URI_KEY)) {
                onto1URI = newProperties.getProperty(ONTO1_URI_KEY);
            } else {
                final String errMessage = "Cannot find configuration key " + ONTO1_URI_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
            if (newProperties.containsKey(ONTO2_URI_KEY)) {
                onto2URI = newProperties.getProperty(ONTO2_URI_KEY);
            } else {
                final String errMessage = "Cannot find configuration key " + ONTO2_URI_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
            if (newProperties.containsKey(ONTO1_LOCATION_KEY)) {
                onto1Location = newProperties.getProperty(ONTO1_LOCATION_KEY);
            } else {
                final String errMessage = "Cannot find configuration key " + ONTO1_LOCATION_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
            if (newProperties.containsKey(ONTO2_LOCATION_KEY)) {
                onto2Location = newProperties.getProperty(ONTO2_LOCATION_KEY);
            } else {
                final String errMessage = "Cannot find configuration key " + ONTO2_LOCATION_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }

        return result;
    }


    public void render(IContextMapping<INode> mapping, String outputFile) throws MappingRendererException {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            try {
                int lg = 0;
                int mg = 0;
                int eq = 0;
                int dj = 0;

                long counter = 0;
                long total = mapping.size();
                long reportInt = (total / 20) + 1;//i.e. report every 5%

                StreamResult streamResult = new StreamResult(out);
                SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
                TransformerHandler hd = tf.newTransformerHandler();
                Transformer serializer = hd.getTransformer();
                serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                hd.setResult(streamResult);
                hd.startDocument();
                final String base = "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#";
                hd.startPrefixMapping("", base);
                hd.endPrefixMapping("");
                hd.startPrefixMapping("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                hd.endPrefixMapping("rdf");
                hd.startPrefixMapping("xsd", "http://www.w3.org/2001/XMLSchema#");
                hd.endPrefixMapping("xsd");
                hd.startPrefixMapping("align", base);
                hd.endPrefixMapping("align");

                AttributesImpl atts = new AttributesImpl();
                hd.startElement("", "", "rdf:RDF", atts);

                hd.startElement("", "", "Alignment", new AttributesImpl());
                renderString(hd, new AttributesImpl(), "xml", "yes");
                renderString(hd, new AttributesImpl(), "level", "0");
                renderString(hd, new AttributesImpl(), "type", "**");

                renderOntology(hd, "1", onto1URI, onto1Location);
                renderOntology(hd, "2", onto2URI, onto2Location);

                for (IMappingElement<INode> mappingElement : mapping) {
                    hd.startElement("", "", "map", new AttributesImpl());
                    hd.startElement("", "", "Cell", new AttributesImpl());

                    atts = new AttributesImpl();
                    atts.addAttribute("", "", "rdf:resource", "CDATA", onto1URI + "#" + mappingElement.getSource().getNodeData().getId());
                    hd.startElement("", "", "entity1", atts);
                    hd.endElement("", "", "entity1");
                    atts = new AttributesImpl();

                    atts.addAttribute("", "", "rdf:resource", "CDATA", onto2URI + "#" + mappingElement.getTarget().getNodeData().getId());
                    hd.startElement("", "", "entity2", atts);
                    hd.endElement("", "", "entity2");
                    char relation = mappingElement.getRelation();

                    hd.startElement("", "", "relation", new AttributesImpl());
                    hd.characters(new char[]{relation}, 0, 1);
                    hd.endElement("", "", "relation");

                    atts = new AttributesImpl();
                    atts.addAttribute("", "", "rdf:datatype", "CDATA", "http://www.w3.org/2001/XMLSchema#float");
                    hd.startElement("", "", "measure", atts);
                    hd.characters(MEASURE.toCharArray(), 0, MEASURE.length());
                    hd.endElement("", "", "measure");

                    hd.endElement("", "", "Cell");
                    hd.endElement("", "", "map");


                    switch (relation) {
                        case IMappingElement.LESS_GENERAL: {
                            lg++;
                            break;
                        }
                        case IMappingElement.MORE_GENERAL: {
                            mg++;
                            break;
                        }
                        case IMappingElement.EQUIVALENCE: {
                            eq++;
                            break;
                        }
                        case IMappingElement.DISJOINT: {
                            dj++;
                            break;
                        }
                        default:
                            break;
                    }

                    counter++;
                    if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                        log.info(100 * counter / total + "%");
                    }
                }//for

                hd.endElement("", "", "Alignment");
                hd.endElement("", "", "rdf:RDF");
                hd.endDocument();

                if (log.isEnabledFor(Level.INFO)) {
                    log.info("rendered links: " + mapping.size());
                    log.info("LG: " + lg);
                    log.info("MG: " + mg);
                    log.info("EQ: " + eq);
                    log.info("DJ: " + dj);
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MappingRendererException(errMessage, e);
        } catch (SAXException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MappingRendererException(errMessage, e);
        } catch (TransformerConfigurationException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MappingRendererException(errMessage, e);
        }
    }

    public String getDescription() {
        return ILoader.RDF_FILES;
    }

    public ILoader.LoaderType getType() {
        return ILoader.LoaderType.FILE;
    }

    private static void renderOntology(TransformerHandler hd, String index, String URI, String location) throws SAXException {
        hd.startElement("", "", "onto" + index, new AttributesImpl());
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "", "rdf:about", "CDATA", URI);
        hd.startElement("", "", "Ontology", atts);
        renderString(hd, new AttributesImpl(), "location", location);
        hd.startElement("", "", "formalism", new AttributesImpl());
        atts = new AttributesImpl();
        atts.addAttribute("", "", "align:name", "CDATA", "OWL2.0");
        atts.addAttribute("", "", "align:uri", "CDATA", "http://www.w3.org/2002/07/owl#");
        hd.startElement("", "", "Formalism", atts);
        hd.endElement("", "", "Formalism");
        hd.endElement("", "", "formalism");
        hd.endElement("", "", "Ontology");
        hd.endElement("", "", "onto" + index);

    }

    private static void renderString(TransformerHandler hd, AttributesImpl atts, final String tagName, final String tagValue) throws SAXException {
        if (null != tagValue && 0 < tagValue.length()) {
            hd.startElement("", "", tagName, atts);
            hd.characters(tagValue.toCharArray(), 0, tagValue.length());
            hd.endElement("", "", tagName);
        }
    }
}
