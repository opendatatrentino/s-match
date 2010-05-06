package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.INode;

/**
 * Interface for a mapping element.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingElement {

    // Relations abbreviations
    char EQUIVALENCE = '=';
    char LESS_GENERAL = '<';
    char MORE_GENERAL = '>';
    char DISJOINT = '!';

    // relations for work with minimal links
    char ENTAILED_LESS_GENERAL = 'L';
    char ENTAILED_MORE_GENERAL = 'M';
    char ENTAILED_DISJOINT = 'X';

    char IDK = '?';
    char ERASED_LG = 'L';
    char ERASED_MG = 'M';

    INode getSourceNode();

    void setSourceNode(INode newSource);

    INode getTargetNode();

    void setTargetNode(INode newTarget);

    char getRelation();

    void setRelation(char newRelation);

}