package it.unitn.disi.smatch.loaders.mapping;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;

/**
 * Interface for mapping loaders.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingLoader extends IConfigurable {

    /**
     * Loads the mapping.
     *
     * @param source   interface of data structure of source
     * @param target   interface of data structure of target
     * @param fileName file with a mapping
     * @return interface to a mapping
     * @throws MappingLoaderException MappingLoaderException
     */
    IContextMapping<INode> loadMapping(IContext source, IContext target, String fileName) throws MappingLoaderException;
}