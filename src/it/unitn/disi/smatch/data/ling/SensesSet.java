package it.unitn.disi.smatch.data.ling;

import java.util.*;

/**
 * Default SensesSet representation.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SensesSet implements ISensesSet {

    private List<String> senseList = new ArrayList<String>();
    //senses obtained after sense refining
    private List<String> refinedSenses = new ArrayList<String>();

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

    public SensesSet() {
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
            senseList = new ArrayList<String>();
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

    public List<String> getSenseList() {
        return senseList;
    }

    public void setSenseList(List<String> senseList) {
        this.senseList.clear();
        this.senseList.addAll(senseList);
    }

    //Add new senses except repeated ones

    public void addNewSenses(List<String> newSenses) {
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
        StringBuilder stringSensesSet = new StringBuilder();
        for (String senseId : senseList) {
            stringSensesSet.append(senseId).append(" ");
        }
        return stringSensesSet.toString().trim();
    }
}
