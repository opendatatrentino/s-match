package it.unitn.disi.annotation.pipelines.components;

import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.pipelines.LabelPipelineComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts tags in the dataset and print them sorted by frequency.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TagCounter extends LabelPipelineComponent {

    private final static HashMap<String, Long> tagCounts = new HashMap<String, Long>();

    public void process(ILabel instance) {
        for (IToken t : instance.getTokens()) {
            Long count = tagCounts.get(t.getPOSTag());
            if (null == count) {
                count = 0L;
            }
            count++;
            tagCounts.put(t.getPOSTag(), count);
        }
    }

    public static Map<String, Long> getTagCounts() {
        return tagCounts;
    }
}