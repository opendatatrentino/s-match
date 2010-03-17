package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

import java.io.IOException;

/**
 * Interface for mapping loaders.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingLoader {

	/**
	 * Loads the matrix of semantic result of input labels in a file.
	 *
	 * @param ctxSource interface of data structure of source
	 * @param ctxTarget interface of data structure of target
	 * @param fileName the file where the result will be written
	 * @return a matrix which contains the relation between concept of nodes
	 * @throws IOException
	 */
    public IMatchMatrix loadMapping(IContext ctxSource, IContext ctxTarget, String fileName) throws IOException;
}
