package it.unitn.disi.smatch.preprocessors;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.*;
import it.unitn.disi.smatch.oracles.ILinguisticOracle;
import it.unitn.disi.smatch.oracles.ISenseMatcher;
import it.unitn.disi.smatch.utils.SMatchUtils;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * This class performs all the operations related to linguistic preprocessing.
 * It also contains some heuristics to perform sense disambiguation.
 * Corresponds to Step 1 and 2 in the semantic matching algorithm
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DefaultContextPreprocessor extends Configurable implements IContextPreprocessor {

    private static final Logger log = Logger.getLogger(DefaultContextPreprocessor.class);

    // controls loading of arrays, used to skip loading before conversion
    private static final String LOAD_ARRAYS_KEY = "loadArrays";

    private static final String JWNL_PROPERTIES_PATH_KEY = "JWNLPropertiesPath";

    // contains all the multiwords in WordNet
    private static final String MULTIWORDS_FILE_KEY = "multiwordsFileName";
    private HashMap<String, ArrayList<ArrayList<String>>> multiwords = null;

    // sense matcher
    private static final String SENSE_MATCHER_KEY = "senseMatcher";
    private ISenseMatcher senseMatcher = null;

    // linguistic oracle
    private static final String LINGUISTIC_ORACLE_KEY = "linguisticOracle";
    private ILinguisticOracle linguisticOracle = null;

    private HashSet<String> unrecognizedWords = new HashSet<String>();

    // flag to output the label being translated in logs
    private final static String DEBUG_LABELS_KEY = "debugLabels";
    private boolean debugLabels = false;

    // the words which are cut off from the area of discourse
    public static String MEANINGLESS_WORDS_KEY = "meaninglessWords";
    private String meaninglessWords = "of on to their than from for by in at is are have has the a as with your etc our into its his her which him among those against ";

    // the words which are treated as logical and (&)
    public static String AND_WORDS_KEY = "andWords";
    private String andWords = " + & ^ ";

    // the words which are treated as logical or (|)
    public static String OR_WORDS_KEY = "orWords";
    private String orWords = " and or | , ";

    // the words which are treated as logical not (~)
    public static String NOT_WORDS_KEY = "notWords";
    private String notWords = " except non without ";

    // Number characters for linguistic preprocessing.
    public static String NUMBER_CHARACTERS_KEY = "numberCharacters";
    private String numberCharacters = "1234567890";


    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        if (!newProperties.equals(properties)) {
            boolean loadArrays = true;
            if (newProperties.containsKey(LOAD_ARRAYS_KEY)) {
                loadArrays = Boolean.parseBoolean(newProperties.getProperty(LOAD_ARRAYS_KEY));
            }

            if (newProperties.containsKey(MULTIWORDS_FILE_KEY)) {
                if (loadArrays) {
                    String multiwordFileName = newProperties.getProperty(MULTIWORDS_FILE_KEY);
                    log.info("Loading multiwords: " + multiwordFileName);
                    multiwords = readHash(multiwordFileName);
                    log.info("loaded multiwords: " + multiwords.size());
                }
            } else {
                final String errMessage = "Cannot find configuration key " + MULTIWORDS_FILE_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            if (newProperties.containsKey(SENSE_MATCHER_KEY)) {
                senseMatcher = (ISenseMatcher) configureComponent(senseMatcher, properties, newProperties, "sense matcher", SENSE_MATCHER_KEY, ISenseMatcher.class);
            } else {
                final String errMessage = "Cannot find configuration key " + SENSE_MATCHER_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            if (newProperties.containsKey(LINGUISTIC_ORACLE_KEY)) {
                linguisticOracle = (ILinguisticOracle) configureComponent(linguisticOracle, properties, newProperties, "linguistic oracle", LINGUISTIC_ORACLE_KEY, ILinguisticOracle.class);
            } else {
                final String errMessage = "Cannot find configuration key " + LINGUISTIC_ORACLE_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            if (newProperties.containsKey(DEBUG_LABELS_KEY)) {
                debugLabels = Boolean.parseBoolean(newProperties.getProperty(DEBUG_LABELS_KEY));
            }

            if (newProperties.containsKey(MEANINGLESS_WORDS_KEY)) {
                meaninglessWords = newProperties.getProperty(MEANINGLESS_WORDS_KEY) + " ";
            }

            if (newProperties.containsKey(AND_WORDS_KEY)) {
                andWords = newProperties.getProperty(AND_WORDS_KEY) + " ";
            }

            if (newProperties.containsKey(OR_WORDS_KEY)) {
                orWords = newProperties.getProperty(OR_WORDS_KEY) + " ";
            }

            if (newProperties.containsKey(NOT_WORDS_KEY)) {
                notWords = newProperties.getProperty(NOT_WORDS_KEY) + " ";
            }

            if (newProperties.containsKey(NUMBER_CHARACTERS_KEY)) {
                numberCharacters = newProperties.getProperty(NUMBER_CHARACTERS_KEY);
            }

            properties.clear();
            properties.putAll(newProperties);
        }
    }

    /**
     * This method perfoms all preprocessing procedures as follows:
     * - linguistic analysis (each lemma is associated with
     * the set of senses taken from the oracle).
     * - sense filtering (elimination of irrelevant to context structure senses)
     *
     * @param context context to be prepocessed
     */
    public void preprocess(IContext context) throws ContextPreprocessorException {
        context.getMatchingContext().resetOldPreprocessing();
        unrecognizedWords.clear();
        //construct cLabs
        context = buildCLabs(context);
        //sense filtering
        context = findMultiwordsInContextStructure(context);
        senseFiltering(context);

        //unrecognized words
        log.info("Unrecognized words: " + unrecognizedWords.size());
        unrecognizedWords.clear();
    }

    /**
     * Constructs cLabs for all nodes of the context.
     *
     * @param context context of node which cLab to be build
     * @return context with cLabs
     */
    private IContext buildCLabs(IContext context) {
        Vector<INode> allNodes = context.getAllNodes();
        try {
            int counter = 0;
            int total = allNodes.size();
            int reportInt = (total / 20) + 1;//i.e. report every 5%
            while (0 < allNodes.size()) {
                counter++;
                if ((SMatchConstants.LARGE_TREE < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
                INode node = allNodes.remove(0);
                processNode(node);
            }
        } catch (Exception e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                //throw new SMatchException(errMessage, e);
            }
        }

        return context;
    }

    /**
     * process node to construct cLabs of context.
     *
     * @param node interface of node which will be processed
     */
    private void processNode(INode node) {
        int id_tok = 0;
        boolean isEmpty = true;
        String labelOfNode = node.getNodeName().trim();

        if (debugLabels) {
            log.debug("preprocessing: " + labelOfNode);
        }

        labelOfNode = replacePunctuation(labelOfNode);
        labelOfNode = labelOfNode.toLowerCase();
        Vector<String> wnSense;
        if (labelOfNode.equals("top")) {
            wnSense = null;
        } else {
            if ((meaninglessWords.indexOf(labelOfNode + " ") == -1) && (isTokenMeaningful(labelOfNode)))
                wnSense = linguisticOracle.getSenses(labelOfNode);
            else
                wnSense = null;
        }

        //Identifiers of meaningful tokens in
        String meaningfulTokens = " ";
        //tokens of the label of node
        Vector<String> tokensOfNodeLabel = new Vector<String>();

        //is the label a Wordnet node?
        if (wnSense != null) {
            id_tok++;

//                    if (labelOfNode.indexOf(' ') > -1) {
//                        for (StringTokenizer stringTokenizer = new StringTokenizer(labelOfNode); stringTokenizer.hasMoreTokens();) {
//                            String subToken = stringTokenizer.nextToken();
//                            Vector<String> subTokenSenses = linguisticOracle.getSenses(subToken);
//                            if ((subTokenSenses != null) && (subTokenSenses.size() > 0)) {
//                                wnSense.addAll(subTokenSenses);
//                            }
//                        }
//                    }

            //add to list of processed labels
            tokensOfNodeLabel.add(labelOfNode);
            String lemma = linguisticOracle.getBaseForm(labelOfNode);
            if (null == lemma) {
                lemma = labelOfNode;
            }

            //create atomic node of label
            IAtomicConceptOfLabel ACoL = AtomicConceptOfLabel.getInstance(id_tok, labelOfNode, lemma, "all");
            //Attach senses obtained from the oracle to the node
            node.getNodeData().addAtomicConceptOfLabel(ACoL);
            //to to token ids
            meaningfulTokens = meaningfulTokens + id_tok + " ";
            //if there are no senses in WN mark as a special node with unknown meaning
            if (wnSense.size() == 0) {
                wnSense.add(ILinguisticOracle.UNKNOWN_MEANING + labelOfNode);
            }
            //add senses to ACoL
            ACoL.addSenses(wnSense);
            isEmpty = false;
        } else {
            //The label of node is not in WN
            //Split the label by words
            StringTokenizer lemmaTokenizer = new StringTokenizer(labelOfNode, " _()[]/'\\#1234567890");
            ArrayList<String> tokens = new ArrayList<String>();
            ArrayList<String> tv = new ArrayList<String>();
            while (lemmaTokenizer.hasMoreElements()) {
                String tmp = (String) lemmaTokenizer.nextElement();
                //TODO add abbr support
                if (tv.size() > 0) {
                    tokens.addAll(tv);
                    tv.clear();
                } else {
                    tokens.add(tmp);
                }
            }

            //perform multiword recognition
            tokens = multiwordRecognition(tokens);
            //for all tokens in label
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i).trim();
                //if the token is not meaningless
                if ((meaninglessWords.indexOf(token + " ") == -1) && (isTokenMeaningful(token))) {
                    //add to list of processed tokens
                    tokensOfNodeLabel.add(token);
                    id_tok++;
                    //if not logical connective
                    if ((andWords.indexOf(token) == -1) && ((orWords.indexOf(token)) == -1)
                            && ((notWords.indexOf(token)) == -1) && (!isNumber(token))) {
                        //get WN senses for token
                        if (token.trim().equals("top")) {
                            wnSense = null;
                        } else {
                            wnSense = linguisticOracle.getSenses(token);
                        }
//multiword sense enrichment
//                                if (token.indexOf(' ') > -1) {
//                                    for (StringTokenizer stringTokenizer = new StringTokenizer(token); stringTokenizer.hasMoreTokens();) {
//                                        String subToken = stringTokenizer.nextToken();
//                                        Vector<String> subTokenSenses = linguisticOracle.getSenses(subToken);
//                                        if ((subTokenSenses != null) && (subTokenSenses.size() > 0)) {
//                                            wnSense.addAll(subTokenSenses);
//                                        }
//                                    }
//
//                                }
                        if ((wnSense == null) || (wnSense.size() == 0)) {
                            Vector<String> newTokens = complexWordsRecognition(token);
                            if (newTokens != null) {
                                tokensOfNodeLabel.removeElementAt(tokensOfNodeLabel.size() - 1);
                                tokensOfNodeLabel.add(newTokens.get(0));
                                wnSense = linguisticOracle.getSenses(newTokens.get(0));
                                tokens.remove(i);
                                tokens.add(i, newTokens.get(0));
                                for (int j = 1; j < newTokens.size(); j++) {
                                    String s = newTokens.elementAt(j);
                                    tokens.add(i + j, s);
                                }
                            }
                        }
                        String lemma = linguisticOracle.getBaseForm(token);
                        if (lemma == null)
                            lemma = token;

                        //create atomic node of label
                        IAtomicConceptOfLabel ACoL = AtomicConceptOfLabel.getInstance(id_tok, token, lemma, "all");
                        //add it to node
                        node.getNodeData().addAtomicConceptOfLabel(ACoL);
                        //mark id  as meaningful
                        meaningfulTokens = meaningfulTokens + id_tok + " ";
                        //if there no WN senses
                        if ((wnSense == null) || (wnSense.size() == 0)) {
                            if (wnSense == null)
                                wnSense = new Vector<String>();
                            //mark as a node with unrecognized meaning
                            wnSense.add(ILinguisticOracle.UNKNOWN_MEANING + token);

                            //log.debug("Unrecognized word " + token);
                            unrecognizedWords.add(token);
                        }
                        //add senses to ACoL
                        ACoL.addSenses(wnSense);
                        isEmpty = false;
                    }
                }
            }
        }

        if (isEmpty) {
            String token = "top";
            id_tok++;
            //add to list of processed labels
            tokensOfNodeLabel.add(token);
            wnSense = null;//linguisticOracle.getSenses(token);
            //create atomic node of label
            IAtomicConceptOfLabel ACoL = AtomicConceptOfLabel.getInstance(id_tok, token, token, "all");
            //Attach senses obtained from the oracle to the node
            node.getNodeData().addAtomicConceptOfLabel(ACoL);
            //to to token ids
            meaningfulTokens = meaningfulTokens + id_tok + " ";
            //if there are no senses in WN mark as a special node with unknown meaning
            if ((wnSense == null) || (wnSense.size() == 0)) {
                wnSense = new Vector<String>();
                wnSense.add(ILinguisticOracle.UNKNOWN_MEANING + token);
            }
            //add senses to ACoL
            ACoL.addSenses(wnSense);
        }
        //  Building formula of complex node
        buildComplexConcept(node, tokensOfNodeLabel, meaningfulTokens);
    }

    /**
     * Checks the token is meaningful or not for processing the node.
     *
     * @param token the lemma of input string
     * @return true if it is meaningful
     */
    private boolean isTokenMeaningful(String token) {
        token = token.trim();
        if ((andWords.indexOf(token) > -1) || ((orWords.indexOf(token)) > -1))
            return true;
        if (token.length() < 3) {
//			Vector<String> wnSense = linguisticOracle.getSenses(token);
//			if ((wnSense == null) || (wnSense.size() == 0))
            return false;
//			else
//				return true;
        }
        return true;
    }

    /**
     * Finds out the input token is complex word or not using WordNet senses.
     *
     * @param token lemma of input string
     * @return a vector which contains parts of the complex word.
     */
    private Vector<String> complexWordsRecognition(String token) {
        Vector<String> senses = null;
        int i = 0;
        String start = null;
        String end = null;
        String toCheck = null;
        boolean flag = false;
        boolean multiword = false;
        while ((i < token.length() - 1) && (senses == null)) {
            i++;
            start = token.substring(0, i);
            end = token.substring(i, token.length());
            toCheck = start + ' ' + end;
            senses = linguisticOracle.getSenses(toCheck);
            if ((senses == null) || (senses.size() == 0)) {
                toCheck = start + '-' + end;
                senses = linguisticOracle.getSenses(toCheck);
            }

            if ((senses != null) && (senses.size() > 0)) {
                multiword = true;
                break;
            } else {
                if ((start.length() > 3) && (end.length() > 3)) {
                    senses = linguisticOracle.getSenses(start);
                    if ((senses != null) && (senses.size() > 0)) {
                        senses = linguisticOracle.getSenses(end);
                        if ((senses != null) && (senses.size() > 0)) {
                            flag = true;
                            break;
                        }
                    }
                }
            }
        }
        if (multiword) {
            Vector<String> out = new Vector<String>();
            out.add(toCheck);
            return out;
        }
        if (flag) {
            Vector<String> out = new Vector<String>();
            out.add(start);
            out.add(end);
            return out;
        }
        return null;
    }

    /**
     * The method constructs the logical formula for the complex concept of label.
     *
     * @param node              node to build complex concept
     * @param tokensOfNodeLabel Vector of tokens in the node label
     * @param meaningfulTokens  identifiers of the meaningful tokens
     */
    private void buildComplexConcept(INode node, Vector<String> tokensOfNodeLabel, String meaningfulTokens) {
        //label of node
        String token;
        //Vector of ACoLs identifiers
        Vector<String> vec = new Vector<String>();
        //formula for the complex concept
        String formulaOfConcept = "";
        //logical connective
        String connective = " ";
        //bracets to add
        String bracket = "";
        //whether to insert brackets
        boolean insert;
        //how many left brackets do not have corresponding right ones
        int bracketsBalance = 0;
        //number of left brackets
        int leftBrackets = 0;
        //for each token of node label
        for (int i = 0; i < tokensOfNodeLabel.size(); i++) {
            token = (tokensOfNodeLabel.elementAt(i));
            //If logical and or or
            if (andWords.indexOf(" " + token + " ") != -1 || orWords.indexOf(" " + token + " ") != -1) {
                insert = false;
                //If non first token
                if (vec != null && vec.size() > 0) {
                    //construct formula
                    if (connective.equals("")) {
                        formulaOfConcept = formulaOfConcept + " | " + bracket + vec.toString();
                    } else {
                        formulaOfConcept = formulaOfConcept + connective + bracket + vec.toString();
                    }
                    insert = true;
                    connective = "";
                    bracket = "";
                    vec = new Vector<String>();
                    leftBrackets = 0;
                }
                //If bracket
                if (token.equals("(") && bracketsBalance >= 0) {
                    connective = " & ";
                    bracket = "(";
                    bracketsBalance = bracketsBalance + 1;
                    leftBrackets = leftBrackets + 1;
                } else if (token.equals(")") && bracketsBalance > 0) {
                    if (insert) {
                        formulaOfConcept = formulaOfConcept + ")";
                    }
                    bracketsBalance = bracketsBalance - 1;
                } else {
                    connective = " | ";
                }
                //If logical not
            } else if (notWords.indexOf(" " + token + " ") != -1) {
                if (vec != null && vec.size() > 0) {
                    formulaOfConcept = formulaOfConcept + connective + vec.toString();
                    vec = new Vector<String>();
                    connective = "";
                }
                //What to add
                if (connective.indexOf("&") != -1 || connective.indexOf("|") != -1) {
                    connective = connective + " ~ ";
                } else {
                    connective = " & ~ ";
                }
            } else {
                if (meaningfulTokens.indexOf(" " + (i + 1) + " ") != -1) {
                    //fill Vector with ACoL ids
                    vec.add((node.getNodeId() + "." + (i + 1)));
                }
            }
        }
        //Dealing with first token of the node
        if (vec != null && vec.size() > 0) {
            //construct formula
            if (connective.indexOf("&") != -1 || connective.indexOf("|") != -1 || connective.equals(" ")) {
                formulaOfConcept = formulaOfConcept + connective + bracket + vec.toString();
            } else {
                formulaOfConcept = formulaOfConcept + " & " + vec.toString();
            }
            connective = "";
        } else {
            if (leftBrackets > 0) {
                bracketsBalance = bracketsBalance - leftBrackets;
            }
        }
        if (bracketsBalance > 0) {
            for (int i = 0; i < bracketsBalance; i++) {
                formulaOfConcept = formulaOfConcept + ")";
            }
        }
        //dealing with brackets
        formulaOfConcept = formulaOfConcept.replace('[', '(');
        formulaOfConcept = formulaOfConcept.replace(']', ')');
        formulaOfConcept = formulaOfConcept.replaceAll(", ", " & ");
        formulaOfConcept = formulaOfConcept.trim();
        if (formulaOfConcept.startsWith("&")) {
            StringTokenizer atoms = new StringTokenizer(formulaOfConcept, "&");
            formulaOfConcept = atoms.nextToken();
        }
        formulaOfConcept = formulaOfConcept.trim();
        if (formulaOfConcept.startsWith("|")) {
            StringTokenizer atoms = new StringTokenizer(formulaOfConcept, "|");
            formulaOfConcept = atoms.nextToken();
        }
        //brackets counters
        StringTokenizer open = new StringTokenizer(formulaOfConcept, "(", true);
        int openCount = 0;
        while (open.hasMoreTokens()) {
            String tmp = open.nextToken();
            tmp.trim();
            if (tmp.equals("("))
                openCount++;
        }
        StringTokenizer closed = new StringTokenizer(formulaOfConcept, ")", true);
        while (closed.hasMoreTokens()) {
            String tmp = closed.nextToken();
            tmp.trim();
            if (tmp.equals(")"))
                openCount--;
        }
        if (openCount > 0) {
            for (int par = 0; par < openCount; par++)
                formulaOfConcept += ")";
        }
        if (openCount < 0) {
            for (int par = 0; par < openCount; par++)
                formulaOfConcept = "(" + formulaOfConcept;
        }
        //Assign formula to the node
        //node.getNodeData().setcLabFormulaToConjunciveForm(formulaOfConcept);
        node.getNodeData().setcLabFormula(formulaOfConcept);
    }

    /**
     * The method replaces punctuation signs by spaces.
     *
     * @param lemma lemma of the input string
     * @return processed lemma with spaces in place of punctuation
     */
    private static String replacePunctuation(String lemma) {
        lemma = lemma.replace(",", " , ");
        lemma = lemma.replace('.', ' ');
//        lemma = lemma.replace('-', ' ');
        lemma = lemma.replace('\'', ' ');
        lemma = lemma.replace('(', ' ');
        lemma = lemma.replace(')', ' ');
        lemma = lemma.replace(':', ' ');
        lemma = lemma.replace(";", " ; ");
        return lemma;
    }

    private Vector<String> checkMW(String source, String target) {
        ArrayList<ArrayList<String>> mwEnds = multiwords.get(source);
        if (mwEnds != null)
            for (int j = 0; j < mwEnds.size(); j++) {
                ArrayList<String> strings = mwEnds.get(j);
                if (extendedIndexOf(strings, target, 0) > 0) {
                    return linguisticOracle.getSenses(source + " " + target);

                }
            }
        return null;
    }

    private void enrichSensesSets(ISensesSet sensesSet, Vector<String> wnSenses) {
        sensesSet.addNewSenses(wnSenses);
    }

    /**
     * Computes all multiwords in input data structure.
     *
     * @param context data structure of input label
     * @return context with multiwords
     */
    private IContext findMultiwordsInContextStructure(IContext context) {
        //all context nodes
        Vector<INode> allNode = context.getAllNodes();
        for (int i = 0; i < allNode.size(); i++) {
            //get Node
            INode sourceNode = allNode.elementAt(i);
            //get node ACoLs
            Vector<IAtomicConceptOfLabel> sourceNodeACoLs = sourceNode.getNodeData().getACoLs();
            //sense disambiguation within the context structure
            //get all the source node descendants
            Vector<INode> allConcept = sourceNode.getDescendants();
            //and ancestors
            allConcept.addAll(sourceNode.getAncestors());
            //for all ACoLs in the source node
            for (int s = 0; s < sourceNodeACoLs.size(); s++) {
                IAtomicConceptOfLabel synSource = sourceNodeACoLs.elementAt(s);
                String sourceLemma = synSource.getLemma();
                for (int id_node_target = 0; id_node_target < allConcept.size(); id_node_target++) {
                    INodeData targetNode = (INodeData) allConcept.elementAt(id_node_target);
                    //get ACoLs
                    Vector<IAtomicConceptOfLabel> targetNodeACoLs = targetNode.getACoLs();
                    for (int senseTarget = 0; senseTarget < targetNodeACoLs.size(); senseTarget++) {
                        IAtomicConceptOfLabel synTarget = targetNodeACoLs.elementAt(senseTarget);
                        String targetLemma = synTarget.getLemma();
                        //TODO add Vectors and enrich with senses
                        Vector<String> wnSenses = checkMW(sourceLemma, targetLemma);
                        if ((wnSenses != null) && (wnSenses.size() > 0)) {
                            enrichSensesSets(synSource.getSenses(), wnSenses);
                            enrichSensesSets(synTarget.getSenses(), wnSenses);


                        }
                    }
                }
            }
        }
        return context;
    }

    /**
     * This method performs elimination the senses which do not suit to overall context meaning.
     * Performs sense filtering in two steps
     * -filtering within complex node label
     * -filtering within context structure
     *
     * @param context context to perform sense filtering
     */
    private void senseFiltering(IContext context) {
        IContextData icd = context.getContextData();
        //all context nodes
        Vector<INode> allNode = context.getAllNodes();
        //Senses Sets for particular ACoL
        Vector<ISensesSet> sourceSensesSets;
        Vector<ISensesSet> targetSensesSets;
        //for all nodes in context
        for (int i = 0; i < allNode.size(); i++) {
            //get Node
            INode sourceNode = allNode.elementAt(i);
            //get node ACoLs
            Vector<IAtomicConceptOfLabel> sourceNodeACoLs = sourceNode.getNodeData().getACoLs();
            //if node is complex
            if (sourceNodeACoLs.size() > 1) {
                //for each ACoL in the node
                for (int j = 0; j < sourceNodeACoLs.size(); j++) {
                    IAtomicConceptOfLabel sourceACoL = sourceNodeACoLs.elementAt(j);
                    ISensesSet sourceSenseSet = sourceACoL.getSenses();
                    //get WN senses
                    Vector<String> sourceSenses = sourceSenseSet.getSenseList();
                    //compare with all the other ACoLs in the node
                    for (int k = 1; k < sourceNodeACoLs.size(); k++) {
                        IAtomicConceptOfLabel targetACoL = sourceNodeACoLs.elementAt(k);
                        if (!targetACoL.equals(sourceACoL)) {
                            ISensesSet targetSenseSet = targetACoL.getSenses();
                            //get WN senses
                            Vector<String> targetSenses = targetSenseSet.getSenseList();
                            //for each sense in source ACoL
                            for (int l = 0; l < sourceSenses.size(); l++) {
                                String sourceSenseID = sourceSenses.elementAt(l);
                                //for each sense in target ACoL
                                for (int m = 0; m < targetSenses.size(); m++) {
                                    String targetSenseID = targetSenses.elementAt(m);
                                    try {
                                        boolean isRelationPresent = false;
                                        if ((senseMatcher.isSourceSynonymTarget(sourceSenseID, targetSenseID))) {
                                            isRelationPresent = true;
                                            fillIntraAxiomsHash(icd, sourceACoL, targetACoL);
                                        }
                                        if (senseMatcher.isSourceLessGeneralThanTarget(sourceSenseID, targetSenseID)) {
                                            isRelationPresent = true;
                                            fillIntraAxiomsHashMG(icd, targetACoL, sourceACoL);
                                        }
                                        if (senseMatcher.isSourceMoreGeneralThanTarget(sourceSenseID, targetSenseID)) {
                                            isRelationPresent = true;
                                            fillIntraAxiomsHashMG(icd, sourceACoL, targetACoL);
                                        }
                                        if (isRelationPresent) {
                                            sourceSenseSet.addToRefinedSenses(sourceSenseID);
                                            targetSenseSet.addToRefinedSenses(targetSenseID);
                                        }

                                        //if senses are synonyms or less (more) general in WN hierarchy
//                                    if ((senseMatcher.isSourceSynonymTarget(sourceSenseID, targetSenseID)) ||
//                                            (senseMatcher.isSourceLessGeneralThanTarget(sourceSenseID, targetSenseID)) ||
//                                            (senseMatcher.isSourceMoreGeneralThanTarget(sourceSenseID, targetSenseID))) {
//                                        //add to refined senses in the sense list
//                                        sourceSenseSet.addToRefinedSenses(sourceSenseID);
//                                        targetSenseSet.addToRefinedSenses(targetSenseID);
//                                    }
//                                    if (senseMatcher.isSourceOppositeToTarget(sourceSenseID, targetSenseID)) {
//                                        log.debug("Inside opposite lemmas " + sourceACoL.getLemma() + " " + targetACoL.getLemma());
//                                    }

                                    } catch (Exception e) {
                                        if (log.isEnabledFor(Level.ERROR)) {
                                            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                                            log.error(errMessage, e);
                                            //throw new SMatchException(errMessage, e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //sense disambiguation within the context structure
            //get all the source node descendants
            Vector<INode> allConcept = sourceNode.getDescendants();
            //and ancestors
            allConcept.addAll(sourceNode.getAncestors());
            //add siblings
//            if (!sourceNode.isRoot()) {
//                Vector<INode> siblings = sourceNode.getParent().getChildren();
//                //siblings.remove(sourceNode);
//                allConcept.addAll(siblings);
//            }
            //for all ACoLs in the source node
            for (int s = 0; s < sourceNodeACoLs.size(); s++) {
                IAtomicConceptOfLabel sourceACoL = sourceNodeACoLs.elementAt(s);
                ISensesSet sourceSenseSet = sourceACoL.getSenses();
                //get WN senses
                if (sourceSenseSet.isRefinedSensesEmpty()) {
                    Vector<String> sourceSenses = sourceSenseSet.getSenseList();
                    for (Iterator<String> itSenseSource = sourceSenses.iterator(); itSenseSource.hasNext();) {
                        String sourceSenseID = itSenseSource.next();
                        //for all target nodes (ancestors and descendants)
                        for (int id_node_target = 0; id_node_target < allConcept.size(); id_node_target++) {
                            INodeData targetNode = (INodeData) allConcept.elementAt(id_node_target);
                            //get ACoLs
                            Vector<IAtomicConceptOfLabel> targetNodeACoLs = targetNode.getACoLs();
                            for (int senseTarget = 0; senseTarget < targetNodeACoLs.size(); senseTarget++) {
                                IAtomicConceptOfLabel targetACoL = targetNodeACoLs.elementAt(senseTarget);
                                ISensesSet targetSenseSet = targetACoL.getSenses();
                                //get WN senses
                                if (targetSenseSet.isRefinedSensesEmpty()) {
                                    Vector<String> targetSenses = targetSenseSet.getSenseList();
                                    for (Iterator<String> itSenseTarget = targetSenses.iterator(); itSenseTarget.hasNext();) {
                                        String targetSenseID = itSenseTarget.next();
                                        // Check whether each sense not synonym or
                                        //more general, less general then the senses of
                                        //the ancestors and descendants of the node in
                                        //context hierarchy
                                        try {
                                            boolean isRelationPresent = false;
                                            if ((senseMatcher.isSourceSynonymTarget(sourceSenseID, targetSenseID))) {
                                                isRelationPresent = true;
                                                fillIntraAxiomsHash(icd, sourceACoL, targetACoL);
                                            }
                                            if (senseMatcher.isSourceLessGeneralThanTarget(sourceSenseID, targetSenseID)) {
                                                isRelationPresent = true;
                                                fillIntraAxiomsHashMG(icd, targetACoL, sourceACoL);
                                            }
                                            if (senseMatcher.isSourceMoreGeneralThanTarget(sourceSenseID, targetSenseID)) {
                                                isRelationPresent = true;
                                                fillIntraAxiomsHashMG(icd, sourceACoL, targetACoL);
                                            }
                                            if (isRelationPresent) {
                                                sourceSenseSet.addToRefinedSenses(sourceSenseID);
                                                targetSenseSet.addToRefinedSenses(targetSenseID);
                                            }

//                                            if ((senseMatcher.isSourceSynonymTarget(sourceSenseID, targetSenseID)) ||
//                                                    (senseMatcher.isSourceLessGeneralThanTarget(sourceSenseID, targetSenseID)) ||
//                                                    (senseMatcher.isSourceMoreGeneralThanTarget(sourceSenseID, targetSenseID))) {
//                                                //add to refined senses in the sense list
//                                                sourceSenseSet.addToRefinedSenses(sourceSenseID);
//                                                targetSenseSet.addToRefinedSenses(targetSenseID);
//                                            }
//                                            if (senseMatcher.isSourceOppositeToTarget(sourceSenseID, targetSenseID)) {
//                                                log.debug("Opposite lemmas " + sourceACoL.getLemma() + " " + targetACoL.getLemma());
//                                            }
                                        } catch (Exception e) {
                                            if (log.isEnabledFor(Level.ERROR)) {
                                                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                                                log.error(errMessage, e);
                                                //throw new SMatchException(errMessage, e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //Loop on SensesSets of the all concepts and assign to them
        //senses mark as refined on the previous step
        //If there are no refined senses save the original ones
        for (int i = 0; i < allNode.size(); i++) {
            INodeData cSource = allNode.elementAt(i).getNodeData();
            Vector<IAtomicConceptOfLabel> sourceSetOfSenses = cSource.getACoLs();
            for (int s = 0; s < sourceSetOfSenses.size(); s++) {
                IAtomicConceptOfLabel synSource = sourceSetOfSenses.elementAt(s);
                ISensesSet SenseSource = synSource.getSenses();
                if (!SenseSource.isRefinedSensesEmpty()) {
                    SenseSource.updateSenseList();
                }
            }
        }
    }

    private void fillIntraAxiomsHash(IContextData context, IAtomicConceptOfLabel sourceACoL, IAtomicConceptOfLabel targetACoL) {
        String source = sourceACoL.getTokenUID();
        String target = targetACoL.getTokenUID();
        String toHash = "";
        if (target.compareTo(source) > 0)
            toHash = target + source;
        else
            toHash = source + target;
        context.getSynonyms().add(toHash);
    }

    private void fillIntraAxiomsHashMG(IContextData context, IAtomicConceptOfLabel sourceACoL, IAtomicConceptOfLabel targetACoL) {
        String source = sourceACoL.getTokenUID();
        String target = targetACoL.getTokenUID();
        String toHash = source + target;
        context.getMg().add(toHash);
    }

    /**
     * Checks whether input string contains a number or not.
     *
     * @param in1 input string
     * @return false if it contains a number
     */
    private boolean isNumber(String in1) {
        for (StringTokenizer stringTokenizer = new StringTokenizer(in1, numberCharacters); stringTokenizer.hasMoreTokens();) {
            return false;
        }
        return true;
    }

    /**
     * extension of Vector indexOf which uses approximate comparison of the words as
     * elements of the Vector
     */
    private int extendedIndexOf(ArrayList<String> vec, String str, int init_pos) {
        //for all words in the input Vector starting from init_pos
        for (int i = init_pos; i < vec.size(); i++) {
            String vel = vec.get(i);
            //try syntactic
            if (vel.equals(str))
                return i;
            else if (vel.indexOf(str) == 0)
                //and semantic comparison
                if (linguisticOracle.isEqual(vel, str)) {
                    vec.add(i, str);
                    vec.remove(i + 1);
                    return i;
                }
        }
        return -1;
    }

    /**
     * Takes as an input Vector of words and returns the Vector consisting the multiwords
     * which are in WN and can be derived from the input
     * For example having [Earth, and, Atmospheric, Sciences] as the input returns
     * [Earth Sciences, and, Atmospheric, Sciences] because Earth Sciences is a WN concept
     * and Atmospheric Sciences is not a WN concept
     *
     * @param tokens input token
     * @return a vector which contains multiwords
     */
    private ArrayList<String> multiwordRecognition(ArrayList<String> tokens) {
        String subLemma;
        HashMap<String, ArrayList<Integer>> is_token_in_multiword = new HashMap<String, ArrayList<Integer>>();
        for (int i = 0; i < tokens.size(); i++) {
            subLemma = tokens.get(i);
            if ((andWords.indexOf(subLemma) == -1) || (orWords.indexOf(subLemma) == -1)) {
                //if the first element of Vector is a key element of hash
                if (multiwords.get(subLemma) != null) {
                    ArrayList<ArrayList<String>> entries = multiwords.get(subLemma);
                    for (int j = 0; j < entries.size(); j++) {
                        ArrayList<String> mweTail = entries.get(j);
                        try {
                            boolean flag = false;
                            int co = 0;
                            //at the end co is need to move pointer for case like Clupea harengus with mw Clupea harengus harengus
                            //TODO move this to pipeline
                            while ((co < mweTail.size()) && (extendedIndexOf(tokens, mweTail.get(co), co) > i + co)) {
                                flag = true;
                                co++;
                            }
                            if ((co > mweTail.size() - 1) && (flag)) {
                                ArrayList<Integer> positions = new ArrayList<Integer>();
                                int word_pos = tokens.indexOf(subLemma);
                                if (word_pos == -1)
                                    break;
                                //TODO add better error recognition code
                                int multiword_pos = word_pos;
                                positions.add(word_pos);
                                boolean cont = true;
                                boolean connectives_prescendence = false;
                                int and_pos = -1;
                                for (int k = 0; k < mweTail.size(); k++) {
                                    String tok = mweTail.get(k);
                                    int old_pos = word_pos;
                                    try {
                                        word_pos = tokens.subList(old_pos + 1, tokens.size()).indexOf(tok) + old_pos + 1;
                                    } catch (Exception e) {
                                        if (log.isEnabledFor(Level.ERROR)) {
                                            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                                            log.error(errMessage, e);
                                            log.error(subLemma);
                                            log.error(positions);
                                            log.error(tok);
                                            log.error(tokens);
                                            log.error(old_pos);
                                            log.error(word_pos);
                                            //throw new SMatchException(errMessage, e);
                                        }
                                    }
                                    if (word_pos == -1) {
                                        word_pos = extendedIndexOf(tokens, tok, old_pos);
                                        if (word_pos == -1)
                                            break;
                                    }
                                    if (word_pos - old_pos > 1) {
                                        cont = false;
                                        for (int r = old_pos + 1; r < word_pos; r++) {
                                            if (((andWords.indexOf(tokens.get(r))) > -1) || (orWords.indexOf(tokens.get(r)) > -1)) {
                                                and_pos = r;
                                                connectives_prescendence = true;
                                            } else {
                                                //connectives_prescendence = false;
                                            }
                                        }
                                    }
                                    positions.add(word_pos);
                                }
                                int removed_tokens_index_correction = 0;
                                if (cont) {
                                    String multiword = "";
                                    for (int k = 0; k < positions.size(); k++) {
                                        Integer integer = positions.get(k);
                                        int pos = integer - removed_tokens_index_correction;
                                        multiword = multiword + tokens.get(pos) + " ";
                                        tokens.remove(pos);
                                        removed_tokens_index_correction++;
                                    }
                                    multiword = multiword.substring(0, multiword.length() - 1);
                                    tokens.add(multiword_pos, multiword);
                                } else {
                                    if (connectives_prescendence) {
                                        if (and_pos > multiword_pos) {
                                            String multiword = "";
                                            int word_distance = positions.get(positions.size() - 1) - positions.get(0);
                                            for (int k = 0; k < positions.size(); k++) {
                                                Integer integer = positions.get(k);
                                                int pos = integer - removed_tokens_index_correction;
                                                try {
                                                    if (is_token_in_multiword.get(tokens.get(pos)) == null) {
                                                        ArrayList<Integer> toAdd = new ArrayList<Integer>();
                                                        toAdd.add(1);
                                                        toAdd.add(word_distance - 1);
                                                        is_token_in_multiword.put(tokens.get(pos), toAdd);
                                                    } else {
                                                        ArrayList<Integer> toAdd = is_token_in_multiword.get(tokens.get(pos));
                                                        int tmp = toAdd.get(0) + 1;
                                                        toAdd.remove(0);
                                                        toAdd.add(0, tmp);
                                                        is_token_in_multiword.put(tokens.get(pos), toAdd);
                                                    }
                                                } catch (Exception e) {
                                                    if (log.isEnabledFor(Level.ERROR)) {
                                                        final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                                                        log.error(errMessage, e);
                                                        log.error(tokens);
                                                        log.error(pos);
                                                        log.error(positions);
                                                        //throw new SMatchException(errMessage, e);
                                                    }
                                                }
                                                multiword = multiword + tokens.get(pos) + " ";
                                            }
                                            multiword = multiword.substring(0, multiword.length() - 1);
                                            tokens.remove(multiword_pos);
                                            tokens.add(multiword_pos, multiword);
                                        }
                                    }
                                }
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            if (log.isEnabledFor(Level.ERROR)) {
                                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                                log.error(errMessage, e);
                                log.error(subLemma + " " + mweTail);
                                //throw new SMatchException(errMessage, e);
                            }
                        }
                    }
                }
            }
        }
        ArrayList<String> tmp = new ArrayList<String>();
        for (int k = 0; k < tokens.size(); k++) {
            String s = tokens.get(k);
            if (is_token_in_multiword.get(s) == null) {
                tmp.add(s);
            } else {
                ArrayList<Integer> toAdd = is_token_in_multiword.get(s);
                int dist_wo_ands_ors = toAdd.get(0);
                int multiword_participation = toAdd.get(1);
                if (dist_wo_ands_ors != multiword_participation)
                    tmp.add(s);
            }
        }
        return tmp;
    }

    /**
     * Create caches of WordNet to speed up matching.
     *
     * @param componentKey a key to the component in the configuration
     * @param properties   configuration
     * @throws it.unitn.disi.smatch.SMatchException
     *          SMatchException
     */
    public static void createWordNetCaches(String componentKey, Properties properties) throws SMatchException {
        properties = getComponentProperties(makeComponentPrefix(componentKey, DefaultContextPreprocessor.class.getSimpleName()), properties);
        if (properties.containsKey(JWNL_PROPERTIES_PATH_KEY)) {
            // initialize JWNL (this must be done before JWNL library can be used)
            try {
                final String configPath = properties.getProperty(JWNL_PROPERTIES_PATH_KEY);
                log.info("Initializing JWNL from " + configPath);
                JWNL.initialize(new FileInputStream(configPath));
            } catch (JWNLException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            } catch (FileNotFoundException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        } else {
            final String errMessage = "Cannot find configuration key " + JWNL_PROPERTIES_PATH_KEY;
            log.error(errMessage);
            throw new SMatchException(errMessage);
        }

        log.info("Creating WordNet caches...");
        writeMultiwords(properties);
        log.info("Done");
    }

    private static void writeMultiwords(Properties properties) throws SMatchException {
        log.info("Creating multiword hash...");
        HashMap<String, ArrayList<ArrayList<String>>> multiwords = new HashMap<String, ArrayList<ArrayList<String>>>();
        POS[] parts = new POS[]{POS.NOUN, POS.ADJECTIVE, POS.VERB, POS.ADVERB};
        for (POS pos : parts) {
            collectMultiwords(multiwords, pos);
        }
        log.info("Multiwords: " + multiwords.size());
        SMatchUtils.writeObject(multiwords, properties.getProperty(MULTIWORDS_FILE_KEY));
    }

    private static void collectMultiwords(HashMap<String, ArrayList<ArrayList<String>>> multiwords, POS pos) throws SMatchException {
        try {
            int count = 0;
            Iterator i = net.didion.jwnl.dictionary.Dictionary.getInstance().getIndexWordIterator(pos);
            while (i.hasNext()) {
                IndexWord iw = (IndexWord) i.next();
                String lemma = iw.getLemma();
                if (-1 < lemma.indexOf(' ')) {
                    count++;
                    if (0 == count % 10000) {
                        log.info(count);
                    }
                    String[] tokens = lemma.split(" ");
                    ArrayList<ArrayList<String>> mwEnds = multiwords.get(tokens[0]);
                    if (null == mwEnds) {
                        mwEnds = new ArrayList<ArrayList<String>>();
                    }
                    ArrayList<String> currentMWEnd = new ArrayList<String>(Arrays.asList(tokens));
                    currentMWEnd.remove(0);
                    mwEnds.add(currentMWEnd);
                    multiwords.put(tokens[0], mwEnds);
                }
            }
            log.info(pos.getKey() + " multiwords: " + count);
        } catch (JWNLException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new SMatchException(errMessage, e);
            }
        }
    }

    /**
     * Loads the hashmap with multiwords
     * the multiwords are stored in the following format
     * Key-the first word in the multiwords
     * Value-Vector of Vectors, which contain the other words in the all the multiwords
     * starting from the word in Key.
     *
     * @param fileName the file name from where the hash table will be read
     * @return multiwords hastable
     */
    private static HashMap<String, ArrayList<ArrayList<String>>> readHash(String fileName) throws SMatchException {
        return (HashMap<String, ArrayList<ArrayList<String>>>) SMatchUtils.readObject(fileName);
    }

}