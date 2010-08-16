package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * Renders nothing.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ZeroMappingRenderer extends Configurable implements IMappingRenderer {

    public void render(IContextMapping<INode> mapping, String outputFile) throws MappingRendererException {
        //does nothing
    }

}
