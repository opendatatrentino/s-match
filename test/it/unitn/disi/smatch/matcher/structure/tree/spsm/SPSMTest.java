package it.unitn.disi.smatch.matcher.structure.tree.spsm;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;


import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.MappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.SPSMTreeMatcher;

public class SPSMTest {
	
	private IMatchManager mm;
    String confPath = ".." + File.separator + "conf" + File.separator + "";//
    String propertiesFile = "s-match-spsm.properties";
    String logProperties = "logging.properties";
    String sourceFile = "source.txt";
    String targetFile = "target.txt";
    String preprocesedSource = "source.xml";
    String preprocesedTarger = "target.xml";
    
    String filePath = ".." + File.separator + "test-data" + File.separator + "spsm" + File.separator + "";
    
    
    public static void main(String[] args) {
    	PropertyConfigurator.configure(".." + File.separator + "conf" + File.separator + "log4j.properties");
                
        SPSMTest ex = new SPSMTest();
        try {
			ex.runMatcher();
		} catch (ConfigurableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //exampleTabIndentedFile(mc);

//		"auto(brand,name, color)", "car(year,brand, colour)"
    }
    
    
    public void runMatcher() throws ConfigurableException {
        String configFile = confPath+ propertiesFile;
        
        mm = new MatchManager(configFile);
        String sourceFileName = filePath+sourceFile;
        String targetFileName = filePath+targetFile;

//        
//        mm.setProperties(configFile);


        //loads the files
        IContext sourceContext = mm.loadContext(sourceFileName);
        IContext targetContext = mm.loadContext(targetFileName);
        
        // linguistic pre-processing
        mm.offline(sourceContext);
        mm.offline(targetContext);

        // match
        IContextMapping<INode> mapping = mm.online(sourceContext, targetContext);
        mm.renderMapping(mapping, filePath + "result-default.txt");
        print(mapping);
    }
  
    
   


   

    public static void print(IContextMapping<INode> tm){
    	
    	Iterator<IMappingElement<INode>> me = tm.iterator();
        if (me != null){
        	//prints the root
	        System.out.println( "SIM: "+  tm.getSimilarity() );
	    	
	        //prints the rest
	        while(me.hasNext()){
	        	print(me.next());
	        }
	     
        }
        
    }

    
    public static void print(IMappingElement<INode> me){
	
        if (me != null){

	            System.out.println( "\t " +me.getSource().getNodeData().getName()+
	                    " <-> " + me.getTarget().getNodeData().getName()  +
	                    "\t {" + me.getRelation()+"}");
 	
        }
        
    }
    
    
   
}
