package it.unitn.disi.smatch.matchers.structure.tree.spsm;

import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.filters.IMappingFilter;
import it.unitn.disi.smatch.matchers.structure.tree.TreeMatcherException;
import it.unitn.disi.smatch.matchers.structure.tree.def.DefaultTreeMatcher;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

//TODO Juan, update Javadoc

/**
 * This class implements <code>ITreeMatcher</code> interface
 * and provides the basic functionality of the matching component.
 * It supports the matching of LCC constraints (e.g, car(big, Nissan) is similar
 * to car(big, Acura)) and label matching (e.g., car=auto).
 * <p/>
 * It supports
 * <p/>
 * 5 string-based label matchers implemented in smatch,
 * structure preserving matcher that exploit node matcher (S-Match),
 * any other label matchers implementing <code>Matcher</code> interface.
 * <p/>
 * See <code>Example</code> for usage example.
 *
 * @author Juan Pane pane@disi.unitn.it
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SPSMTreeMatcher extends DefaultTreeMatcher {

    private final static Logger log = Logger.getLogger(SPSMTreeMatcher.class.getName());

    private static final String SPSM_FILTER_KEY = "spsmFilter";
    protected IMappingFilter spsmFilter = null;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(SPSM_FILTER_KEY)) {
                spsmFilter = (IMappingFilter) configureComponent(spsmFilter, oldProperties, newProperties, "spsm filter", SPSM_FILTER_KEY, IMappingFilter.class);
            } else {
                final String errMessage = "Cannot find configuration key " + SPSM_FILTER_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
        }
        return result;
    }

    @Override
    public IContextMapping<INode> treeMatch(IContext sourceContext,
                                            IContext targetContext,
                                            IContextMapping<IAtomicConceptOfLabel> acolMapping)
            throws TreeMatcherException {


        try {
            IContextMapping<INode> defaultMappings = super.treeMatch(sourceContext, targetContext, acolMapping);

            IContextMapping<INode> spsmMapppings = spsmFilter.filter(defaultMappings);

            return spsmMapppings;
        }
        catch (Exception e) {
            log.info("Problem matching source[" + getFNSignatureFromIContext(sourceContext.getRoot()) + "] to target[" + getFNSignatureFromIContext(targetContext.getRoot()) + "]");
            log.info(e.getMessage());
            log.info(SPSMTreeMatcher.class.getName(), e);

            return null;
        }
    }

    //TODO Juan, add Javadoc

    private String getFNSignatureFromIContext(INode node) {

        String ret = node.getNodeData().getName();
        List<INode> children = node.getChildrenList();
        if (children != null && children.size() > 0) {
            ret += "(";
            for (INode aChildren : children) {
                ret += getFNSignatureFromIContext(aChildren) + ",";
            }

            ret = ret.substring(0, ret.length() - 1);
            ret += ")";
        }
        return ret;

    }


}
