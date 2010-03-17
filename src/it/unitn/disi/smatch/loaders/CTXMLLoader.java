package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.SMatchException;
import org.apache.log4j.Logger;

/**
 * Loader for CTXML format.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CTXMLLoader implements ILoader {

    public IContext loadContext(String fileName) throws SMatchException {
        CTXML parserSource = CTXML.getInstance();
        return parserSource.parseAndLoadContext(fileName);
    }

}
