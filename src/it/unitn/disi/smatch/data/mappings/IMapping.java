package it.unitn.disi.smatch.data.mappings;

import java.util.Set;

/**
 * Interface for mappings.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMapping<T> extends Set<IMappingElement<T>> {

    /**
     * Returns relation between source and target.
     *
     * @param source source
     * @param target target
     * @return relation between source and target
     */
    char getRelation(T source, T target);

    /**
     * Sets relation between source and target.
     *
     * @param source   source
     * @param target   target
     * @param relation relation
     * @return true if the mapping was modified
     */
    boolean setRelation(T source, T target, char relation);
}
