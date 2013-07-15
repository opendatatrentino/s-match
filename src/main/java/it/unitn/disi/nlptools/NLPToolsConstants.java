package it.unitn.disi.nlptools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Holds various constants needed for NLPTools.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NLPToolsConstants {

    //Constant pos
    public final static char NOUN = 'n';
    public final static char ADJECTIVE = 'a';
    public final static char VERB = 'v';
    public final static char ADVERB = 'r';

    //constant pos from Penn Treebank Tagsets
    public final static String COORDINATING_CON = "CC"; //Coordinating conjunction
    public final static String CARDINAL_NO = "CD"; //Cardinal number
    public final static String DETERMINER = "DT"; //Determiner
    public final static String EXIST_THERE = "EX"; //Existential there
    public final static String FOREIGN = "FW"; //Foreign word
    public final static String PREP_SUBCON = "IN"; //Preposition or subordinating conjunction
    public final static String ADJ = "JJ"; //Adjective
    public final static String ADJ_COMP = "JJR"; //Adjective, comparative
    public final static String ADJ_SUP = "JJS"; //Adjective, superlative
    public final static String LIST_ITEM = "LS"; //List item marker
    public final static String MODAL = "MD"; //Modal
    public final static String NOUN_SING = "NN"; //Noun, singular or mass
    public final static String NOUN_PL = "NNS"; //Noun, plural
    public final static String PROPER_NOUN_SING = "NNP"; //Proper noun, singular. see below *
    public final static String PROPER_NOUN_PL = "NNPS"; //Proper noun, plural. see below *
    public final static String PREDETERMINER = "PDT"; //Predeterminer
    public final static String POSSESSIVE_ENDING = "POS"; //Possessive ending
    public final static String PERSONAL_PRONOUN = "PRP"; //Personal pronoun. see below *
    public final static String POSSESSIVE_PRONOUN = "PP$"; //Possessive pronoun
    public final static String ADV = "RB"; //Adverb
    public final static String ADV_COMP = "RBR"; //Adverb, comparative
    public final static String ADV_SUP = "RBS"; //Adverb, superlative
    public final static String PARTICLE = "RP"; //Particle
    public final static String SYMBOL = "SYM"; //Symbol
    public final static String TO = "TO"; //to
    public final static String INTERJECTION = "UH"; //Interjection
    public final static String VERB_BASE = "VB"; //Verb, base form
    public final static String VERB_PAST = "VBD"; //Verb, past tense
    public final static String VERB_GERUND = "VBG"; //Verb, gerund or present participle
    public final static String VERB_PAST_PAR = "VBN"; //Verb, past participle
    public final static String VERB_NON3PER_SING = "VBP"; //Verb, non-3rd person singular present
    public final static String VERB_3PER_SING = "VBZ"; //Verb, 3rd person singular present
    public final static String WH_DETERMINER = "WDT"; //Wh-determiner
    public final static String WH_PRONOUN = "WP"; //Wh-pronoun
    public final static String POSSESSIVE_WH_PRONOUN = "WP$"; //Possessive wh-pronoun
    public final static String WH_ADVERB = "WRB"; // Wh-adverb
    //36

    public final static String PUNC_POUND_SIGN = "#"; // pound sign
    public final static String PUNC_DOLLAR_SIGN = "$"; // dollar sign
    public final static String PUNC_SENTENCE_FINAL = "."; // sentence final punctuation
    public final static String PUNC_COMMA = ","; // comma
    public final static String PUNC_COLON = ":"; // colon, semicolon
    public final static String PUNC_LB = "("; // left bracket character
    public final static String PUNC_RB = ")"; // right bracket character
    public final static String PUNC_DOUBLE_QUOTE = "\""; // straight double quote
    public final static String PUNC_LEFT_OPEN_SINGLE_QUOTE = "`"; // left open single quote
    public final static String PUNC_LEFT_OPEN_DOUBLE_QUOTE = "``"; // left open double quote
    public final static String PUNC_RIGHT_CLOSE_SINGLE_QUOTE = "'"; // right close single quote
    public final static String PUNC_RIGHT_CLOSE_DOUBLE_QUOTE = "''"; // right close double quote
    //48

    /* cl93.pdf, page 5
* In versions of the tagged corpus distributed before November 1992, singular proper nouns, plural proper nouns
and personal pronouns were tagged as NP	 NPS and PP respectively. The current tags NNP NNPS and PRP
were introduced in order to avoid confusion with the syntactic tags NP noun text and PP prepositional text
see Table 3 */

    /**
     * Maps POS tags to their descriptions.
     */
    public final static HashMap<String, String> posDescriptions = new HashMap<String, String>(36);

    static {
        posDescriptions.put(COORDINATING_CON, "Coordinating conjunction");
        posDescriptions.put(CARDINAL_NO, "Cardinal number");
        posDescriptions.put(DETERMINER, "Determiner");
        posDescriptions.put(EXIST_THERE, "Existential there");
        posDescriptions.put(FOREIGN, "Foreign word");
        posDescriptions.put(PREP_SUBCON, "Preposition or subordinating conjunction");
        posDescriptions.put(ADJ, "Adjective");
        posDescriptions.put(ADJ_COMP, "Adjective, comparative");
        posDescriptions.put(ADJ_SUP, "Adjective, superlative");
        posDescriptions.put(LIST_ITEM, "List item marker");
        posDescriptions.put(MODAL, "Modal");
        posDescriptions.put(NOUN_SING, "Noun, singular or mass");
        posDescriptions.put(NOUN_PL, "Noun, plural");
        posDescriptions.put(PROPER_NOUN_SING, "Proper noun, singular");
        posDescriptions.put(PROPER_NOUN_PL, "Proper noun, plural");
        posDescriptions.put(PREDETERMINER, "Predeterminer");
        posDescriptions.put(POSSESSIVE_ENDING, "Possessive ending");
        posDescriptions.put(PERSONAL_PRONOUN, "Personal pronoun");
        posDescriptions.put(POSSESSIVE_PRONOUN, "Possessive pronoun");
        posDescriptions.put(ADV, "Adverb");
        posDescriptions.put(ADV_COMP, "Adverb, comparative");
        posDescriptions.put(ADV_SUP, "Adverb, superlative");
        posDescriptions.put(PARTICLE, "Particle");
        posDescriptions.put(SYMBOL, "Symbol");
        posDescriptions.put(TO, "to");
        posDescriptions.put(INTERJECTION, "Interjection");
        posDescriptions.put(VERB_BASE, "Verb, base form");
        posDescriptions.put(VERB_PAST, "Verb, past tense");
        posDescriptions.put(VERB_GERUND, "Verb, gerund or present participle");
        posDescriptions.put(VERB_PAST_PAR, "Verb, past participle");
        posDescriptions.put(VERB_NON3PER_SING, "Verb, non-3rd person singular present");
        posDescriptions.put(VERB_3PER_SING, "Verb, 3rd person singular present");
        posDescriptions.put(WH_DETERMINER, "Wh-determiner");
        posDescriptions.put(WH_PRONOUN, "Wh-pronoun");
        posDescriptions.put(POSSESSIVE_WH_PRONOUN, "Possessive wh-pronoun");
        posDescriptions.put(WH_ADVERB, " Wh-adverb");
        posDescriptions.put(PUNC_POUND_SIGN, "Pound sign");
        posDescriptions.put(PUNC_DOLLAR_SIGN, "Dollar sign");
        posDescriptions.put(PUNC_SENTENCE_FINAL, "Sentence final punctuation");
        posDescriptions.put(PUNC_COMMA, "Comma");
        posDescriptions.put(PUNC_COLON, "Colon, semicolon");
        posDescriptions.put(PUNC_LB, "Left bracket character");
        posDescriptions.put(PUNC_RB, "Right bracket character");
        posDescriptions.put(PUNC_DOUBLE_QUOTE, "Straight double quote");
        posDescriptions.put(PUNC_LEFT_OPEN_SINGLE_QUOTE, "Left open single quote");
        posDescriptions.put(PUNC_LEFT_OPEN_DOUBLE_QUOTE, "Left open double quote");
        posDescriptions.put(PUNC_RIGHT_CLOSE_SINGLE_QUOTE, "Right close single quote");
        posDescriptions.put(PUNC_RIGHT_CLOSE_DOUBLE_QUOTE, "Right close double quote");
        posDescriptions.put(null, "");
    }

    /**
     * Open class POS.
     */
    public final static String[] ARR_POS_OPEN_CLASS = {
            CARDINAL_NO,
            FOREIGN,
            ADJ,
            ADJ_COMP,
            ADJ_SUP,
            NOUN_SING,
            NOUN_PL,
            PROPER_NOUN_SING,
            PROPER_NOUN_PL,
            ADV,
            ADV_COMP,
            ADV_SUP,
            SYMBOL,
            VERB_BASE,
            VERB_PAST,
            VERB_GERUND,
            VERB_PAST_PAR,
            VERB_NON3PER_SING,
            VERB_3PER_SING
    };

    public static final HashSet<String> H_POS_OPEN_CLASS = new HashSet<String>(Arrays.asList(NLPToolsConstants.ARR_POS_OPEN_CLASS));


    /**
     * Noun POS.
     */
    public final static String[] ARR_POS_NOUN = {
            NOUN_SING,
            NOUN_PL,
            PROPER_NOUN_SING,
            PROPER_NOUN_PL,
    };

    /**
     * Adjective POS.
     */
    public final static String[] ARR_POS_ADJ = {
            ADJ,
            ADJ_COMP,
            ADJ_SUP,
    };

    /**
     * Verb POS.
     */
    public final static String[] ARR_POS_VERB = {
            VERB_BASE,
            VERB_PAST,
            VERB_GERUND,
            VERB_PAST_PAR,
            VERB_NON3PER_SING,
            VERB_3PER_SING
    };

    /**
     * Adverb POS.
     */
    public final static String[] ARR_POS_ADV = {
            ADV,
            ADV_COMP,
            ADV_SUP,
    };

    /**
     * Punctuation POS.
     */
    public final static String[] ARR_POS_PUNC = {
            PUNC_POUND_SIGN,
            PUNC_DOLLAR_SIGN,
            PUNC_SENTENCE_FINAL,
            PUNC_COMMA,
            PUNC_COLON,
            PUNC_LB,
            PUNC_RB,
            PUNC_DOUBLE_QUOTE,
            PUNC_LEFT_OPEN_SINGLE_QUOTE,
            PUNC_LEFT_OPEN_DOUBLE_QUOTE,
            PUNC_RIGHT_CLOSE_SINGLE_QUOTE,
            PUNC_RIGHT_CLOSE_DOUBLE_QUOTE
    };

    /**
     * All POS.
     */
    public final static String[] ARR_POS_ALL = {
            NOUN_SING,
            NOUN_PL,
            PROPER_NOUN_SING,
            PROPER_NOUN_PL,
            ADJ,
            ADJ_COMP,
            ADJ_SUP,
            COORDINATING_CON,
            CARDINAL_NO,
            DETERMINER,
            PREP_SUBCON,
            ADV,
            ADV_COMP,
            ADV_SUP,
            FOREIGN,
            VERB_BASE,
            VERB_PAST,
            VERB_GERUND,
            VERB_PAST_PAR,
            VERB_NON3PER_SING,
            VERB_3PER_SING,
            EXIST_THERE,
            LIST_ITEM,
            MODAL,
            PREDETERMINER,
            POSSESSIVE_ENDING,
            PERSONAL_PRONOUN,
            POSSESSIVE_PRONOUN,
            PARTICLE,
            SYMBOL,
            TO,
            INTERJECTION,
            WH_DETERMINER,
            WH_PRONOUN,
            POSSESSIVE_WH_PRONOUN,
            WH_ADVERB,
            PUNC_POUND_SIGN,
            PUNC_DOLLAR_SIGN,
            PUNC_SENTENCE_FINAL,
            PUNC_COMMA,
            PUNC_COLON,
            PUNC_LB,
            PUNC_RB,
            PUNC_DOUBLE_QUOTE,
            PUNC_LEFT_OPEN_SINGLE_QUOTE,
            PUNC_LEFT_OPEN_DOUBLE_QUOTE,
            PUNC_RIGHT_CLOSE_SINGLE_QUOTE,
            PUNC_RIGHT_CLOSE_DOUBLE_QUOTE
    };

    public static final HashSet<String> H_POS_ALL = new HashSet<String>(Arrays.asList(NLPToolsConstants.ARR_POS_ALL));

    //PennTrees
    public final static String P_ADJP = "ADJP";//adjective text
    public final static String P_ADVP = "ADVP";//adverbial text
    public final static String P_NP = "NP";//noun text ;)
    public final static String P_PP = "PP";//prepositional text
    public final static String P_S = "S";//declarative clause
    public final static String P_SBAR = "SBAR";//clause int-d by subordinating conjunction
    public final static String P_SBARQ = "SBARQ";//direct question intr-d by WH word or text
    public final static String P_SINV = "SINV";//decl sentence with subject-aux inversion
    public final static String P_SQ = "SQ";//subconstituent of SBARQ excl wh-word or prhase
    public final static String P_VP = "VP";//verb text
    public final static String P_WHADVP = "WHADVP";//WH adverb text
    public final static String P_WHNP = "WHNP";//WH noun text
    public final static String P_WHPP = "WHPP";//WH prepositional text
    public final static String P_X = "X";//unknown or uncertain category
    public final static String P_NX = "NX";//unkn or uncertain, noun?

    //formal language
    public final static String DISJ_FL = "|";
    public final static String CONJ_FL = "&";
    public final static String NEGA_FL = "~";

    //multiword separator
    public final static String MW_SEPARATOR = " ";//root node label that are not analysed

    //"meaningless" top labels
    public final static String[] ROOT_LABELS = {"Top", "Thing"};
    public final static HashSet<String> H_ROOT_LABELS = new HashSet<String>(Arrays.asList(ROOT_LABELS));

    public final static String[] WH_ADVERBS = {"when", "where", "why", "how"};
    public static final HashSet<String> H_WH_ADVERBS = new HashSet<String>(Arrays.asList(NLPToolsConstants.WH_ADVERBS));


    //COORDINATING CONJUNCTIONS: for and nor but or yet so (FANBOYS)
    //CORRELATIVE CONJUNCTIONS: both...and not only...but also either...or neither...nor whether...or
    //in the list below we excluded "FOR" as it is much more often is used as a preposition rather than conjunction
    public final static String[] CONJUNCTIONS = {"after", "although", "and", "&", "as", "because", "before", "both",
            "but", "either", "except", "neither", "nor",
            "once", "only", "or", "since", "so", "though",
            "until", "when", "whenever", "whereas", "whether",
            "while", "yet"};

    public static final HashSet<String> H_CONJUNCTIONS = new HashSet<String>(Arrays.asList(NLPToolsConstants.CONJUNCTIONS));

    //almost all prepositions have a locational or temporal meaning.
    //Prepositions of Movement | Prepositions of Place | Prepositions of Time (point of time and length of time)
    public final static String[] PREPOSITIONS = {"aboard", "about", "above", "across", "after", "against",
            "along", "alongside", "amid", "amidst", "among", "amongst", "anti", "around", "as", "at",
            "before", "behind", "below", "beneath", "beside",
            "besides", "between", "betwixt", "beyond", "by", "despite", "down",
            "during", "except", "excepting", "excluding", "following",
            "for", "from", "in", "inside", "into", "opposite", "out", "like", "near",
            "of", "off", "on", "onto", "opposite", "outside", "over",
            "past", "per", "plus", "round", "save", "since", "than",
            "through", "throughout", "till", "to", "toward", "towards", "under", "underneath",
            "unlike", "until", "up", "upon", "versus", "via", "with",
            "within", "without"};

    public static final HashSet<String> H_PREPOSITIONS = new HashSet<String>(Arrays.asList(NLPToolsConstants.PREPOSITIONS));

    //In English the pronouns are classified as personal (I, we, you, thou, he, she, it, they),
    // demonstrative (this, these, that, those), relative (who, which, that, as), indefinite
    // (e.g., each, all, everyone, either, one, both, any, such, somebody), interrogative
    // (who, which, what), possessive, sometimes termed possessive adjectives (my, your, his,
    // her, our, their), and reflexive (e.g., myself, herself)
    public final static String[] PRONOUNS = {"i", "we", "you", "thou", "he", "she", "it", "they",
            "this", "these", "that", "those", "who", "which", "that",
            "as", "each", "all", "everyone", "either", "one", "both",
            "any", "such", "somebody", "who", "which", "what", "my", "your",
            "his", "her", "our", "their", "myself", "herself", "themselves",
            "itself", "ourselves", "yourselves"};

    public static final HashSet<String> H_PRONOUNS = new HashSet<String>(Arrays.asList(NLPToolsConstants.PRONOUNS));

    //In grammar, the words a, an, and the, which precede a noun or its modifier.
    // The is the definite article; a and an are indefinite articles.
    public final static String[] ARTICLES = {"a", "an", "the"};//We don't use ' as a delimiter
    public static final HashSet<String> H_ARTICLES = new HashSet<String>(Arrays.asList(NLPToolsConstants.ARTICLES));

    public static final HashSet<String> H_CLOSED_CLASS_TOKENS = new HashSet<String>();

    static {
        H_CLOSED_CLASS_TOKENS.addAll(H_WH_ADVERBS);
        H_CLOSED_CLASS_TOKENS.addAll(H_CONJUNCTIONS);
        H_CLOSED_CLASS_TOKENS.addAll(H_PREPOSITIONS);
        H_CLOSED_CLASS_TOKENS.addAll(H_PRONOUNS);
        H_CLOSED_CLASS_TOKENS.addAll(H_ARTICLES);
        H_CLOSED_CLASS_TOKENS.add(",");
        H_CLOSED_CLASS_TOKENS.add("(");
        H_CLOSED_CLASS_TOKENS.add(")");
    }

    public final static String DELIMITERS = "[ _\t:!?\\-\\+/=()\\[\\]<>{}#&\\\"]"; //the set of delimeters for tokenizer
    public final static String DELIMITERS_EXCLUDING_BRACKETS = "[ _\t:!?\\-\\+/=<>#&\\\"]+"; //the set of delimeters for tokenizer
    public final static String DELIMITERS_OF_FORMULA = "[|&]+"; //the set of delimeters for formula

    //tips for UI...
    public final static String PREPOSITION_TIP = "Prepositions like 'from', 'in', 'to', 'of' are logically considered as conjunctions of the terms they relate.";
    public final static String CONJ_DISJ_TIP = "Conjunctions like 'and', 'or' are logically considered as disjunctions of the terms they relate.";
    public final static String CONJUNCTION_TIP = "Conjunctions like 'after', 'although', 'as', 'because', 'before', 'both', etc. are logically considered as conjunctions.";
    public final static String PUNC_MARK_TIP = "Commas, semicolons and other punctuation marks are translated into logical disjunctions.";
    public final static String NON_CONCEPT_TIP = "The word is not present in the controlled vocabulary. Click on the word box and then click 'Manage senses' to add this word to the controlled vocabulary.";
    public final static String ARTICLE_TIP = "Articles are ignored during semantic conversion.";//NL conjunctions which are tranlsated into disjuncitons

    public static final String[] INC_DISJUNCTIONS = {"and", "or"};

    public static final List<String> INC_DISJ_V = Arrays.asList(INC_DISJUNCTIONS);
    public static final List<String> CONJ_V = Arrays.asList(CONJUNCTIONS);
    public static final List<String> PREP_V = Arrays.asList(PREPOSITIONS);
    public static final List<String> PRON_V = Arrays.asList(PRONOUNS);
    public static final List<String> ART_V = Arrays.asList(ARTICLES);

    //Named Entities TAGS
    //B-I-O tags for
    public static final String NE_OUT = "O";
    public static final String NE_BEGIN = "B-";
    public static final String NE_IN = "I-";

    public static final String NE_ORGANIZATION = "ORG";
    public static final String NE_LOCATION = "LOC";
    public static final String NE_PERSON = "PERS";
    public static final String NE_MISC = "MISC";

    public static final String NEB_ORGANIZATION = NE_BEGIN + NE_ORGANIZATION;
    public static final String NEB_LOCATION = NE_BEGIN + NE_LOCATION;
    public static final String NEB_PERSON = NE_BEGIN + NE_PERSON;
    public static final String NEB_MISC = NE_BEGIN + NE_MISC;
    public static final String NEI_ORGANIZATION = NE_IN + NE_ORGANIZATION;
    public static final String NEI_LOCATION = NE_IN + NE_LOCATION;
    public static final String NEI_PERSON = NE_IN + NE_PERSON;
    public static final String NEI_MISC = NE_IN + NE_MISC;

    public static final String[] ARR_NE_CATEGORIES = {
            NE_LOCATION,
            NE_PERSON,
            NE_ORGANIZATION,
            NE_MISC
    };

    public static final String[] ARR_NE_ALL = {
            NE_OUT,
            NEB_LOCATION,
            NEB_PERSON,
            NEB_ORGANIZATION,
            NEB_MISC,
            NEI_LOCATION,
            NEI_PERSON,
            NEI_ORGANIZATION,
            NEI_MISC
    };

    public static final HashSet<String> H_NE = new HashSet<String>(Arrays.asList(ARR_NE_ALL));
}
