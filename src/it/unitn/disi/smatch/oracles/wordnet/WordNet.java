package it.unitn.disi.smatch.oracles.wordnet;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISynset;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.log4j.Logger;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Implements a Linguistic Oracle using WordNet.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class WordNet implements ILinguisticOracle {

    private static final Logger log = Logger.getLogger(WordNet.class);

    private static Dictionary wordNetDictionary;

    public WordNet() {
        wordNetDictionary = MatchManager.getWordNetDictionary();
    }

    public Vector<String> getSenses(String label) {
        IndexWordSet lemmas = null;
        //TODO remove when Thing is a top
        try {
            lemmas = wordNetDictionary.lookupAllIndexWords(label);
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            //throw new SMatchException(errMessage, e);
        }
        if (null != lemmas && 0 < lemmas.size()) {
            Vector<String> tmpSense = new Vector<String>();
            IndexWord lemma;
            Synset synset;
            String synsetId;
            try {
                //Looping on all words in indexWordSet
                for (int i = 0; i < lemmas.getIndexWordArray().length; i++) {
                    lemma = lemmas.getIndexWordArray()[i];
                    for (int j = 0; j < lemma.getSenseCount(); j++) {
                        synset = lemma.getSenses()[j];
                        //Forming the sense string
                        //TODO: cut as experimental heuristic
                        //if ((synset.getPOS().getKey().equals("n"))||(synset.getPOS().getKey().equals("a"))){
                        synsetId = synset.getPOS().getKey() + "#" + synset.getOffset();
                        tmpSense.add(synsetId);
                        //}
                    }
                }
            } catch (Exception e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                //throw new SMatchException(errMessage, e);
            }
            return tmpSense;
        } else {
            return null;
        }
    }

    public String getBaseForm(String deriviation) {
        try {
            IndexWordSet tmp = Dictionary.getInstance().lookupAllIndexWords(deriviation);
            IndexWord[] tmpar = tmp.getIndexWordArray();
            for (IndexWord indexWord : tmpar) {
                String word = indexWord.getLemma();
                if (word != null)
                    return word;
            }
        } catch (JWNLException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            //throw new SMatchException(errMessage, e);
        }
        return null;
    }

    private Synset getSynset(String source) {
        StringTokenizer stSource = new StringTokenizer(source, "#");
        try {
            POS POSSource = POS.getPOSForKey(stSource.nextToken());
            if (!stSource.hasMoreTokens()) {
                System.err.println(source);
                return null;
            }
            String sourseID = stSource.nextToken();
            if (!sourseID.startsWith("000000")) {
                long lSourseID = Long.parseLong(sourseID);
                return wordNetDictionary.getSynsetAt(POSSource, lSourseID);
            }
        } catch (Exception ex) {
            System.err.println(source);
            ex.printStackTrace();
        }
        return null;
    }

    public boolean isEqual(String str1, String str2) {
        try {
            IndexWordSet lemmas1 = wordNetDictionary.lookupAllIndexWords(str1);
            IndexWordSet lemmas2 = wordNetDictionary.lookupAllIndexWords(str2);
            if ((lemmas1 == null) || (lemmas2 == null) || (lemmas1.size() < 1) || (lemmas2.size() < 1))
                return false;
            else {
                IndexWord[] v1 = lemmas1.getIndexWordArray();
                IndexWord[] v2 = lemmas2.getIndexWordArray();
                for (IndexWord aV1 : v1) {
                    for (IndexWord aV2 : v2) {
                        if (aV1.equals(aV2))
                            return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ISynset getISynset(String source) {
        return new WordNetSynset(getSynset(source));
    }
}
