package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.data.IContext;

/**
 * Loader for CTXML format.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CTXMLContextLoader extends BaseContextLoader implements IContextLoader {

    protected IContext internalLoad(String fileName) throws ContextLoaderException {
        CTXML parserSource = CTXML.getInstance();
        return parserSource.parseAndLoadContext(fileName);
    }
}