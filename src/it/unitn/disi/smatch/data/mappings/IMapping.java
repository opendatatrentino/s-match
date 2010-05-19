package it.unitn.disi.smatch.data.mappings;

import java.util.Set;

/**
 * Interface for mappings.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMapping<T> extends Set<IMappingElement<T>> {

    char getRelation(T source, T target);

    boolean setRelation(T source, T target, char relation);
}
