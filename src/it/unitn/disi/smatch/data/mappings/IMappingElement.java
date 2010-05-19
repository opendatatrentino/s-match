package it.unitn.disi.smatch.data.mappings;

/**
 * Interface for a mapping element.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingElement<T> {

    // Relations abbreviations
    char EQUIVALENCE = '=';
    char LESS_GENERAL = '<';
    char MORE_GENERAL = '>';
    char DISJOINT = '!';

    // relations for minimal links
    char ENTAILED_LESS_GENERAL = 'L';
    char ENTAILED_MORE_GENERAL = 'M';
    char ENTAILED_DISJOINT = 'X';

    char IDK = '?';

    T getSource();

    T getTarget();

    char getRelation();
}