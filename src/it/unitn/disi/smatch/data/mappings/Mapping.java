package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.MatchManager;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Vector;

/**
 * Holds a mapping.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Mapping implements IMapping {

    Vector<IMappingElement> map = null;

    public boolean contains(IMappingElement me) {
        return map.contains(me);

    }

    public Vector<IMappingElement> getMapping() {
        return map;
    }

    public void setMapping(Vector<IMappingElement> map) {
        this.map = map;
    }


    public Mapping(Vector<IMappingElement> map) {
        this.map = map;
    }

    public Mapping() {
        map = new Vector<IMappingElement>();
    }

    public void add(IMappingElement m) {
        map.add(m);
    }

    public void add(IMapping m) {
        if (m != null) {
            map.addAll(m.getMapping());
        }
    }

    //compare the mapping with the gold standard
    public void compare(IMapping m) {
        System.out.println("Mapping comparison...");
        double precision = 0;
        double recall = 0;
        double overall = 0;
        double f_measure = 0;
        double fallout = 0;
        double result = 0;
        int found = this.getSize();
        int expected = m.getSize();
        int correct = 0;
        Vector<IMappingElement> tmp = m.getMapping();
        for (IMappingElement source : map) {
            for (IMappingElement target : tmp) {
                if (target.equals(source)) {
                    correct++;
                    System.out.println(source);
                    System.out.println(target);
                }
            }
        }
        DecimalFormat df = new DecimalFormat("00.0000");
        precision = (double) correct / (double) found;
        recall = (double) correct / (double) expected;
        fallout = (double) (found - correct) / (double) found;
        f_measure = (2 * precision * recall) / (precision + recall);
        overall = recall * (2 - (1 / precision));
        result = recall / precision;
        System.out.println("Presision " + df.format(precision));
        System.out.println("Recall    " + df.format(recall));
        System.out.println("Overall   " + df.format(overall));
        System.out.println("F-measure " + df.format(f_measure));
        System.out.println("Fallout   " + df.format(fallout));
        System.out.println("Result    " + df.format(result));
    }

    public String compareWORel(IMapping m) {
        double precision = 0;
        double recall = 0;
        double overall = 0;
        double f_measure = 0;
        double fallout = 0;
        double result = 0;
        int found = this.getSize();
        int expected = m.getSize();
        int correct = 0;
        Vector<IMappingElement> tmp = m.getMapping();
        for (IMappingElement source : map) {
            for (IMappingElement target : tmp) {
                if (target.weakEquals(source)) {
                    correct++;
                }
            }
        }
        found = correct;
        if (found == 0) {
            precision = 1;
        } else {
            precision = (double) correct / (double) found;
        }
        recall = (double) correct / (double) expected;
        fallout = (double) (found - correct) / (double) found;
        f_measure = 2 * precision * recall / (precision + recall);
        overall = recall * (2 - (1 / precision));
        result = recall / precision;
//		System.out.println("Found " + found);
//		System.out.println("Expected " + expected);
//		System.out.println("Correct " + correct);
//		System.out.println("Presision " + precision);
//		System.out.println("Recall    " + recall);
//		System.out.println("Overall   " + overall);
//		System.out.println("F-measure " + f_measure);
//		System.out.println("Fallout   " + fallout);
//		System.out.println("Result    " + result);
        return (found + " " + expected + " " + correct + " " + precision + " " + recall + " " + overall + " " + f_measure + " " + fallout + " " + result);
    }

    public int getSize() {
        return map.size();
    }

    public void sort() {
        Collections.sort(map);
    }

    public void toFile(String fileName) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            for (IMappingElement mappingElement : map) {
                /*                char rel = mappingElement.getRelation();
                if ((rel == MatchManager.IDK_RELATION) || (rel == ' '))
                    ;
                else*/
                {
                    out.write(mappingElement.toString());
                    out.newLine();
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(String fileName) {
        System.out.println("Loading mapping from " + fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
            for (String s = br.readLine(); null != s; s = br.readLine()) {
                if (!s.trim().startsWith("#")) {
                    if (s.trim().length() > 5) {
                        if (-1 == s.indexOf("null null")) {
                            if (-1 == s.indexOf("nullnull")) {
                                add(new MappingElement(s));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Loaded mapping");
    }

    public void reportStats() {
        System.out.println("Mapping size: " + map.size());
        int eq = 0;
        int dj = 0;
        int mg = 0;
        int lg = 0;
        int un = 0;
        for (IMappingElement me : map) {
            switch (me.getRelation()) {
                case MatchManager.SYNOMYM: {
                    eq++;
                    break;
                }
                case MatchManager.OPPOSITE_MEANING: {
                    dj++;
                    break;
                }
                case MatchManager.MORE_GENERAL_THAN: {
                    mg++;
                    break;
                }
                case MatchManager.LESS_GENERAL_THAN: {
                    lg++;
                    break;
                }
                default: {
                    un++;
                    break;
                }
            }
        }
        System.out.println("EQ: " + eq);
        System.out.println("DJ: " + dj);
        System.out.println("MG: " + mg);
        System.out.println("LG: " + lg);
        System.out.println("UN: " + un);
    }
    // TODO more than one main function is confusing
    public static void main(String[] args) {
        if (2 > args.length) {
            System.out.println("Usage: Mapping stats|compare source target");
        } else {
            if ("stats".equals(args[0])) {
                Mapping m = new Mapping();
                m.loadFromFile(args[1]);
                m.reportStats();
            } else {
                if ("compare".equals(args[0])) {
                    Mapping source = new Mapping();
                    Mapping target = new Mapping();
                    source.loadFromFile(args[1]);
                    source.reportStats();
                    target.loadFromFile(args[2]);
                    target.reportStats();
                    source.compare(target);
                }
            }
        }
    }
}
