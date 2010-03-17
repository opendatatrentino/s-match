package it.unitn.disi.smatch.matchers.element.string;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.IStringBasedElementLevelSemanticMatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Implements GPrefix matcher.
 * Tries to use morphological knowledge (suffixes) to enhance relations returned.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class GPrefix implements IStringBasedElementLevelSemanticMatcher {

    private static int invocationCount = 0;
    private static int relCount = 0;
    private static HashSet<String> hm = new HashSet<String>();

    // TODO Perhaps the commenting will be about prefix. Need revision.

    //suffix -> relation
    //based on http://en.wiktionary.org/wiki/Appendix:Suffixes:English
    //in most cases = left
    //however may be > here in general case, because suffix specifies the meaning
    //also here http://en.wikipedia.org/wiki/List_of_English_suffixes
    //but not considered (yet)

    // TODO variable name is suffix, but this is prefix matcher
    private static HashMap<String, Character> suffixes = new HashMap<String, Character>();

    static {
        suffixes.put("a", MatchManager.SYNOMYM);
        suffixes.put("ability", MatchManager.SYNOMYM);
        suffixes.put("able", MatchManager.SYNOMYM);
        suffixes.put("ably", MatchManager.SYNOMYM);
        suffixes.put("ac", MatchManager.SYNOMYM);
        //in WM lots with spaces, others are unrelated
        //suffixes.put("acea", MatchManager.IDK_RELATION);//   1. taxonomic superfamily of plants, algae and fungi
        //in WM lots with spaces
        suffixes.put("aceae", MatchManager.SYNOMYM);
        suffixes.put("acean", MatchManager.SYNOMYM);
        suffixes.put("aceous", MatchManager.SYNOMYM);
        suffixes.put("ad", MatchManager.SYNOMYM);
        suffixes.put("ade", MatchManager.SYNOMYM);
        suffixes.put("aemia", MatchManager.SYNOMYM);
        suffixes.put("age", MatchManager.SYNOMYM);
        suffixes.put("agog", MatchManager.SYNOMYM);
        suffixes.put("agogue", MatchManager.SYNOMYM);
        suffixes.put("aholic", MatchManager.SYNOMYM);
        suffixes.put("al", MatchManager.SYNOMYM);
        suffixes.put("ales", MatchManager.SYNOMYM);
        suffixes.put("algia", MatchManager.SYNOMYM);
        suffixes.put("amine", MatchManager.SYNOMYM);
        suffixes.put("an", MatchManager.SYNOMYM);
        suffixes.put("ana", MatchManager.SYNOMYM);
        suffixes.put("anae", MatchManager.SYNOMYM);
        suffixes.put("ance", MatchManager.SYNOMYM);
        suffixes.put("ancy", MatchManager.SYNOMYM);
        suffixes.put("androus", MatchManager.SYNOMYM);
        suffixes.put("andry", MatchManager.SYNOMYM);
        suffixes.put("ane", MatchManager.SYNOMYM);
        suffixes.put("ant", MatchManager.SYNOMYM);
        suffixes.put("ar", MatchManager.SYNOMYM);
        suffixes.put("arch", MatchManager.SYNOMYM);
        suffixes.put("archy", MatchManager.SYNOMYM);
        suffixes.put("ard", MatchManager.SYNOMYM);
        suffixes.put("aria", MatchManager.SYNOMYM);
        suffixes.put("arian", MatchManager.SYNOMYM);
        suffixes.put("arium", MatchManager.SYNOMYM);
        suffixes.put("art", MatchManager.SYNOMYM);
        suffixes.put("ary", MatchManager.SYNOMYM);
        suffixes.put("ase", MatchManager.SYNOMYM);
        suffixes.put("ate", MatchManager.SYNOMYM);
        suffixes.put("athon", MatchManager.SYNOMYM);
        suffixes.put("ation", MatchManager.SYNOMYM);
        suffixes.put("ative", MatchManager.SYNOMYM);
        suffixes.put("ator", MatchManager.SYNOMYM);
        suffixes.put("atory", MatchManager.SYNOMYM);
        suffixes.put("biont", MatchManager.SYNOMYM);
        suffixes.put("biosis", MatchManager.SYNOMYM);
        suffixes.put("blast", MatchManager.SYNOMYM);
        suffixes.put("cade", MatchManager.SYNOMYM);
        suffixes.put("caine", MatchManager.SYNOMYM);
        suffixes.put("carp", MatchManager.SYNOMYM);
        suffixes.put("carpic", MatchManager.SYNOMYM);
        suffixes.put("carpous", MatchManager.SYNOMYM);
        suffixes.put("cele", MatchManager.SYNOMYM);
        suffixes.put("cene", MatchManager.SYNOMYM);
        suffixes.put("centric", MatchManager.SYNOMYM);
        suffixes.put("cephalic", MatchManager.SYNOMYM);
        suffixes.put("cephalous", MatchManager.SYNOMYM);
        suffixes.put("cephaly", MatchManager.SYNOMYM);
        suffixes.put("chore", MatchManager.SYNOMYM);
        suffixes.put("chory", MatchManager.SYNOMYM);
        suffixes.put("chrome", MatchManager.SYNOMYM);
        suffixes.put("cide", MatchManager.SYNOMYM);
        suffixes.put("clinal", MatchManager.SYNOMYM);
        suffixes.put("cline", MatchManager.SYNOMYM);
        suffixes.put("clinic", MatchManager.SYNOMYM);
        suffixes.put("coccus", MatchManager.SYNOMYM);
        suffixes.put("coel", MatchManager.SYNOMYM);
        suffixes.put("coele", MatchManager.SYNOMYM);
        suffixes.put("colous", MatchManager.SYNOMYM);
        suffixes.put("cracy", MatchManager.SYNOMYM);
        suffixes.put("crat", MatchManager.SYNOMYM);
        suffixes.put("cratic", MatchManager.SYNOMYM);
        suffixes.put("cratical", MatchManager.SYNOMYM);
        suffixes.put("cy", MatchManager.SYNOMYM);
        suffixes.put("cyte", MatchManager.SYNOMYM);
        suffixes.put("derm", MatchManager.SYNOMYM);
        suffixes.put("derma", MatchManager.SYNOMYM);
        suffixes.put("dermatous", MatchManager.SYNOMYM);
        suffixes.put("dom", MatchManager.SYNOMYM);
        suffixes.put("drome", MatchManager.SYNOMYM);
        suffixes.put("dromous", MatchManager.SYNOMYM);
        suffixes.put("eae", MatchManager.SYNOMYM);
        suffixes.put("ectomy", MatchManager.SYNOMYM);
        suffixes.put("ed", MatchManager.SYNOMYM);
        suffixes.put("ee", MatchManager.SYNOMYM);
        suffixes.put("eer", MatchManager.SYNOMYM);
        suffixes.put("ein", MatchManager.SYNOMYM);
        suffixes.put("eme", MatchManager.SYNOMYM);
        suffixes.put("emia", MatchManager.SYNOMYM);
        suffixes.put("en", MatchManager.SYNOMYM);
        suffixes.put("ence", MatchManager.SYNOMYM);
        suffixes.put("enchyma", MatchManager.SYNOMYM);
        suffixes.put("ency", MatchManager.SYNOMYM);
        suffixes.put("ene", MatchManager.SYNOMYM);
        suffixes.put("ent", MatchManager.SYNOMYM);
        suffixes.put("eous", MatchManager.SYNOMYM);
        suffixes.put("er", MatchManager.SYNOMYM);
        suffixes.put("ern", MatchManager.SYNOMYM);
        suffixes.put("ergic", MatchManager.SYNOMYM);
        suffixes.put("ergy", MatchManager.SYNOMYM);
        suffixes.put("es", MatchManager.SYNOMYM);
        suffixes.put("escence", MatchManager.SYNOMYM);
        suffixes.put("escent", MatchManager.SYNOMYM);
        suffixes.put("ese", MatchManager.SYNOMYM);
        suffixes.put("esque", MatchManager.SYNOMYM);
        suffixes.put("ess", MatchManager.SYNOMYM);
        suffixes.put("est", MatchManager.SYNOMYM);
        suffixes.put("et", MatchManager.SYNOMYM);
        suffixes.put("eth", MatchManager.SYNOMYM);
        suffixes.put("etic", MatchManager.SYNOMYM);
        suffixes.put("ette", MatchManager.SYNOMYM);
        suffixes.put("ey", MatchManager.SYNOMYM);
        suffixes.put("facient", MatchManager.SYNOMYM);
        suffixes.put("faction", MatchManager.SYNOMYM);
        suffixes.put("fer", MatchManager.SYNOMYM);
        suffixes.put("ferous", MatchManager.SYNOMYM);
        suffixes.put("fic", MatchManager.SYNOMYM);
        suffixes.put("fication", MatchManager.SYNOMYM);
        suffixes.put("fid", MatchManager.SYNOMYM);
        suffixes.put("florous", MatchManager.SYNOMYM);
        suffixes.put("fold", MatchManager.SYNOMYM);
        suffixes.put("foliate", MatchManager.SYNOMYM);
        suffixes.put("foliolate", MatchManager.SYNOMYM);
        suffixes.put("form", MatchManager.SYNOMYM);
        suffixes.put("fuge", MatchManager.SYNOMYM);
        suffixes.put("ful", MatchManager.SYNOMYM);
        suffixes.put("fy", MatchManager.SYNOMYM);
        suffixes.put("gamous", MatchManager.SYNOMYM);
        suffixes.put("gamy", MatchManager.SYNOMYM);
        suffixes.put("gate", MatchManager.SYNOMYM);
        suffixes.put("gen", MatchManager.SYNOMYM);
        suffixes.put("gene", MatchManager.SYNOMYM);
        suffixes.put("genesis", MatchManager.SYNOMYM);
        suffixes.put("genetic", MatchManager.SYNOMYM);
        suffixes.put("genic", MatchManager.SYNOMYM);
        suffixes.put("genous", MatchManager.SYNOMYM);
        suffixes.put("geny", MatchManager.SYNOMYM);
        suffixes.put("gnathous", MatchManager.SYNOMYM);
        suffixes.put("gon", MatchManager.SYNOMYM);
        suffixes.put("gony", MatchManager.SYNOMYM);
        suffixes.put("gram", MatchManager.SYNOMYM);
        suffixes.put("graph", MatchManager.SYNOMYM);
        suffixes.put("grapher", MatchManager.SYNOMYM);
        suffixes.put("graphy", MatchManager.SYNOMYM);
        suffixes.put("gyne", MatchManager.SYNOMYM);
        suffixes.put("gynous", MatchManager.SYNOMYM);
        suffixes.put("gyny", MatchManager.SYNOMYM);
        suffixes.put("hood", MatchManager.SYNOMYM);
        suffixes.put("ia", MatchManager.SYNOMYM);
        suffixes.put("ial", MatchManager.SYNOMYM);
        suffixes.put("ian", MatchManager.SYNOMYM);
        suffixes.put("iana", MatchManager.SYNOMYM);
        suffixes.put("iasis", MatchManager.SYNOMYM);
        suffixes.put("iatric", MatchManager.SYNOMYM);
        suffixes.put("iatrics", MatchManager.SYNOMYM);
        suffixes.put("iatry", MatchManager.SYNOMYM);
        suffixes.put("ibility", MatchManager.SYNOMYM);
        suffixes.put("ible", MatchManager.SYNOMYM);
        suffixes.put("ic", MatchManager.SYNOMYM);
        suffixes.put("ical", MatchManager.SYNOMYM);
        suffixes.put("ically", MatchManager.SYNOMYM);
        suffixes.put("ician", MatchManager.SYNOMYM);
        suffixes.put("ics", MatchManager.SYNOMYM);
        suffixes.put("id", MatchManager.SYNOMYM);
        suffixes.put("idae", MatchManager.SYNOMYM);
        suffixes.put("ide", MatchManager.SYNOMYM);
        suffixes.put("ie", MatchManager.SYNOMYM);
        suffixes.put("ify", MatchManager.SYNOMYM);
        suffixes.put("ile", MatchManager.SYNOMYM);
        suffixes.put("in", MatchManager.SYNOMYM);
        suffixes.put("ina", MatchManager.SYNOMYM);
        suffixes.put("inae", MatchManager.SYNOMYM);
        suffixes.put("ine", MatchManager.SYNOMYM);
        suffixes.put("ineae", MatchManager.SYNOMYM);
        suffixes.put("ing", MatchManager.SYNOMYM);
        suffixes.put("ini", MatchManager.SYNOMYM);
        suffixes.put("ion", MatchManager.SYNOMYM);
        suffixes.put("ious", MatchManager.SYNOMYM);
        suffixes.put("isation", MatchManager.SYNOMYM);
        suffixes.put("ise", MatchManager.SYNOMYM);
        suffixes.put("ish", MatchManager.SYNOMYM);
        suffixes.put("ism", MatchManager.SYNOMYM);
        suffixes.put("ist", MatchManager.SYNOMYM);
        suffixes.put("ite", MatchManager.SYNOMYM);
        suffixes.put("itious", MatchManager.SYNOMYM);
        suffixes.put("itis", MatchManager.SYNOMYM);
        suffixes.put("ity", MatchManager.SYNOMYM);
        suffixes.put("ium", MatchManager.SYNOMYM);
        suffixes.put("ive", MatchManager.SYNOMYM);
        suffixes.put("ix", MatchManager.SYNOMYM);
        suffixes.put("ization", MatchManager.SYNOMYM);
        suffixes.put("ize", MatchManager.SYNOMYM);
        suffixes.put("i", MatchManager.SYNOMYM);
        suffixes.put("kin", MatchManager.SYNOMYM);
        suffixes.put("kinesis", MatchManager.SYNOMYM);
        suffixes.put("kins", MatchManager.SYNOMYM);
        suffixes.put("latry", MatchManager.SYNOMYM);
        suffixes.put("lepry", MatchManager.SYNOMYM);
        suffixes.put("less", MatchManager.OPPOSITE_MEANING);//lacking something
        suffixes.put("let", MatchManager.SYNOMYM);
        suffixes.put("like", MatchManager.SYNOMYM);
        suffixes.put("ling", MatchManager.SYNOMYM);
        suffixes.put("lite", MatchManager.SYNOMYM);
        suffixes.put("lith", MatchManager.SYNOMYM);
        suffixes.put("lithic", MatchManager.SYNOMYM);
        suffixes.put("log", MatchManager.SYNOMYM);
        suffixes.put("logue", MatchManager.SYNOMYM);
        suffixes.put("logic", MatchManager.SYNOMYM);
        suffixes.put("logical", MatchManager.SYNOMYM);
        suffixes.put("logist", MatchManager.SYNOMYM);
        suffixes.put("logy", MatchManager.SYNOMYM);
        suffixes.put("ly", MatchManager.SYNOMYM);
        suffixes.put("lyse", MatchManager.SYNOMYM);
        /*
   1. decomposition or breakdown
   2. dissolving
   3. disintegration
         */
        suffixes.put("lysis", MatchManager.OPPOSITE_MEANING);
        suffixes.put("lyte", MatchManager.SYNOMYM);
        suffixes.put("lytic", MatchManager.SYNOMYM);
        suffixes.put("lyze", MatchManager.SYNOMYM);
        suffixes.put("mancy", MatchManager.SYNOMYM);
        suffixes.put("mania", MatchManager.SYNOMYM);
        suffixes.put("meister", MatchManager.SYNOMYM);
        suffixes.put("ment", MatchManager.SYNOMYM);
        suffixes.put("mer", MatchManager.SYNOMYM);
        suffixes.put("mere", MatchManager.SYNOMYM);
        suffixes.put("merous", MatchManager.SYNOMYM);
        suffixes.put("meter", MatchManager.SYNOMYM);
        suffixes.put("metric", MatchManager.SYNOMYM);
        suffixes.put("metrics", MatchManager.SYNOMYM);
        suffixes.put("metry", MatchManager.SYNOMYM);
        suffixes.put("mo", MatchManager.SYNOMYM);
        suffixes.put("morph", MatchManager.SYNOMYM);
        suffixes.put("morphic", MatchManager.SYNOMYM);
        suffixes.put("morphism", MatchManager.SYNOMYM);
        suffixes.put("morphous", MatchManager.SYNOMYM);
        suffixes.put("most", MatchManager.SYNOMYM);
        suffixes.put("mycete", MatchManager.SYNOMYM);
        suffixes.put("mycetes", MatchManager.SYNOMYM);
        suffixes.put("mycetidae", MatchManager.SYNOMYM);
        suffixes.put("mycin", MatchManager.SYNOMYM);
        suffixes.put("mycota", MatchManager.SYNOMYM);
        suffixes.put("mycotina", MatchManager.SYNOMYM);
        suffixes.put("n", MatchManager.SYNOMYM);//geographical africa - african
        suffixes.put("n't", MatchManager.SYNOMYM);
        suffixes.put("nasty", MatchManager.SYNOMYM);
        suffixes.put("ness", MatchManager.SYNOMYM);
        suffixes.put("nik", MatchManager.SYNOMYM);
        suffixes.put("nomy", MatchManager.SYNOMYM);
        suffixes.put("o", MatchManager.SYNOMYM);
        suffixes.put("ode", MatchManager.SYNOMYM);
        suffixes.put("odon", MatchManager.SYNOMYM);
        suffixes.put("odont", MatchManager.SYNOMYM);
        suffixes.put("odontia", MatchManager.SYNOMYM);
        suffixes.put("oholic", MatchManager.SYNOMYM);
        suffixes.put("oic", MatchManager.SYNOMYM);
        suffixes.put("oid", MatchManager.SYNOMYM);
        suffixes.put("oidea", MatchManager.SYNOMYM);
        suffixes.put("oideae", MatchManager.SYNOMYM);
        suffixes.put("ol", MatchManager.SYNOMYM);
        suffixes.put("ole", MatchManager.SYNOMYM);
        suffixes.put("oma", MatchManager.SYNOMYM);
        suffixes.put("ome", MatchManager.SYNOMYM);
        suffixes.put("on", MatchManager.SYNOMYM);
        suffixes.put("one", MatchManager.SYNOMYM);
        suffixes.put("ont", MatchManager.SYNOMYM);
        suffixes.put("onym", MatchManager.SYNOMYM);
        suffixes.put("onymy", MatchManager.SYNOMYM);
        suffixes.put("opia", MatchManager.SYNOMYM);
        suffixes.put("opsida", MatchManager.SYNOMYM);
        suffixes.put("opsis", MatchManager.SYNOMYM);
        suffixes.put("opsy", MatchManager.SYNOMYM);
        suffixes.put("or", MatchManager.SYNOMYM);
        suffixes.put("ory", MatchManager.SYNOMYM);
        suffixes.put("ose", MatchManager.SYNOMYM);
        suffixes.put("osis", MatchManager.SYNOMYM);
        suffixes.put("otic", MatchManager.SYNOMYM);
        suffixes.put("otomy", MatchManager.SYNOMYM);
        suffixes.put("ous", MatchManager.SYNOMYM);
        suffixes.put("o", MatchManager.SYNOMYM);
        suffixes.put("para", MatchManager.SYNOMYM);
        suffixes.put("parous", MatchManager.SYNOMYM);
        suffixes.put("path", MatchManager.SYNOMYM);
        suffixes.put("pathy", MatchManager.SYNOMYM);
        suffixes.put("ped", MatchManager.SYNOMYM);
        suffixes.put("pede", MatchManager.SYNOMYM);
        suffixes.put("penia", MatchManager.SYNOMYM);
        suffixes.put("petal", MatchManager.SYNOMYM);
        suffixes.put("phage", MatchManager.SYNOMYM);
        suffixes.put("phagia", MatchManager.SYNOMYM);
        suffixes.put("phagous", MatchManager.SYNOMYM);
        suffixes.put("phagy", MatchManager.SYNOMYM);
        suffixes.put("phane", MatchManager.SYNOMYM);
        suffixes.put("phasia", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("phil", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("phile", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("philia", MatchManager.MORE_GENERAL_THAN);//Used in the formation of nouns and adjectives meaning loving and friendly or love and friend
        suffixes.put("philiac", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("philic", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("philous", MatchManager.MORE_GENERAL_THAN);
        /*
   1. Used to form nouns meaning a person having a fear of a specific thing.
          claustrophobe
   2. Used to form nouns meaning a person who hates a particular type of person (due to their fear of that type of person).
          homophobe
         */
        suffixes.put("phobe", MatchManager.OPPOSITE_MEANING);
        suffixes.put("phobia", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("phobic", MatchManager.MORE_GENERAL_THAN);
        /*
   1. a type of sound e.g. allophone
   2. something that makes a sound e.g. saxophone
   3. a speaker of a certain language e.g. Francophone
   4. part of some classical names, e.g., Persephone, Tisiphone
         */
        suffixes.put("phone", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("phony", MatchManager.SYNOMYM);
        suffixes.put("phore", MatchManager.SYNOMYM);
        suffixes.put("phoresis", MatchManager.SYNOMYM);
        suffixes.put("phorous", MatchManager.SYNOMYM);
        suffixes.put("phrenia", MatchManager.SYNOMYM);
        suffixes.put("phyll", MatchManager.SYNOMYM);
        suffixes.put("phyllous", MatchManager.SYNOMYM);
        suffixes.put("phyceae", MatchManager.SYNOMYM);
        suffixes.put("phycidae", MatchManager.SYNOMYM);
        suffixes.put("phyta", MatchManager.SYNOMYM);
        suffixes.put("phyte", MatchManager.SYNOMYM);
        suffixes.put("phytina", MatchManager.SYNOMYM);
        suffixes.put("plasia", MatchManager.SYNOMYM);
        suffixes.put("plasm", MatchManager.SYNOMYM);
        suffixes.put("plast", MatchManager.SYNOMYM);
        suffixes.put("plastic", MatchManager.SYNOMYM);
        suffixes.put("plasty", MatchManager.SYNOMYM);
        suffixes.put("plegia", MatchManager.SYNOMYM);
        suffixes.put("plex", MatchManager.SYNOMYM);
        suffixes.put("ploid", MatchManager.SYNOMYM);
        suffixes.put("pod", MatchManager.SYNOMYM);
        suffixes.put("pode", MatchManager.SYNOMYM);
        suffixes.put("podous", MatchManager.SYNOMYM);
        suffixes.put("poieses", MatchManager.SYNOMYM);
        suffixes.put("poietic", MatchManager.SYNOMYM);
        suffixes.put("pter", MatchManager.SYNOMYM);
        suffixes.put("rrhagia", MatchManager.SYNOMYM);
        suffixes.put("rrhea", MatchManager.SYNOMYM);
        suffixes.put("ric", MatchManager.SYNOMYM);
        suffixes.put("ry", MatchManager.SYNOMYM);
        suffixes.put("'s", MatchManager.SYNOMYM);
        suffixes.put("s", MatchManager.SYNOMYM);
        suffixes.put("scope", MatchManager.MORE_GENERAL_THAN);//   1. instrument for viewing or examination
        suffixes.put("scopy", MatchManager.SYNOMYM);
        suffixes.put("scribe", MatchManager.SYNOMYM);
        suffixes.put("script", MatchManager.SYNOMYM);
        suffixes.put("sect", MatchManager.SYNOMYM);
        suffixes.put("sepalous", MatchManager.SYNOMYM);
        suffixes.put("ship", MatchManager.SYNOMYM);
        /*
   1. characterized by some specific condition or quality
          * Example: troublesome
   2. a group of a specified number of members
          * Example: foursome
         */
        suffixes.put("some", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("speak", MatchManager.SYNOMYM);
        suffixes.put("sperm", MatchManager.SYNOMYM);
        suffixes.put("sporous", MatchManager.SYNOMYM);
        suffixes.put("st", MatchManager.SYNOMYM);
        suffixes.put("stasis", MatchManager.SYNOMYM);
        suffixes.put("stat", MatchManager.SYNOMYM);
        suffixes.put("ster", MatchManager.SYNOMYM);
        suffixes.put("stome", MatchManager.SYNOMYM);
        suffixes.put("stomy", MatchManager.SYNOMYM);
        suffixes.put("taxis", MatchManager.SYNOMYM);
        suffixes.put("taxy", MatchManager.SYNOMYM);
        suffixes.put("th", MatchManager.SYNOMYM);
        suffixes.put("therm", MatchManager.SYNOMYM);
        suffixes.put("thermal", MatchManager.SYNOMYM);
        suffixes.put("thermic", MatchManager.SYNOMYM);
        suffixes.put("thermy", MatchManager.SYNOMYM);
        suffixes.put("thon", MatchManager.SYNOMYM);
        suffixes.put("thymia", MatchManager.SYNOMYM);
        suffixes.put("tion", MatchManager.SYNOMYM);
        suffixes.put("tome", MatchManager.SYNOMYM);
        suffixes.put("tomy", MatchManager.SYNOMYM);
        suffixes.put("tonia", MatchManager.SYNOMYM);
        suffixes.put("trichous", MatchManager.SYNOMYM);
        suffixes.put("trix", MatchManager.SYNOMYM);
        suffixes.put("tron", MatchManager.SYNOMYM);
        suffixes.put("trophic", MatchManager.SYNOMYM);
        suffixes.put("trophy", MatchManager.SYNOMYM);
        suffixes.put("tropic", MatchManager.SYNOMYM);
        suffixes.put("tropism", MatchManager.SYNOMYM);
        suffixes.put("tropous", MatchManager.SYNOMYM);
        suffixes.put("tropy", MatchManager.SYNOMYM);
        suffixes.put("tude", MatchManager.SYNOMYM);
        suffixes.put("ty", MatchManager.SYNOMYM);
        suffixes.put("ular", MatchManager.SYNOMYM);
        suffixes.put("ule", MatchManager.SYNOMYM);
        suffixes.put("ure", MatchManager.SYNOMYM);
        suffixes.put("urgy", MatchManager.SYNOMYM);
        suffixes.put("uria", MatchManager.SYNOMYM);
        suffixes.put("uronic", MatchManager.SYNOMYM);
        suffixes.put("urous", MatchManager.SYNOMYM);
        suffixes.put("valent", MatchManager.SYNOMYM);
        suffixes.put("virile", MatchManager.SYNOMYM);
        suffixes.put("vorous", MatchManager.SYNOMYM);
        suffixes.put("ward", MatchManager.SYNOMYM);
        suffixes.put("wards", MatchManager.SYNOMYM);
        suffixes.put("ware", MatchManager.SYNOMYM);
        suffixes.put("ways", MatchManager.SYNOMYM);
        suffixes.put("wide", MatchManager.SYNOMYM);
        suffixes.put("wise", MatchManager.SYNOMYM);
        suffixes.put("worthy", MatchManager.SYNOMYM);
        suffixes.put("xor", MatchManager.SYNOMYM);
        suffixes.put("y", MatchManager.SYNOMYM);
        suffixes.put("yl", MatchManager.SYNOMYM);
        suffixes.put("yne", MatchManager.SYNOMYM);
        suffixes.put("zoic", MatchManager.SYNOMYM);
        suffixes.put("zoon", MatchManager.SYNOMYM);
        suffixes.put("zygous", MatchManager.SYNOMYM);
        suffixes.put("zyme", MatchManager.SYNOMYM);

        //"roots"
        //e.g. fish is more general than parrot-fish
        //or it is a fish? :)
        suffixes.put("fish", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("fish's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("fishes", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("way", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("ways", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bird", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bird's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("birds", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("room", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("rooms", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("grass", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("grasses", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("boat", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("boats", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bush", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bushes", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bone", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bones", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("band", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bands", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("cake", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("cakes", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("shop", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("shops", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("mill", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("mills", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("paper", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("papers", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("worship", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("snake", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("snake's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("snakes", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("road", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("roads", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("hound", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("hound's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("hounds", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("care", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("cares", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("virus", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("virus'", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("viruses", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("storm", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("storms", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("sail", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("sail's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("sails", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("boot", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("boots", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bee", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bee's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("bees", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("ache", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("aches", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("wear", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("wears", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("tit", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("tits", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("tax", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("taxes", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("spoon", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("spoons", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("song", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("songs", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("builder", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("builder's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("builders", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("vine", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("vines", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("saddle", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("saddles", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("plant", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("plants", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("knife", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("knives", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("frog", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("frog's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("frogs", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("chop", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("chops", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("writer", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("writer's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("writers", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("wright", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("wrights", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("person", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("person's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("persons", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("owner", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("owner's", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("owners", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("mint", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("rack", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("racks", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("name", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("names", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("mast", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("in-law", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("fruit", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("fruits", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("pox", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("poxes", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("hide", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("force", MatchManager.MORE_GENERAL_THAN);
        suffixes.put("forces", MatchManager.MORE_GENERAL_THAN);
    }

    public char match(String str1, String str2) {
        invocationCount++;
        char rel;

        if (str1 == null || str2 == null) {
            rel = MatchManager.IDK_RELATION;
        } else {
            if ((str1.length() > 3) && (str2.length() > 3)) {
                if (str1.equals(str2)) {
                    rel = MatchManager.SYNOMYM;
                } else if (str1.startsWith(str2)) {
                    rel = matchPrefix(str1, str2);
                } else if (str2.startsWith(str1)) {
                    rel = matchPrefix(str2, str1);
                    rel = reverseRelation(rel);
                } else {
                    rel = MatchManager.IDK_RELATION;
                }
            } else {//if ((str1.length() > 3) && (str2.length() > 3)) {
                rel = MatchManager.IDK_RELATION;
            }
        }

        if (rel != MatchManager.IDK_RELATION) {
            relCount++;
            addCase(str1, str2, rel);
        }
        return rel;
    }

    /**
     * Computes relation with prefix matcher.
     *
     * @param str1 the source input
     * @param str2 the target input
     * @return synonym, more general, less general or IDK relation
     */
    private char matchPrefix(String str1, String str2) {
        //here always str1.startsWith(str2) colorless!color
        char rel = MatchManager.IDK_RELATION;
        int spacePos1 = str1.indexOf(' ');
        String suffix = str1.substring(str2.length());
        if (-1 < spacePos1 && !suffixes.containsKey(suffix)) {//check suffixes - pole vault=pole vaulter // TODO the matcher is prefix not suffix. need revision
            if (str2.length() == spacePos1) {//plant part<plant
                rel = MatchManager.LESS_GENERAL_THAN;
            } else {//plant part<plan
                String left = str1.substring(0, spacePos1);
                char secondRel = match(left, str2);
                if (MatchManager.MORE_GENERAL_THAN == secondRel ||
                        MatchManager.SYNOMYM == secondRel) {
                    rel = MatchManager.LESS_GENERAL_THAN;
                } else { //?,<,!
                    rel = secondRel;
                }
            }
        } else {
            //spelling: -tree and tree
            if (suffix.startsWith("-")) {
                suffix = suffix.substring(1);
            }
            if (suffix.endsWith("-") || suffix.endsWith(";") || suffix.endsWith(".") || suffix.endsWith(",") || suffix.endsWith("-")) {
                suffix = suffix.substring(0, suffix.length() - 1);
            }
            if (suffixes.containsKey(suffix)) {
                rel = suffixes.get(suffix);
                rel = reverseRelation(rel);
            }

            //another approximation = Gversion4
//            if (rel == MatchManager.LESS_GENERAL_THAN || rel == MatchManager.MORE_GENERAL_THAN) {
//                rel = MatchManager.SYNOMYM;
//            }
        }

        //filter = Gversion3
//        if (MatchManager.LESS_GENERAL_THAN == rel || MatchManager.MORE_GENERAL_THAN == rel) {
//            rel = MatchManager.SYNOMYM;
//        }

        return rel;
    }

    private char reverseRelation(char rel) {
        char res = rel;
        if (rel == MatchManager.MORE_GENERAL_THAN) {
            res = MatchManager.LESS_GENERAL_THAN;
        }
        if (rel == MatchManager.LESS_GENERAL_THAN) {
            res = MatchManager.MORE_GENERAL_THAN;
        }
        return res;
    }


    public void reportUsage() {
        System.out.println("GPrefix");
        System.out.println("GPrefix rel count = " + relCount);
        System.out.println("GPrefix invocation count = " + invocationCount);
        String[] arr = new String[hm.size()];
        hm.toArray(arr);
        Arrays.sort(arr);
        for (String entry : arr) {
            System.out.println(entry);
        }
        System.out.println("GPrefix rel count = " + hm.size());
        System.out.println("GPrefix");
    }

    private void addCase(String lstr1, String lstr2, char rel) {
        if (lstr1.compareTo(lstr2) < 0) {
            if (!hm.contains(lstr1 + rel + lstr2)) {
                hm.add(lstr1 + rel + lstr2);
            }
        } else {
            if (rel == MatchManager.MORE_GENERAL_THAN) {
                rel = MatchManager.LESS_GENERAL_THAN;
            } else {
                if (rel == MatchManager.LESS_GENERAL_THAN) {
                    rel = MatchManager.MORE_GENERAL_THAN;
                }
            }
            if (!hm.contains(lstr2 + rel + lstr1)) {
                hm.add(lstr2 + rel + lstr1);
            }
        }
    }
}
