package it.unitn.disi.smatch.matcher.structure.tree.spsm;

import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import junit.framework.TestCase;
import org.junit.Before;

import java.io.File;
import java.util.Iterator;


public class SPSMTest extends TestCase {

    private IMatchManager mm;
    private String confPath = ".." + File.separator + "conf" + File.separator + "";//

    private String propertiesFile = "s-match-spsm-function.properties";

    @Before
    protected void setUp() throws Exception{
        String configFile = confPath + propertiesFile;
        
         mm = new MatchManager(configFile);
        
    }


    public void runTest() throws Exception {
        System.out.println("RunTest initialized");
        testMappingsExample();
        testIsAlive6Response();
        testGetWine();
        System.out.println("RunTest finished");
    }


    /**
     * Example test as shown in <a href="http://s-match.org/spsm.html">http://s-match.org/spsm.html</a>
     * @throws SMatchException SMatchException 
     */
    public void testMappingsExample() throws SMatchException {
    	
    	
        IContextMapping<INode> mappings = matchFunctions("Courses(College of Arts and Sciences(Earth and Atmospheric Sciences,History(Latin America History,America History,Ancient European History),Computer Science))",
                "Course(College of Arts and Sciences(Earth Sciences(Geophysics,Geological Sciences),Computer Science,History(History of Americas,Ancient and Medieval History)))");
      
        assertFalse(null == mappings);
        assertFalse(mappings.isEmpty());
        //test the parsing of the source tree and that all the expected nodes are present 
        //in the correct position of the tree 
        /* Source tree *
    	 * Courses
    	 * 		College of Arts and Sciences
    	 * 			Earth and Atmospheric Sciences
    	 * 			History
    	 *				Latin America History
    	 *				America History
    	 *				Ancient European History
    	 *			Computer Science
    	 */
        
        INode sourceRoot = mappings.getSourceContext().getRoot();	//Courses
        testINode(sourceRoot,1,false);
        
        INode sourceChild0 = sourceRoot.getChildAt(0); //College of Arts and Sciences
        testINode(sourceChild0,3,false);
        
        INode sourceChild0_0 = sourceChild0.getChildAt(0); //Earth and Atmospheric Sciences
        testINode(sourceChild0_0,0,true);
        INode sourceChild0_1 = sourceChild0.getChildAt(1); //History
        testINode(sourceChild0_1,3,false);
        INode sourceChild0_2 = sourceChild0.getChildAt(2); //Computer Science
        testINode(sourceChild0_2,0,true);
        
        INode sourceChild0_1_0 = sourceChild0_1.getChildAt(0); //Latin America History
        testINode(sourceChild0_1_0,0,true);
        INode sourceChild0_1_1 = sourceChild0_1.getChildAt(1); //America History
        testINode(sourceChild0_1_1,0,true);
        INode sourceChild0_1_2 = sourceChild0_1.getChildAt(2); //Ancient European History
        testINode(sourceChild0_1_2,0,true);
        
        
        //test the parsing of the target tree and that all the expected nodes are present 
        //in the correct position of the tree 
        /* Target Tree *
         * Course
         * 		College of Arts and Sciences
         * 			Earth Sciences
         * 				Geophysics
         * 				Geological Sciences
         * 			Computer Science
         * 			History
         * 				History of Americas
         * 				Ancient and Medieval History
         */
        INode targetRoot = mappings.getTargetContext().getRoot(); //Course
        testINode(targetRoot,1,false);
        
        INode targetChild0 = targetRoot.getChildAt(0); //College of Arts and Sciences
        testINode(targetChild0,3,false);
        INode targetChild0_0 = targetChild0.getChildAt(0); //Earth Sciences
        testINode(targetChild0_0,2,false);
        INode targetChild0_1 = targetChild0.getChildAt(1);//Computer Science
        testINode(targetChild0_1,0,true);
        INode targetChild0_2 = targetChild0.getChildAt(2);//History
        testINode(targetChild0_2,2,false);
        
        INode targetChild0_0_0 = targetChild0_0.getChildAt(0); //Geophysics
        testINode(targetChild0_0_0,0,true);
        INode targetChild0_0_1 = targetChild0_0.getChildAt(1); //Geological Sciences
        testINode(targetChild0_0_1,0,true);
        
        INode targetChild0_2_0 = targetChild0_2.getChildAt(0); //History of Americas
        testINode(targetChild0_2_0,0,true);
        INode targetChild0_2_1 = targetChild0_2.getChildAt(1); //Ancient and Medieval History
        testINode(targetChild0_2_1,0,true);
        
        //test the mappings
        assertTrue(mappings.size() == 6);
        assertTrue(mappings.getSimilarity() > 0.2);
        
        //Courses = Course
        assertTrue(mappings.getRelation(sourceRoot, targetRoot) == IMappingElement.EQUIVALENCE);

        //College of Arts and Sciences = College of Arts and Sciences
        assertTrue(mappings.getRelation(sourceChild0, targetChild0) == IMappingElement.EQUIVALENCE);
        
        //Earth and Atmospheric Sciences >  Geophysics
        assertTrue(mappings.getRelation(sourceChild0_0, targetChild0_0_0) == IMappingElement.MORE_GENERAL);
        
        //History = History
        assertTrue(mappings.getRelation(sourceChild0_1, targetChild0_2) == IMappingElement.EQUIVALENCE);
        
        //America History = History of Americas
        assertTrue(mappings.getRelation(sourceChild0_1_1, targetChild0_2_0) == IMappingElement.EQUIVALENCE);
        
        //Computer Science = Computer Science
        assertTrue(mappings.getRelation(sourceChild0_2, targetChild0_1) == IMappingElement.EQUIVALENCE);
        
        System.out.println("Example test finished");

    }
    
    
    /**
     * Test a particular INode for certain properties:
     * <ul>
     * <li>the INode is not null
     * <li>the INode contains the number of specified children
     * <li>the INode is (or not) a leaf
     * </ul>
     * @param node			the node to be tested
     * @param childCount	the number of expected children for the given node
     * @param isLeaf		whether the given node should be of not a leaf node
     */
    private void testINode(INode node, int childCount, boolean isLeaf){
        assertFalse(node == null);
        assertTrue(node.getChildCount() == childCount);
        assertTrue(node.isLeaf() == isLeaf);
    }

    
    public void testIsAlive6Response() throws SMatchException{
    	IContextMapping<INode> mappings = matchFunctions("IsAlive6Response(IsAlive(IsAlive6Request),return)",
      "ListLiveCityCamsResponse(ListLiveCityCams(ListLiveCityCamsRequest),ListLiveCityCamsResponse(LiveCityCam(citycam(camid,city,location)))))");
 
    	assertFalse(null == mappings);
        assertFalse(mappings.isEmpty());
        //test the parsing of the source tree and that all the expected nodes are present 
        //in the correct position of the tree 
        /* Source Tree *
         * IsAlive6Response
         * 		IsAlive
         * 			IsAlive6Request
         * 		return
         */
        INode sourceRoot = mappings.getSourceContext().getRoot(); //Course
        testINode(sourceRoot,2,false);
    	
        INode isAlive = sourceRoot.getChildAt(0); //IsAlive
        testINode(isAlive,1,false);
        
    	
        //test the parsing of the target tree and that all the expected nodes are present 
        //in the correct position of the tree 
        /* Target Tree *
         * ListLiveCityCamsResponse
         * 		ListLiveCityCams
         * 			ListLiveCityCamsRequest
         * 		ListLiveCityCamsResponse
         * 			LiveCityCam
         * 				citycam
         * 					camid
         * 					city
         * 					location 
         */
        INode targetRoot = mappings.getTargetContext().getRoot(); //Course
        testINode(targetRoot,2,false);
        
        INode listLiveCityCamsResponse = targetRoot.getChildAt(1); //ListLiveCityCamsResponse
        testINode(listLiveCityCamsResponse,1,false);
        
        //test the mappings
        assertTrue(mappings.size() == 2);
        assertTrue(mappings.getSimilarity() > 0.1);
        
        //IsAlive6Response < ListLiveCityCamsResponse
        assertTrue(mappings.getRelation(sourceRoot, targetRoot) == IMappingElement.LESS_GENERAL);
      
        //IsAlive < ListLiveCityCamsResponse 
        //(not that this node, even though it contains the same name a the root, is another node)
        assertTrue(mappings.getRelation(isAlive, listLiveCityCamsResponse) == IMappingElement.LESS_GENERAL);
    }
    
    
    public void testGetWine() throws SMatchException{
    	IContextMapping<INode> mappings =  matchFunctions(" get_wine(region,country,color,price,amount)",
    	   "get_wine(region( country,area	), colour, cost, year, quantity)");
    	
    	assertFalse(null == mappings);
        assertFalse(mappings.isEmpty());
    	
        //test the parsing of the soruce tree and that all the expected nodes are present 
        //in the correct position of the tree 
        /* Source Tree *
         * get_wine
         * 		region
         * 		country
         * 		color
         * 		price
         * 		amount
         */
        INode sourceRoot = mappings.getSourceContext().getRoot(); //Course
        testINode(sourceRoot,5,false);
    	
        INode sourceRegion = sourceRoot.getChildAt(0); //region
        testINode(sourceRegion,0,true);
        
        INode sourceCountry = sourceRoot.getChildAt(1); //country
        testINode(sourceCountry,0,true);
        
        INode sourceColor = sourceRoot.getChildAt(2); //color
        testINode(sourceColor,0,true);
        
        INode sourcePrice = sourceRoot.getChildAt(3); //price
        testINode(sourcePrice,0,true);
        
        INode sourceAmount = sourceRoot.getChildAt(4); //amount
        testINode(sourceAmount,0,true);
        
        //test the parsing of the target tree and that all the expected nodes are present 
        //in the correct position of the tree 
        /* Target Tree *
         * get_wine
         * 		region
         * 			country
         * 			area 
         * 		colour 
         * 		cost 
         * 		year
         * 		quantity
         */
        
        INode targetRoot = mappings.getTargetContext().getRoot(); //Course
        testINode(targetRoot,5,false);
        
        INode targetRegion = targetRoot.getChildAt(0); //region 
        testINode(targetRegion,2,false);
        
        INode targetCountry = targetRegion.getChildAt(0); //country 
        testINode(targetCountry,0,true);
        
        INode targetArea = targetRegion.getChildAt(1); //area 
        testINode(targetArea,0,true);
        
        INode targetColour = targetRoot.getChildAt(1); //colour 
        testINode(targetColour,0,true);
        
        INode targetCost = targetRoot.getChildAt(2); //cost 
        testINode(targetCost,0,true);
        
        INode targetYear = targetRoot.getChildAt(3); //year 
        testINode(targetYear,0,true);
        
        INode targetQuantity = targetRoot.getChildAt(4); //quantity 
        testINode(targetQuantity,0,true);
        
        //test the mappings
        assertTrue(mappings.size() == 6);
        assertTrue(mappings.getSimilarity() == 0.5);
        
        //get_wine = get_wine
        assertTrue(mappings.getRelation(sourceRoot, targetRoot) == IMappingElement.EQUIVALENCE);
      
        //region <-> area
        assertTrue(mappings.getRelation(sourceRegion, targetArea) == IMappingElement.EQUIVALENCE);
        
        //country <-> country
        assertTrue(mappings.getRelation(sourceCountry, targetCountry) == IMappingElement.EQUIVALENCE);
        
        //color <-> colour
        assertTrue(mappings.getRelation(sourceColor, targetColour) == IMappingElement.EQUIVALENCE);
        
        //price <-> cost	
        assertTrue(mappings.getRelation(sourcePrice, targetCost) == IMappingElement.EQUIVALENCE);
        
        //amount <-> quantity
        assertTrue(mappings.getRelation(sourceAmount, targetQuantity) == IMappingElement.EQUIVALENCE);
        
        
    }

