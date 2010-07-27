package it.unitn.disi.smatch.matchers.structure.tree.spsm;



import java.util.List;
import java.util.Properties;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;



import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingFactory;
import it.unitn.disi.smatch.matchers.structure.node.INodeMatcher;
import it.unitn.disi.smatch.matchers.structure.tree.BaseTreeMatcher;
import it.unitn.disi.smatch.matchers.structure.tree.ITreeMatcher;
import it.unitn.disi.smatch.matchers.structure.tree.TreeMatcherException;
import it.unitn.disi.smatch.matchers.structure.tree.def.DefaultTreeMatcher;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.TreeEditDistance;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeAccessor;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl.CTXMLTreeAccessor;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.MatchedTreeNodeComparator;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.WorstCaseDistanceConversion;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.filters.IMappingFilter;
import it.unitn.disi.smatch.filters.SPSMMappingFilter;



/**
 * This class implements <code>Matcher</code> interface 
 * and provides the basic functionality of the matching component.
 * It supports 
 * the matching of LCC constraints (e.g, car(big, Nissan) is similar 
 * to car(big, Acura)) and label matching (e.g., car=auto).
 * As for April 29th, 2007 it supports 
 * 
 * 5 string-based label matchers implemented in smatch,
 * structure preserving matcher that exploit node matcher (S-Match),
 * any other label matchers implementing <code>Matcher</code> interface.
 
 *See <code>Example</code> for usage example.
 *
 * @author Juan Pane pane@disi.unitn.it
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 * 
 */
public class SPSMTreeMatcher extends DefaultTreeMatcher {


	private final static Logger log = Logger 
	.getLogger(SPSMTreeMatcher.class.getName());
	
    private static final String SPSM_FILTER_KEY = "spsmFilter";
    protected IMappingFilter spsmFilter = null;

    
    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
    	super.setProperties(newProperties);
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        boolean result = super.setProperties(newProperties);
//        if (result) {
            if (newProperties.containsKey(SPSM_FILTER_KEY)) {
                spsmFilter = new SPSMMappingFilter();//(IMappingFilter) configureComponent(spsmFilter, oldProperties, newProperties, "spsm filter", SPSM_FILTER_KEY, IMappingFilter.class);
            } else {
                final String errMessage = "Cannot find configuration key " + SPSM_FILTER_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }
//        }
        return result;
    }


	@Override
	public IContextMapping<INode> treeMatch(IContext sourceContext,
			IContext targetContext,
			IContextMapping<IAtomicConceptOfLabel> acolMapping)
			throws TreeMatcherException {
    	
    	
    	try{
    		IContextMapping<INode> defaultMappings = super.treeMatch(sourceContext, targetContext, acolMapping);
    		
    		IContextMapping<INode> spsmMapppings = spsmFilter.filter(defaultMappings);
    		
    		return spsmMapppings;
    	}
    	catch(Exception e)
    	{
    		log.info("Problem matching source[" +getFNSignatureFromIContext(sourceContext.getRoot()) +"] to target["+getFNSignatureFromIContext(targetContext.getRoot())+"]");
        	log.info(e.getMessage());
        	log.info(SPSMTreeMatcher.class.getName(), e);
    		
        	return null;
    	}
    }
    


    private String getFNSignatureFromIContext(INode node){
    	
		String ret = node.getNodeData().getName();
		List<INode> children = node.getChildrenList();
		if(children != null && children.size() > 0){
			ret+="(";
			for (int i = 0; i < children.size(); i++)
				ret+=getFNSignatureFromIContext(children.get(i)) +",";
			
			ret = ret.substring(0, ret.length()-1);
			ret+=")";
		}
		return  ret;	
   
    }

  
 
}
