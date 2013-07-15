package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Writes the mapping eXcluding Top node mappings.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PlainXTopMappingRenderer extends PlainMappingRenderer {

    private static final Logger log = Logger.getLogger(PlainXTopMappingRenderer.class);

    @Override
    protected void process(IContextMapping<INode> mapping, BufferedWriter out) throws IOException {
        for (IMappingElement<INode> mappingElement : mapping) {
            if (mappingElement.getSource().hasParent() && mappingElement.getTarget().hasParent()) {
                String sourceConceptName = getNodePathToRoot(mappingElement.getSource());
                String targetConceptName = getNodePathToRoot(mappingElement.getTarget());
                char relation = mappingElement.getRelation();

                out.write(sourceConceptName + "\t" + relation + "\t" + targetConceptName + "\n");

                countRelation(relation);
                reportProgress();
            }
        }
    }
}