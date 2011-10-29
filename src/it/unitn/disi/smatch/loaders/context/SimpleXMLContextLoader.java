package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.Context;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import java.util.Properties;

/**
 * Loader for XML format. Reads uniqueStrings boolean parameter which configures whether create all strings as
 * separate instances or save memory by reusing string instances. False by default, it is useful for
 * contexts with a lot of repetition on the level of labels or label tokens.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SimpleXMLContextLoader extends BaseSimpleXMLContextLoader<IContext> implements IContextLoader, ContentHandler {

    private static final Logger log = Logger.getLogger(SimpleXMLContextLoader.class);

    // atomic concept being read
    private IAtomicConceptOfLabel acol;

    private final static String LINGUISTIC_ORACLE = "oracle";
    private ILinguisticOracle oracle;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(LINGUISTIC_ORACLE)) {
                oracle = (ILinguisticOracle) configureComponent(oracle, oldProperties, newProperties, "linguistic oracle", LINGUISTIC_ORACLE, ILinguisticOracle.class);
            } else {
                final String errMessage = "Cannot find configuration key " + LINGUISTIC_ORACLE;
                log.warn(errMessage);
                oracle = null;
            }
        }
        return result;
    }

    public SimpleXMLContextLoader() throws ContextLoaderException {
        super();
    }

    //org.xml.sax.ContentHandler methods re-implementation start

    public void startDocument() {
        super.startDocument();
        ctx = new Context();
    }

    public void startElement(String namespace, String localName, String qName, Attributes atts) {
        if ("node".equals(localName)) {
            INode node;
            if (null == ctx.getRoot()) {
                node = ctx.createRoot();
            } else {
                if (0 < pathToRoot.size()) {
                    node = (INode) pathToRoot.getLast().createChild();
                } else {
                    // looks like there are multiple roots
                    INode oldRoot = ctx.getRoot();
                    INode newRoot = ctx.createRoot("Top");
                    newRoot.addChild(oldRoot);
                    node = newRoot.createChild();
                }
            }
            node.getNodeData().setId(atts.getValue("id"));
            node.getNodeData().setIsPreprocessed(-1 < atts.getIndex("", "preprocessed"));
            pathToRoot.addLast(node);
        } else if ("token".equals(localName)) {
            acol = ((INode) pathToRoot.getLast()).getNodeData().createACoL();
            acol.setId(Integer.parseInt(atts.getValue("id")));
        } else if ("sense".equals(localName)) {
            if (null != oracle) {
                if (-1 == atts.getIndex("pos")) {
                    try {
                        acol.addSense(oracle.createSense(atts.getValue("id")));
                    } catch (LinguisticOracleException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                } else {
                    try {
                        acol.addSense(oracle.createSense(atts.getValue("pos") + "#" + atts.getValue("id")));
                    } catch (LinguisticOracleException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        } else {
            content = new StringBuilder();
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if ("name".equals(localName)) {
            pathToRoot.getLast().getNodeData().setName(makeUnique(content.toString()));
        } else if ("label-formula".equals(localName)) {
            ((INode) pathToRoot.getLast()).getNodeData().setcLabFormula(content.toString());
        } else if ("node-formula".equals(localName)) {
            ((INode) pathToRoot.getLast()).getNodeData().setcNodeFormula(content.toString());
        } else if ("provenance".equals(localName)) {
            ((INode) pathToRoot.getLast()).getNodeData().setProvenance(content.toString());
        } else if ("text".equals(localName)) {
            acol.setToken(makeUnique(content.toString()));
        } else if ("lemma".equals(localName)) {
            acol.setLemma(makeUnique(content.toString()));
        } else if ("token".equals(localName)) {
            ((INode) pathToRoot.getLast()).getNodeData().addACoL(acol);
        } else if ("node".equals(localName)) {
            pathToRoot.removeLast();

            nodesParsed++;
            if (0 == (nodesParsed % 1000)) {
                log.info("nodes parsed: " + nodesParsed);
            }
        }
    }
}