import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Demonstrates S-Match API demo.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class SMatchAPIDemo {

    private final static String CONFIG_FILE_PREFIX = ".." + File.separator + "conf" + File.separator;

    public static void main(String[] args) throws ConfigurableException, IOException {
        example1();
        example2();
    }

    /**
     * Loads the classifications from files, matches them and renders the results.
     *
     * @throws SMatchException SMatchException
     * @throws IOException     IOException
     */
    public static void example1() throws ConfigurableException, IOException {
        System.out.println("Starting example 1...");
        System.out.println("Creating MatchManager...");
        IMatchManager mm = MatchManager.getInstance();

        Properties config = new Properties();
        config.load(new FileInputStream(CONFIG_FILE_PREFIX + "s-match-Tab2XML.properties"));

        System.out.println("Configuring MatchManager from " + CONFIG_FILE_PREFIX + "s-match-Tab2XML.properties" + "...");
        mm.setProperties(config);

        System.out.println("Loading source context...");
        IContext s = mm.loadContext("../test-data/cw/c.txt");

        System.out.println("Loading target context...");
        IContext t = mm.loadContext("../test-data/cw/w.txt");

        config.load(new FileInputStream(CONFIG_FILE_PREFIX + "s-match.properties"));
        System.out.println("Configuring MatchManager from " + CONFIG_FILE_PREFIX + "s-match.properties" + "...");
        mm.setProperties(config);

        System.out.println("Preprocessing source context...");
        mm.offline(s);

        System.out.println("Preprocessing target context...");
        mm.offline(t);

        System.out.println("Matching...");
        IContextMapping<INode> result = mm.online(s, t);

        System.out.println("Rendering results...");
        mm.renderMapping(result, "../test-data/cw/result.txt");

        System.out.println("Done");
    }

    /**
     * Creates the classifications, matches them and processes the results.
     *
     * @throws SMatchException SMatchException
     * @throws IOException     IOException
     */
    public static void example2() throws ConfigurableException, IOException {
        System.out.println("Starting example 2...");
        System.out.println("Creating MatchManager...");
        IMatchManager mm = MatchManager.getInstance();

        Properties config = new Properties();
        config.load(new FileInputStream(CONFIG_FILE_PREFIX + "s-match.properties"));

        System.out.println("Configuring MatchManager from " + CONFIG_FILE_PREFIX + "s-match.properties" + "...");
        mm.setProperties(config);

        String example = "Courses";
        System.out.println("Creating source context...");
        IContext s = mm.createContext();
        s.createRoot(example);

        System.out.println("Creating target context...");
        IContext t = mm.createContext();
        INode root = t.createRoot("Course");
        INode node = root.createChild("College of Arts and Sciences");
        node.createChild("English");

        node = root.createChild("College Engineering");
        node.createChild("Civil and Environmental Engineering");

        System.out.println("Preprocessing source context...");
        mm.offline(s);

        System.out.println("Preprocessing target context...");
        mm.offline(t);

        System.out.println("Matching...");
        IContextMapping<INode> result = mm.online(s, t);

        System.out.println("Processing results...");
        System.out.println("Printing matches to: " + example);
        for (IMappingElement<INode> e : result) {
            System.out.println("\t" + e.getRelation() + "\t" + e.getTarget().getNodeData().getName());
        }

        System.out.println("Done");
    }

}
