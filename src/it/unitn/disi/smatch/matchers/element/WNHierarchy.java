package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISynset;

import java.util.Vector;

/**
 * Implements WNHierarchy matcher.
 * see Element Level Semantic matchers paper for more details.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WNHierarchy implements ISenseGlossBasedElementLevelSemanticMatcher {
    private static ILinguisticOracle ILO;

    static int depth = 2;

    /**
     * Match two string with WNHeirarchy matcher.
     *
     * @param source1 gloss of source label
     * @param target1 gloss of target label
     * @return synonym or IDk relation
     */
    public char match(ISynset source1, ISynset target1) {
        Vector<ISynset> sourceVector = new Vector<ISynset>();
        Vector<ISynset> targetVector = new Vector<ISynset>();
        sourceVector = getAncestors(source1, sourceVector, depth);
        targetVector = getAncestors(target1, targetVector, depth);
        targetVector.retainAll(sourceVector);
        if (targetVector.size() > 0)
            return MatchManager.SYNOMYM;
        else
            return MatchManager.IDK_RELATION;
    }

    private Vector<ISynset> getAncestors(ISynset node, Vector<ISynset> ve, int depth) {
        return node.getParents(depth);
    }
    // TODO more than one main is confusing.
    public static void main(String[] args) {
        WNHierarchy wnh = new WNHierarchy();
        ILO = MatchManager.getLinguisticOracle();

        ISynset sourceSynset = ILO.getISynset("n#434536");
        ISynset targetSynset = ILO.getISynset("n#490571");

        System.out.println(wnh.match(sourceSynset, targetSynset));
    }
}
