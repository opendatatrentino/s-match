package it.unitn.disi.smatch.data.ling;

/**
 * Interface for a dictionary sense.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ISense {

    char getPos();

    void setPos(char pos);

    long getId();

    void setId(long id);
}