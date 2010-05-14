package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISynset;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.Vector;

/**
 * Implements WNHierarchy matcher.
 * see Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNHierarchy extends Configurable implements ISenseGlossBasedElementLevelSemanticMatcher {

    private static final Logger log = Logger.getLogger(WNHierarchy.class);

    // depth
    private static final String DEPTH_KEY = "depth";
    private int depth = 2;

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            if (newProperties.containsKey(DEPTH_KEY)) {
                depth = Integer.parseInt(newProperties.getProperty(DEPTH_KEY)) ;
            } else {
                final String errMessage = "Cannot find configuration key " + DEPTH_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            properties.clear();
            properties.putAll(newProperties);
        }
    }

    /**
     * Matches two strings with WNHeirarchy matcher.
     *
     * @param source gloss of source label
     * @param target gloss of target label
     * @return synonym or IDk relation
     */
    public char match(ISynset source, ISynset target) {
        Vector<ISynset> sourceVector = getAncestors(source, depth);
        Vector<ISynset> targetVector = getAncestors(target, depth);
        targetVector.retainAll(sourceVector);
        if (targetVector.size() > 0)
            return IMappingElement.EQUIVALENCE;
        else
            return IMappingElement.IDK;
    }

    private Vector<ISynset> getAncestors(ISynset node, int depth) {
        return node.getParents(depth);
    }
}
