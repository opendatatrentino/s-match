package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.Context;
import it.unitn.disi.smatch.data.IContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Loads context from a tab-separated file.
 * Expects a single-rooted hierarchy, otherwise adds an artificial "Top" node.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 * @author Juan Pane pane@disi.unitn.it
 */
public class TabContextLoader extends BaseContextLoader implements IContextLoader {

    private static final Logger log = Logger.getLogger(TabContextLoader.class);

    protected IContext internalLoad(String fileName) throws ContextLoaderException {
        IContext result = null;
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            try {
                result = process(input);
            } finally {
                input.close();
            }
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        }
        return result;
    }


    /**
     * Processes the file loading the content. This content is supposed to be
     * formatted in tab indented format.
     *
     * @param input Reader for the input file
     * @throws IOException IOException
     * @return the loaded IContext
     */
    private IContext process(BufferedReader input) throws IOException {
        IContext result = new Context();
        ArrayList<String> rootPath = new ArrayList<String>();

        //loads the root node
        String fatherConceptId = result.newNode(input.readLine(), null);
        rootPath.add(fatherConceptId);

        int artificialLevel = 0;//flags that we added Top and need an increment in level
        String fatherId;
        int old_depth = 0;
        String line;
        while ((line = input.readLine()) != null &&
                !line.startsWith("#") &&
                !line.equals("")) {

            int int_depth = numOfTabs(line);
            String name = line.substring(int_depth);
            int_depth = int_depth + artificialLevel;
            if (int_depth == old_depth) {
                fatherId = rootPath.get(old_depth - 1);
                String newCID = result.newNode(name, fatherId);
                setArrayNodeID(int_depth, rootPath, newCID);
            } else if (int_depth > old_depth) {
                fatherId = rootPath.get(old_depth);
                String newCID = result.newNode(name, fatherId);
                setArrayNodeID(int_depth, rootPath, newCID);
                old_depth = int_depth;
            } else if (int_depth < old_depth) {
                if (0 == int_depth) {//looks like we got multiple roots in the input
                    artificialLevel = 1;
                    fatherId = result.newNode("Top", null);
                    rootPath.add(0, fatherId);
                    int_depth = 1;
                }
                fatherId = rootPath.get(int_depth - 1);
                String newCID = result.newNode(name, fatherId);
                setArrayNodeID(int_depth, rootPath, newCID);
                old_depth = int_depth;
            }
        }

        return result;
    }

    /**
     * Counts the number of tabs in the line.
     *
     * @param line the string of the each line of file
     * @return the number of tabs at the beginning of the line
     */
    private int numOfTabs(String line) {
        int close_counter = 0;
        while (close_counter < line.length() && '\t' == line.charAt(close_counter)) {
            close_counter++;
        }
        return close_counter;
    }

    /**
     * Sets the nodeID at a given position of the array.
     * Changes the current value if there is one, if there is no value, add a new one.
     *
     * @param index  position to be filled
     * @param array  array to be modified
     * @param nodeID value to be set
     */
    private static void setArrayNodeID(int index, ArrayList<String> array, String nodeID) {
        if (index < array.size()) {
            array.set(index, nodeID);
        } else {
            array.add(index, nodeID);
        }
    }
}