package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.data.Context;
import it.unitn.disi.smatch.data.IContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Loads context from a tab-separated file.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TABLoader implements ILoader {

    private static final Logger log = Logger.getLogger(TABLoader.class);

    private Context result = null;
    private BufferedReader input;
    private String[] rootPath = new String[50];//max depth

    public IContext loadContext(String fileName) {
        try {
            result = new Context();
            input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            try {
                String fatherConceptId = result.newNode("Top", null);
                rootPath[0] = fatherConceptId;
                process();
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

    private void process() throws IOException {
        input.readLine();//skip Top
        String fatherId;
        int old_depth = 0;
        String line;
        while ((line = input.readLine()) != null &&
                !line.startsWith("#") &&
                !line.equals("")) {

            int int_depth = numOfTabs(line);
            String name = line.substring(int_depth);
            if (int_depth == old_depth) {
                fatherId = rootPath[old_depth - 1];
                String newCID = result.newNode(name, fatherId);
                rootPath[int_depth] = newCID;
            } else if (int_depth > old_depth) {
                fatherId = rootPath[old_depth];
                String newCID = result.newNode(name, fatherId);
                rootPath[int_depth] = newCID;
                old_depth = int_depth;
            } else if (int_depth < old_depth) {
                fatherId = rootPath[int_depth - 1];
                String newCID = result.newNode(name, fatherId);
                rootPath[int_depth] = newCID;
                old_depth = int_depth;
            }
        }
    }

    private int numOfTabs(String in) {
        int close_counter = 0;
        while (close_counter < in.length() && '\t' == in.charAt(close_counter)) {
            close_counter++;
        }
        return close_counter;
    }
}