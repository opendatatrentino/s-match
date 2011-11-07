package it.unitn.disi.annotation.loaders.context;

import it.unitn.disi.annotation.data.INLPContext;
import it.unitn.disi.annotation.data.INLPNode;
import it.unitn.disi.annotation.data.NLPContext;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.data.Label;
import it.unitn.disi.nlptools.data.Token;
import it.unitn.disi.smatch.data.ling.ISense;
import it.unitn.disi.smatch.loaders.context.BaseSimpleXMLContextLoader;
import it.unitn.disi.smatch.loaders.context.ContextLoaderException;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.LinguisticOracleException;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Loads SimpleXML with labels.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NLPXMLContextLoader extends BaseSimpleXMLContextLoader<INLPContext> implements INLPContextLoader {

    private static final Logger log = Logger.getLogger(NLPXMLContextLoader.class);

    // label being read
    private ILabel label;
    // token being read
    private IToken token;

    private final static String LINGUISTIC_ORACLE = "oracle";
    private ILinguisticOracle oracle;

    public NLPXMLContextLoader() throws ContextLoaderException {
        super();
        uniqueStrings = true;//at least for POS tags
    }

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

    public void startDocument() {
        super.startDocument();
        ctx = new NLPContext();
    }

    public void startElement(String namespace, String localName, String qName, Attributes atts) {
        if ("node".equals(localName)) {
            INLPNode node;
            if (null == ctx.getRoot()) {
                node = ctx.createRoot();
            } else {
                if (0 < pathToRoot.size()) {
                    node = (INLPNode) pathToRoot.getLast().createChild();
                } else {
                    // looks like there are multiple roots
                    INLPNode oldRoot = ctx.getRoot();
                    INLPNode newRoot = ctx.createRoot("Top");
                    newRoot.addChild(oldRoot);
                    node = newRoot.createChild();
                }
            }
            node.getNodeData().setId(atts.getValue("id"));
            pathToRoot.addLast(node);
        } else if ("label".equals(localName)) {
            label = new Label();
            ((INLPNode) pathToRoot.getLast()).getNodeData().setLabel(label);
        } else if ("token".equals(localName)) {
            if (null != label) {
                token = new Token();
                if (0 == label.getTokens().size()) {
                    label.setTokens(new ArrayList<IToken>());
                }
                token.setText(atts.getValue("text"));
                token.setPOSTag(atts.getValue("pos").intern());
                label.getTokens().add(token);
            }
        } else if ("sense".equals(localName)) {
            if (null != oracle) {
                if (0 == token.getSenses().size()) {
                    token.setSenses(new ArrayList<ISense>());
                }
                try {
                    token.getSenses().add(oracle.createSense(atts.getValue("id")));
                } catch (LinguisticOracleException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        } else {
            content = new StringBuilder();
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if ("name".equals(localName)) {
            pathToRoot.getLast().getNodeData().setName(makeUnique(content.toString()));
        } else if ("formula".equals(localName)) {
            if (null != label) {
                label.setFormula(content.toString());
            }
        } else if ("text".equals(localName)) {
            if (null != label) {
                label.setText(makeUnique(content.toString()));
            }
        } else if ("label".equals(localName)) {
            label = null;
        } else if ("token".equals(localName)) {
            token = null;
        } else if ("node".equals(localName)) {
            pathToRoot.removeLast();

            nodesParsed++;
            if (0 == (nodesParsed % 1000)) {
                log.info("nodes parsed: " + nodesParsed);
            }
        }
    }
}
