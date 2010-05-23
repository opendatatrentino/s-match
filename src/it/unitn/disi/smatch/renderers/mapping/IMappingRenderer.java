package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * An interface for mapping renderers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingRenderer extends IConfigurable {

    /**
     * Saves the mapping into a file.
     *
     * @param mapping    a mapping to render
     * @param outputFile an output file or DB connection
     * @throws MappingRendererException MappingRendererException
     */
    void render(IContextMapping<INode> mapping, String outputFile) throws MappingRendererException;
}
