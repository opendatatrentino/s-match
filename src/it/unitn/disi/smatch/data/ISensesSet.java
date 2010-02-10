package it.unitn.disi.smatch.data;

import java.util.Vector;

/**
 * An interface to a set of senses.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISensesSet {
    long[] getIntSenses();

    void setIntSenses(long[] intSenses);

    char[] getPOSSenses();

    void setPOSSenses(char[] POSSenses);

    void convertSenses();

    //Set up lemmas after sense filtering
    void updateSenseList();

    //Set lemmas for sense filtering
    void addToRefinedSenses(String toAdd);

    //Check whether any lemmas refined
    boolean isRefinedSensesEmpty();

    //Get senses
    Vector<String> getSenseList();

    void setSenseList(Vector<String> senseList);

    //Add new senses exept repeated ones
    void addNewSenses(Vector<String> newSenses);

    //Is empty?
    boolean hasSomeSenses();
}
