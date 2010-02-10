package it.unitn.disi.smatch.matchers.element;

/**
 * Interface for string-based element-level matchers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IStringBasedElementLevelSemanticMatcher {

    /**
     * Returns a relation between source and target strings.
     *
     * @param source source string
     * @param target target string
     * @return a relation between source and target
     */
    char match(String source, String target);
}
