package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.components.Configurable;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;
//import org.sat4j.tools.RemiUtils;

import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.StringReader;
/**
 * // TODO Need comments.
 */

public class SAT4J extends Configurable implements ISATSolver {
    static String test = "p cnf 2 2\n1 2 0\n-1 0\n";
    static String test1 = "p cnf 6 6\n-1 3 0\n1 2 0\n-3 0\n-4 0\n5 0\n6 0\n";
    static String test2 = "p cnf 8 13\n-1 5 0\n" +
            "1 -5 0\n" +
            "2 -6 0\n" +
            "6 -2 0\n" +
            "-7 3 0\n" +
            "-3 7 0\n" +
//            "8 -4 0\n" +
//            "8 3 0\n" +
            "4 -8 0\n" +
            "1 2 0\n" +
            "3 4 0\n" +
            "-6 -8 0\n" +
            "-8 -5 0\n" +
            "-6 -7 0\n" +
            "-7 -5 0\n";

    static String test3 = "p cnf 6 8\n-1 4 0\n1 -4 0\n-3 6 0\n3 -6 0\n1 0\n2 0\n3 0\n-4 -5 -6 0\n";


    public boolean isSatisfiable(String input) {
        ISolver solver = new ModelIterator(SolverFactory.newMiniLearning());
        DimacsReader reader = new DimacsReader(solver);
// CNF filename is given on the command line
        try {
            LineNumberReader lnr = new LineNumberReader(new BufferedReader(new StringReader(input)));
            reader.parseInstance(lnr);
            return solver.isSatisfiable();

        } catch (ParseFormatException e) {
            System.out.println("parse error");
            e.printStackTrace();
        } catch (ContradictionException e) {
            return false;
//                System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");
        }
        return false;

    }

    public void model(String input) {
        ISolver solver = new ModelIterator(SolverFactory.newMiniLearning());
        DimacsReader reader = new DimacsReader(solver);
// CNF filename is given on the command line
        try {
            LineNumberReader lnr = new LineNumberReader(new BufferedReader(new StringReader(input)));
            reader.parseInstance(lnr);
      //      System.out.println(RemiUtils.backbone(solver));

            while (solver.isSatisfiable()) {
                int[] m = solver.model();
                for (int i1 : m) {
                    System.out.print(i1 + ",");
                }
                System.out.println();
            }
//            if (solver.isSatisfiable()) {
            //System.out.println(RemiUtils.backbone(solver));
//                return true;
//            } else {
//                return false;
//            }

        } catch (ParseFormatException e) {
            System.out.println("parse error");
            e.printStackTrace();
        } catch (ContradictionException e) {
            //return false;
//                System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");
        }
        //return false;

    }

    // TODO It is confusing more than one main function. remove it.
    public static void main(String[] args) {
        SAT4J s4j = new SAT4J();
        s4j.model(test3);
    }

}
