package it.unitn.disi.smatch.data;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Default SensesSet representation.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SensesSet implements ISensesSet {
    //senses
    private Vector<String> senseList;
    //senses obtained after sense refining
    private Vector<String> refinedSenses = new Vector<String>();

    private long[] intSenses = null;
    private char[] POSSenses = null;

    public long[] getIntSenses() {
        return intSenses;
    }

    public void setIntSenses(long[] intSenses) {
        this.intSenses = intSenses;
    }

    public char[] getPOSSenses() {
        return POSSenses;
    }

    public void setPOSSenses(char[] POSSenses) {
        this.POSSenses = POSSenses;
    }

    public SensesSet(Vector<String> senseList) {
        this.senseList = senseList;
    }

    //Used for integer array representation of WordNet
    public void convertSenses() {
        if ((intSenses == null) || (POSSenses == null)) {
            intSenses = new long[senseList.size()];
            POSSenses = new char[senseList.size()];
            for (int i = 0; i < senseList.size(); i++) {
                String s = senseList.get(i);
                StringTokenizer stringTokenizer = new StringTokenizer(s, "#");
                String pos = stringTokenizer.nextToken();
                String number = stringTokenizer.nextToken();
                POSSenses[i] = pos.charAt(0);
                if (!number.startsWith("000000"))
                    intSenses[i] = Long.parseLong(number);
                else
                    intSenses[i] = 0;
            }
            //remove this line after scalability  test
            senseList = new Vector<String>();
        }
    }

    //Set up lemmas after sense filtering
    public void updateSenseList() {
        senseList = refinedSenses;
    }

    //Set lemmas for sense filtering
    public void addToRefinedSenses(String toAdd) {
        if (!refinedSenses.contains(toAdd))
            refinedSenses.add(toAdd);
    }

    //Check whether any lemmas refined
    public boolean isRefinedSensesEmpty() {
        return refinedSenses.isEmpty();
    }

    //Get senses
    public Vector<String> getSenseList() {
        return senseList;
    }

    public void setSenseList(Vector<String> senseList) {
        this.senseList = senseList;
    }

    //Add new senses except repeated ones
    public void addNewSenses(Vector<String> newSenses) {
        for (String toAdd : newSenses) {
            if (!senseList.contains(toAdd))
                senseList.add(toAdd);
        }
    }

    //Is empty?
    public boolean hasSomeSenses() {
        return (senseList.size() > 0);
    }

    public String toString() {
        StringBuffer stringSensesSet = new StringBuffer();
        for (String senseId : senseList) {
            stringSensesSet.append(senseId).append(" ");
        }
        return stringSensesSet.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SensesSet)) return false;

        final SensesSet sensesSet = (SensesSet) o;

        if (!Arrays.equals(POSSenses, sensesSet.POSSenses)) return false;
        if (!Arrays.equals(intSenses, sensesSet.intSenses)) return false;
        if (senseList != null ? !senseList.equals(sensesSet.senseList) : sensesSet.senseList != null) return false;

        return true;
    }
}
