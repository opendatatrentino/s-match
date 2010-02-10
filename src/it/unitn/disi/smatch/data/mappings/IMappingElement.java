package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.INode;

/**
 * Interface for a mapping element.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingElement extends Comparable {
    String getSourceEntity();

    void setSourceEntity(String sourceEntity);

    String getTargetEntity();

    void setTargetEntity(String targetEntity);

    char getRelation();

    void setRelation(char relation);

    boolean weakEquals(Object o);

//    public boolean equals(Object o);

    public double getEq();

    public double getLg();

    public double getMg();

    public INode getSourceNode();

    public INode getTargetNode();
}
