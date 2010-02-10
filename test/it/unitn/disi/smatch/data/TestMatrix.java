package it.unitn.disi.smatch.data;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.utils.SMatchUtils;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Tests for a matrix.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class TestMatrix {

    static {
        SMatchUtils.configureLog4J();
    }

    //logger
    private static final Logger log = Logger.getLogger(TestMatrix.class);

    public void testMatrix(IMatchMatrix sm) {
        char[][] test = {
                {MatchManager.IDK_RELATION, MatchManager.IDK_RELATION,
                        MatchManager.SYNOMYM, MatchManager.IDK_RELATION, MatchManager.IDK_RELATION},
                {MatchManager.MORE_GENERAL_THAN, MatchManager.LESS_GENERAL_THAN,
                        MatchManager.IDK_RELATION, MatchManager.IDK_RELATION, MatchManager.IDK_RELATION}};

        log.info("Init");
        sm.init(2, 5, 10);
        log.info("Done");
        log.info("Assignment");
        for (int i = 0; i < test.length; i++) {
            for (int j = 0; j < test[i].length; j++) {
                sm.setElement(i, j, test[i][j]);
            }
            sm.endOfRow();
        }
        log.info("Done");

        log.info("Test");
        for (int i = 0; i < test.length; i++) {
            for (int j = 0; j < test[i].length; j++) {
                assertThat(sm.getElement(i, j), equalTo(test[i][j]));
            }
        }
        log.info("Done");
    }

    public void testReverseMatrix(IMatchMatrix sm) {
        char[][] test = {
                {MatchManager.IDK_RELATION, MatchManager.IDK_RELATION,
                        MatchManager.SYNOMYM, MatchManager.IDK_RELATION, MatchManager.IDK_RELATION},
                {MatchManager.MORE_GENERAL_THAN, MatchManager.LESS_GENERAL_THAN,
                        MatchManager.IDK_RELATION, MatchManager.IDK_RELATION, MatchManager.IDK_RELATION}};


        sm.init(2, 5, 10);
        for (int i = test.length - 1; -1 < i; i--) {
            for (int j = test[i].length - 1; -1 < j; j--) {
                sm.setElement(i, j, test[i][j]);
            }
            sm.endOfRow();
        }

        for (int i = 0; i < test.length; i++) {
            for (int j = 0; j < test[i].length; j++) {
                assertThat(sm.getElement(i, j), equalTo(test[i][j]));
            }
        }
    }


    public void testRow1(IMatchMatrix sm) {
        char[] test = {MatchManager.IDK_RELATION, MatchManager.IDK_RELATION,
                MatchManager.SYNOMYM, MatchManager.IDK_RELATION, MatchManager.IDK_RELATION};

        sm.init(1, 5, 10);
        for (int i = 0; i < test.length; i++) {
            sm.setElement(0, i, test[i]);
        }
        sm.endOfRow();

        for (int i = 0; i < test.length; i++) {
            assertThat(sm.getElement(0, i), equalTo(test[i]));
        }
    }

    public void testRow2(IMatchMatrix sm) {
        char[] test = {MatchManager.MORE_GENERAL_THAN, MatchManager.LESS_GENERAL_THAN,
                MatchManager.IDK_RELATION, MatchManager.IDK_RELATION, MatchManager.IDK_RELATION};

        sm.init(1, 5, 10);
        for (int i = 0; i < test.length; i++) {
            sm.setElement(0, i, test[i]);
        }
        sm.endOfRow();

        for (int i = 0; i < test.length; i++) {
            assertThat(sm.getElement(0, i), equalTo(test[i]));
        }
    }

    public void testMtx(String fileName, IMatchMatrix sm) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName)), "ASCII"));
            try {
                String line = null;
                /*
                * readLine is a bit quirky :
                * it returns the content of a line MINUS the newline.
                * it returns null only for the END of the stream.
                * it returns an empty String if two newlines appear in a row.
                */
                //first line, comment
                line = input.readLine();
                //second line, dimensions and #
                line = input.readLine();
                String[] pieces = line.split(" ");

                log.info("Init");
                sm.init(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2]));
                log.info("Done");

                log.info("Assignment");
                //lines
                int row;
                int col;
                int lastRow = 0;
                char el;
                while ((line = input.readLine()) != null) {
                    pieces = line.split(" ");
                    row = Integer.parseInt(pieces[0]) - 1;
                    col = Integer.parseInt(pieces[1]) - 1;
                    if (lastRow != row) {
                        sm.endOfRow();
                    }
                    el = MatchManager.SYNOMYM;
                    if (0 != (row % 2) * (col % 2)) {
                        el = MatchManager.OPPOSITE_MEANING;
                    }
                    sm.setElement(row, col, el);
                    lastRow = row;
                }
                sm.endOfRow();
                log.info("Done");
            }
            finally {
                input.close();
            }

            //test
            input = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName)), "ASCII"));
            try {
                String line = null;
                /*
                * readLine is a bit quirky :
                * it returns the content of a line MINUS the newline.
                * it returns null only for the END of the stream.
                * it returns an empty String if two newlines appear in a row.
                */
                //first line, comment
                line = input.readLine();
                //second line, dimensions and #
                line = input.readLine();
                String[] pieces = line.split(" ");

                log.info("Test");
                //lines
                int row;
                int col;
                char el;
                while ((line = input.readLine()) != null) {
                    pieces = line.split(" ");
                    row = Integer.parseInt(pieces[0]) - 1;
                    col = Integer.parseInt(pieces[1]) - 1;
                    el = MatchManager.SYNOMYM;
                    if (0 != (row % 2) * (col % 2)) {
                        el = MatchManager.OPPOSITE_MEANING;
                    }
                    assertThat(sm.getElement(row, col), equalTo(el));
                }
                log.info("Done");
            }
            finally {
                input.close();
            }
        }
        catch (IOException ex) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("IOException " + ex.getMessage(), ex);
            }
        }
    }
}