package it.unitn.disi.smatch.data.mappings;

import java.util.Vector;

/**
 * Interface for mappings.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMapping {

    boolean contains(IMappingElement me);

    Vector<IMappingElement> getMapping();

    void setMapping(Vector<IMappingElement> map);

    void add(IMappingElement m);

    void add(IMapping m);

    //compare the mapping with the gold standard
    void compare(IMapping m);

    int getSize();

    void toFile(String fileName);

    void loadFromFile(String fileName);

    String compareWORel(IMapping m);

    void reportStats();
}
