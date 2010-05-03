package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.mappings.IMapping;

/**
 * An interface for mapping renderers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingRenderer {

    /**
     * Saves the mapping into a file.
     *
     * @param mapping    a mapping to render
     * @param outputFile an output file
     * @throws SMatchException SMatchException
     */
    public void render(IMapping mapping, String outputFile) throws SMatchException;
}
