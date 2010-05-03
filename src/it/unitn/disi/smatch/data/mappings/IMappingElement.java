package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.INode;

/**
 * Interface for a mapping element.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingElement {

    INode getSourceNode();

    void setSourceNode(INode newSource);

    INode getTargetNode();

    void setTargetNode(INode newTarget);

    char getRelation();

    void setRelation(char newRelation);

}
