package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.data.IContext;

/**
 * Loader for CTXML format.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CTXMLContextLoader extends BaseContextLoader implements IContextLoader {

    private CTXML parser;

    public CTXMLContextLoader() throws ContextLoaderException {
        parser = new CTXML();
    }

    protected IContext internalLoad(String fileName) throws ContextLoaderException {
        return parser.parseAndLoadContext(fileName);
    }
}