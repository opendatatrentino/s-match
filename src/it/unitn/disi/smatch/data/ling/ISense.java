package it.unitn.disi.smatch.data.ling;

/**
 * Interface for a dictionary sense.
 *
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public interface ISense {

    char getPos();

    void setPos(char pos);

    long getId();

    void setId(long id);
}