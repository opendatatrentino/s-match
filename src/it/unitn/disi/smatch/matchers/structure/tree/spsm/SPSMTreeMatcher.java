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


/**
 * Used the DefaultTreeMatcher for computing the default set of mapping elements and 
 * then calls the TreeMatcher.SPSMTreeMatcher.spsmFilter property where the filtering 
 * and computation of the similarity score is performed. 
 * The filters returns the set of mapping elements to preserve a set of structural properties, 
 * namely:
 * <ul>
 * <li> one-to-one correspondences between semantically related nodes,
 * <li> leaf nodes are matched to leaf nodes and internal nodes are matched to internal nodes.
 * </ul>
 * <p>    
 * For further details refer to:
 * Approximate structure-preserving semantic matching 
 * by
 * Giunchiglia, Fausto and McNeill, Fiona and Yatskevich, Mikalai and 
 * Pane, Juan and Besana, Paolo and Shvaiko, Pavel (2008) 
 * {@link http://eprints.biblio.unitn.it/archive/00001459/} 
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

    
    /**
     * Creates a function-like tree from the given root node
     * @param node the root node
     */
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
