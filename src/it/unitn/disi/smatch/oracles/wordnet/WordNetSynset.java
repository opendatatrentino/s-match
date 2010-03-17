package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.oracles.ISynset;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;

import java.util.Iterator;
import java.util.Vector;

/**
 * WordNet-based synset implementation.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WordNetSynset implements ISynset {

    Synset tmp = null;

    /**
     * Constructor class with sense input.
     *
     * @param sense input sense
     */
    public WordNetSynset(Synset sense) {
        tmp = sense;
    }


    public String getGloss() {
        return tmp.getGloss();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Vector<String> getLemmas() {
        Vector<String> out = new Vector<String>();
        String lemmaToCompare = null;

        for (int i = 0; i < tmp.getWordsSize(); i++) {
            lemmaToCompare = tmp.getWord(i).getLemma();
            out.add(lemmaToCompare);
        }

        return out;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Vector<ISynset> getParents() {
        Vector<ISynset> out = new Vector<ISynset>();
        PointerTargetTree hypernyms = null;
        PointerUtils pu = PointerUtils.getInstance();
        try {
            hypernyms = pu.getHypernymTree(tmp, 1);
            for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
//                itr.next();
                if (itr.hasNext()) {
                    for (Iterator itr1 = ((PointerTargetNodeList) itr.next()).iterator(); itr1.hasNext();) {
                        Synset t = ((PointerTargetNode) itr1.next()).getSynset();
                        if (!isEqual(tmp, t)) {

                            WordNetSynset wns = new WordNetSynset(t);
                            ISynset is = (ISynset) wns;
                            out.add(is);
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        out.remove(tmp);
        return out;
    }

    public Vector<ISynset> getParents(int depth) {
        Vector<ISynset> out = new Vector<ISynset>();
        PointerTargetTree hypernyms = null;
        PointerUtils pu = PointerUtils.getInstance();
        try {
            hypernyms = pu.getHypernymTree(tmp, depth);
            for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
//                itr.next();
                if (itr.hasNext()) {
                    for (Iterator itr1 = ((PointerTargetNodeList) itr.next()).iterator(); itr1.hasNext();) {
                        Synset t = ((PointerTargetNode) itr1.next()).getSynset();
                        if (!isEqual(tmp, t)) {

                            WordNetSynset wns = new WordNetSynset(t);
                            ISynset is = (ISynset) wns;
                            out.add(is);
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        out.remove(tmp);
        return out;
    }

    public Vector<ISynset> getChildren() {
        Vector<ISynset> out = new Vector<ISynset>();
        PointerTargetTree hypernyms = null;
        PointerUtils pu = PointerUtils.getInstance();
        try {
            hypernyms = pu.getHyponymTree(tmp, 1);
            for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
//                itr.next();
                if (itr.hasNext()) {
                    for (Iterator itr1 = ((PointerTargetNodeList) itr.next()).iterator(); itr1.hasNext();) {
                        Synset t = ((PointerTargetNode) itr1.next()).getSynset();
                        if (!isEqual(tmp, t)) {

                            WordNetSynset wns = new WordNetSynset(t);
                            ISynset is = (ISynset) wns;
                            out.add(is);
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        out.remove(tmp);
        return out;
    }

    public Vector<ISynset> getChildren(int depth) {
        Vector<ISynset> out = new Vector<ISynset>();
        PointerTargetTree hypernyms = null;
        PointerUtils pu = PointerUtils.getInstance();
        try {
            hypernyms = pu.getHyponymTree(tmp, depth);
            for (Iterator itr = hypernyms.toList().iterator(); itr.hasNext();) {
//                itr.next();
                if (itr.hasNext()) {
                    for (Iterator itr1 = ((PointerTargetNodeList) itr.next()).iterator(); itr1.hasNext();) {
                        Synset t = ((PointerTargetNode) itr1.next()).getSynset();
                        if (!isEqual(tmp, t)) {
                            WordNetSynset wns = new WordNetSynset(t);
                            ISynset is = (ISynset) wns;
                            out.add(is);
                        }
                    }
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
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

    public boolean isNull() {
        return (tmp == null);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WordNetSynset)) return false;

        final WordNetSynset wordNetSynset = (WordNetSynset) o;

        if (tmp != null ? !tmp.equals(wordNetSynset.tmp) : wordNetSynset.tmp != null) return false;

        return true;
    }

    public int hashCode() {
        return (tmp != null ? tmp.hashCode() : 0);
    }
}
