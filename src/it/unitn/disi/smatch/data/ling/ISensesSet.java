package it.unitn.disi.smatch.data.ling;

import java.util.List;

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

    List<String> getSenseList();

    void setSenseList(List<String> senseList);

    //Add new senses except repeated ones

    void addNewSenses(List<String> newSenses);

    //Is empty?

    boolean hasSomeSenses();
}
