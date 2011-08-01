package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.oracles.ISynset;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.data.list.PointerTargetTree;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * WordNet-based synset implementation.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public class WordNetSynset implements ISynset {

    private Synset tmp;

    /**
     * Constructor class with sense input.
     *
     * @param sense input sense
     */
    public WordNetSynset(Synset sense) {
        tmp = sense;
    }


    public String getGloss() {
        return tmp.getGloss();
    }

    public List<String> getLemmas() {
        List<String> out = new ArrayList<String>();
        String lemmaToCompare;

        for (int i = 0; i < tmp.getWords().size(); i++) {
            lemmaToCompare = tmp.getWords().get(i).getLemma();
            out.add(lemmaToCompare);
        }

        return out;
    }

    public List<ISynset> getParents() {
        List<ISynset> out = new ArrayList<ISynset>();
        PointerTargetTree hypernyms = PointerUtils.getHypernymTree(tmp, 1);
        for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
            if (itr.hasNext()) {
                for (Object o : ((PointerTargetNodeList) itr.next())) {
                    Synset t = ((PointerTargetNode) o).getSynset();
                    if (!isEqual(tmp, t)) {
                        out.add(new WordNetSynset(t));
                    }
                }
            }
        }
        return out;
    }

    public List<ISynset> getParents(int depth) {
        List<ISynset> out = new ArrayList<ISynset>();
        PointerTargetTree hypernyms = PointerUtils.getHypernymTree(tmp, depth);
        for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
            if (itr.hasNext()) {
                for (Object o : ((PointerTargetNodeList) itr.next())) {
                    Synset t = ((PointerTargetNode) o).getSynset();
                    if (!isEqual(tmp, t)) {
                        out.add(new WordNetSynset(t));
                    }
                }
            }
        }
        return out;
    }

    public List<ISynset> getChildren() {
        List<ISynset> out = new ArrayList<ISynset>();
        PointerTargetTree hypernyms = PointerUtils.getHyponymTree(tmp, 1);
        for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
            if (itr.hasNext()) {
                for (Object o : ((PointerTargetNodeList) itr.next())) {
                    Synset t = ((PointerTargetNode) o).getSynset();
                    if (!isEqual(tmp, t)) {
                        out.add(new WordNetSynset(t));
                    }
                }
            }
        }
        return out;
    }

    public List<ISynset> getChildren(int depth) {
        List<ISynset> out = new ArrayList<ISynset>();
        PointerTargetTree hypernyms = PointerUtils.getHyponymTree(tmp, depth);
        for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
            if (itr.hasNext()) {
                for (Object o : ((PointerTargetNodeList) itr.next())) {
                    Synset t = ((PointerTargetNode) o).getSynset();
                    if (!isEqual(tmp, t)) {
                        out.add(new WordNetSynset(t));
                    }
                }
            }
        }
        return out;
    }

    public boolean isEqual(Synset source, Synset target) {
        long so = source.getOffset();
        long to = target.getOffset();
        String sourcePOS = source.getPOS().toString();
        String targetPOS = target.getPOS().toString();
        return ((sourcePOS.equals(targetPOS)) && (so == to));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WordNetSynset)) {
            return false;
        }

        final WordNetSynset wordNetSynset = (WordNetSynset) o;

        if (tmp != null ? !tmp.equals(wordNetSynset.tmp) : wordNetSynset.tmp != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (tmp != null ? tmp.hashCode() : 0);
    }
}