    /**
     * Matches two trees in function-like structures. The function loads the context for each fn and
     * then performs the pre-processing. Finally performs the rendering
     *
     * @param fn1 the first tree
     * @param fn2 the second tree
     * @return the resulting mapping context
     * @throws SMatchException SMatchException
     */
    private IContextMapping<INode> matchFunctions(String fn1, String fn2) throws SMatchException {
        IContext sourceContext = (IContext) mm.loadContext(fn1);
        IContext targetContext = (IContext) mm.loadContext(fn2);

        // linguistic pre-processing
        mm.offline(sourceContext);
        mm.offline(targetContext);

        // match
        IContextMapping<INode> mapping = mm.online(sourceContext, targetContext);

        print(mapping);

        return mapping;
    }


    /**
     * Prints the mappings into the std output.
     *
     * @param contextMapping the mapping
     */
    public static void print(IContextMapping<INode> contextMapping) {

        Iterator<IMappingElement<INode>> me = contextMapping.iterator();
        if (me != null) {
            //prints the root
            System.out.println("SIM: " + contextMapping.getSimilarity());

            //prints the rest
            while (me.hasNext()) {
                print(me.next());
            }

        }

    }


    /**
     * Prints a mapping elements into the std output.
     *
     * @param mappingElement the mapping element to be printed
     */
    public static void print(IMappingElement<INode> mappingElement) {

        if (mappingElement != null) {

            System.out.println("\t " + mappingElement.getSource().getNodeData().getName() +
                    " <-> " + mappingElement.getTarget().getNodeData().getName() +
                    "\t {" + mappingElement.getRelation() + "}");

        }

    }

}
