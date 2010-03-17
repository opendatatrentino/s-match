package it.unitn.disi.smatch.loaders;

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
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 * @author Juan Pane pane@disi.unitn.it
 */
public class TABLoader implements ILoader {

	/*
	 * refactored, deleted class level variables and changed rootPath from array[]
	 * to allay list
	 */
	//TODO the loader could be made static, but this requires a change in the ILoader interface


    private static final Logger log = Logger.getLogger(TABLoader.class);


    public IContext loadContext(String fileName) {
    	IContext result = null;
    	try {
    		BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            try {
                result = process(input);
            } finally {
                input.close();
            }
        } catch (Exception e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Exception: " + e.getMessage(), e);
            }
        }
        return result;
    }


    /**
     * Processes the file loading the content. This content is supposed to be
     * formatted in tab indented format.
     *
     * @param input		Reader for the input file
     * @return			the loaded IContext
     * @throws IOException
     */
    private IContext process(BufferedReader input) throws IOException {
    	IContext result = new Context();
    	ArrayList<String> rootPath = new ArrayList<String>();

    	//loads the root node
    	String fatherConceptId = result.newNode(input.readLine(), null);
        rootPath.add(fatherConceptId);


        String fatherId;
        int old_depth = 0;
        String line;
        while ((line = input.readLine()) != null &&
                !line.startsWith("#") &&
                !line.equals("")) {

            int int_depth = numOfTabs(line);
            String name = line.substring(int_depth);
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
     * @param line	the string of the each line of file
     * @return 	the number of tabs at the beginning of the line
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
     * @param index		position to be filled
     * @param array		array to be modified
     * @param nodeID	value to be set
     */
    private static void setArrayNodeID(int index, ArrayList<String> array, String nodeID){
        if(index < array.size() ){
        	array.set(index, nodeID);
        } else {
        	array.add(index, nodeID);
        }
    }

}