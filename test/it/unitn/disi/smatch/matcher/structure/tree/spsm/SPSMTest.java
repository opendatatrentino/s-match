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


//TODO Juan, add it to the SMatchSuite
public class SPSMTest extends TestCase {

    private IMatchManager mm;
    private String confPath = ".." + File.separator + "conf" + File.separator + "";//

    private String propertiesFile = "s-match-spsm-function.properties";

    private String filePath = ".." + File.separator + "test-data" + File.separator + "spsm" + File.separator + "";

    @Before
    protected void setUp() {
        String configFile = confPath + propertiesFile;
        //TODO Juan, don't hush exceptions, throw them further, test will correctly break
        try {
            mm = new MatchManager(configFile);
        } catch (SMatchException e) {
            e.printStackTrace();
        }
    }


    public void runTest() {
        System.out.println("RunTest initialized");
        testMappingsExample();
        System.out.println("RunTest finished");
    }


    /**
     * Example test as shown in <a href="http://s-match.org/spsm.html">http://s-match.org/spsm.html</a>
     */
    public void testMappingsExample() {
        IContextMapping<INode> mappings = matchFunctions("Courses(College of Arts and Sciences(Earth and Atmospheric Sciences,History(Latin America History,America History,Ancient European History),Computer Science))",
                "Course(College of Arts and Sciences(Earth Sciences(Geophysics,Geological Sciences),Computer Science,History(History of Americas,Ancient and Medieval History)))");

        //TODO Juan, great beginning!
        assertFalse(null == mappings);
        assertFalse(mappings.isEmpty());
        //TODO Juan, hard code these 6, this way it will break if something will change
        assertTrue(mappings.size() == 6);
        assertTrue(mappings.getSimilarity() > 0.2);

        System.out.println("Example test finished");

    }


    /**
     * Matches two trees in function-like structures. The function loads the context for each fn and
     * then performs the pre-processing. Finally performs the rendering
     *
     * @param fn1 the first tree
     * @param fn2 the second tree
     * @return the resulting mapping context
     */
    private IContextMapping<INode> matchFunctions(String fn1, String fn2) {
        IContextMapping<INode> mapping = null;
        try {
            IContext sourceContext = mm.loadContext(fn1);
            IContext targetContext = mm.loadContext(fn2);

            // linguistic pre-processing
            mm.offline(sourceContext);
            mm.offline(targetContext);

            // match
            mapping = mm.online(sourceContext, targetContext);
            //TODO Juan, unit tests should produce as little side effects as possible. Do you need to render the result?
            mm.renderMapping(mapping, filePath + "result-default.txt");
            print(mapping);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

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
