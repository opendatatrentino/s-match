package it.unitn.disi.smatch.matchers.element.string;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.matchers.element.IStringBasedElementLevelSemanticMatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Implements GSuffix matcher.
 * Tries to use morphological knowledge (prefixes) to enhance relations returned.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */

public class GSuffix implements IStringBasedElementLevelSemanticMatcher {
    private static int invocationCount = 0;
    private static int relCount = 0;
    private static HashSet<String> hm = new HashSet<String>();

    // TODO Perhaps the commenting will be about suffix. Need revision.
    //prefix -> relation
    //based on http://en.wiktionary.org/wiki/Appendix:Prefixes:English

    // TODO variable name is prefix, but this is suffix matcher
    private static HashMap<String, Character> prefixes = new HashMap<String, Character>();

    //for roots
    private static HashMap<String, Character> suffixes = new HashMap<String, Character>();

    static {
        prefixes.put("a", MatchManager.SYNOMYM);
        prefixes.put("ab", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("abs", MatchManager.SYNOMYM);
        prefixes.put("ac", MatchManager.SYNOMYM);
        prefixes.put("acet", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("aceto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("acr", MatchManager.SYNOMYM);
        prefixes.put("acro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("actin", MatchManager.SYNOMYM);
        prefixes.put("actino", MatchManager.SYNOMYM);
        prefixes.put("ad", MatchManager.SYNOMYM);
        prefixes.put("aden", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("adeno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ae", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("aer", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("aero", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("af", MatchManager.SYNOMYM);
        prefixes.put("afro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ag", MatchManager.SYNOMYM);
        prefixes.put("agr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("agri", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("agro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("al", MatchManager.SYNOMYM);
        prefixes.put("allo", MatchManager.OPPOSITE_MEANING);
        prefixes.put("ambi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("amphi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("an", MatchManager.OPPOSITE_MEANING);
        prefixes.put("ana", MatchManager.OPPOSITE_MEANING);
        prefixes.put("and", MatchManager.SYNOMYM);
        prefixes.put("andr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("andro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("anemo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("angio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("anglo", MatchManager.LESS_GENERAL_THAN);//was Anglo
        prefixes.put("ano", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("antho", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("anthrop", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("anthropo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ante", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ant", MatchManager.OPPOSITE_MEANING);//variation of anti
        prefixes.put("anth", MatchManager.OPPOSITE_MEANING);//variation of anti
        prefixes.put("anti", MatchManager.OPPOSITE_MEANING);
        prefixes.put("ap", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("apo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("aqua", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("aque", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("aqui", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("arc", MatchManager.OPPOSITE_MEANING);
        prefixes.put("arch", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("archi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("archaeo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("archeo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("arithmo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("arterio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("arthr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("arthro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("astr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("astro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("atto", MatchManager.LESS_GENERAL_THAN);//10^-18 from danish "atten"
        prefixes.put("audio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("aut", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("auto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("azo", MatchManager.LESS_GENERAL_THAN);
        //prefixes.put("b", MatchManager.SYNOMYM);//was B
        prefixes.put("bacter", MatchManager.SYNOMYM);
        prefixes.put("bacteri", MatchManager.SYNOMYM);
        prefixes.put("bacterio", MatchManager.SYNOMYM);
        prefixes.put("bar", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("baro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bathy", MatchManager.LESS_GENERAL_THAN);//greek deep
        prefixes.put("be", MatchManager.SYNOMYM);
        prefixes.put("benz", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("benzo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bin", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("biblio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("blast", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("blasto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("brachy", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("brady", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("brom", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bromo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bronch", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bronchi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("broncho", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bry", MatchManager.SYNOMYM);
        prefixes.put("bryo", MatchManager.SYNOMYM);
        prefixes.put("by", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("bye", MatchManager.SYNOMYM);
        //prefixes.put("c", MatchManager.SYNOMYM);//was C
        prefixes.put("caco", MatchManager.OPPOSITE_MEANING);//From Ancient Greek ????? (kakos), an adjective that means bad.
        prefixes.put("carb", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("carbo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cardi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cardio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cel", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("celo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cen", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ceno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cent", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("centi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("centr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("centri", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cephal", MatchManager.LESS_GENERAL_THAN);//   1. (biology) relating to the brain or head
        prefixes.put("cephalo", MatchManager.LESS_GENERAL_THAN);//   1. (biology) relating to the brain or head
        prefixes.put("chalco", MatchManager.LESS_GENERAL_THAN);//copper, brass etc
        prefixes.put("cheiro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chem", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chemi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chemico", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chemo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chino", MatchManager.LESS_GENERAL_THAN);//was Chino
        prefixes.put("chiro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chlor", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chloro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("choan", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("choano", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chol", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chole", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("christo", MatchManager.LESS_GENERAL_THAN);//was Christo
        prefixes.put("chron", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chrono", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chrys", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("chryso", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cine", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("circum", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cis", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("co", MatchManager.SYNOMYM);
        prefixes.put("coel", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("coelo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("coen", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("coeno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("col", MatchManager.SYNOMYM);//con
        prefixes.put("com", MatchManager.SYNOMYM);//con
        prefixes.put("copr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("copro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("con", MatchManager.SYNOMYM);
        prefixes.put("contra", MatchManager.OPPOSITE_MEANING);
        prefixes.put("cor", MatchManager.SYNOMYM);//con
        prefixes.put("cosmo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("counter", MatchManager.OPPOSITE_MEANING);
        prefixes.put("cryo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("crypto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyan", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyano", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyber", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cycl", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyclo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyn", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyt", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("cyto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("de", MatchManager.OPPOSITE_MEANING);
        prefixes.put("dec", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("deca", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("deci", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("deka", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("demi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("deoxy", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("deuter", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("deutero", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("di", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dia", MatchManager.LESS_GENERAL_THAN);
        //prefixes.put("di", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dichlor", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dichloro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dinitro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dino", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dipl", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("diplo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dis", MatchManager.OPPOSITE_MEANING);
        //prefixes.put("di", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dodeca", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("dys", MatchManager.OPPOSITE_MEANING);
        prefixes.put("eco", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ecto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("eigen", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("electro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("em", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("en", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("endo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ennea", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ento", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("epi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("equi", MatchManager.SYNOMYM);
        prefixes.put("ethno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("eu", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("eur", MatchManager.LESS_GENERAL_THAN);//Eur
        prefixes.put("euro", MatchManager.LESS_GENERAL_THAN);//was Euro
        prefixes.put("ex", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("exa", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("exbi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("exo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("extra", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("femto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ferro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("fluor", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("fluoro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("for", MatchManager.OPPOSITE_MEANING);//   1. Meaning from.    2. Meaning against.
        prefixes.put("fore", MatchManager.OPPOSITE_MEANING);//   1. Meaning from.    2. Meaning against.
        prefixes.put("franco", MatchManager.LESS_GENERAL_THAN);//was Franco
        prefixes.put("gastr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("gastro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("genito", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("geo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("gibi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("giga", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("geno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("gymno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("gyn", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("gyno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("gyro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("haem", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("haemat", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("haemo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hagi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hagio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("half", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hect", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hecto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("helio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hem", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hemat", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hemi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hemo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hendeca", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hept", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hepta", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hetero", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hex", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hexa", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hipp", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hippo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hispano", MatchManager.LESS_GENERAL_THAN);//was Hispano
        prefixes.put("hist", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("histio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("histo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("holo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("homeo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("homo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("homoeo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hydro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hyper", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hypno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("hypo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("il", MatchManager.OPPOSITE_MEANING);//in
        prefixes.put("im", MatchManager.OPPOSITE_MEANING);//in
        /*
, "not", or "in", "into".

[edit] Prefix

in-

   1. Used with certain words to reverse their meaning

          Note: Before certain letters, the n. changes to another letter:
              * il- before l, eg. illegal
              * im- before b, m. or p, eg. improper
              * ir- before r, eg. irresistible

         1. Added to adjectives to mean not

              inedible
              inaccurate

         1. Added to nouns to mean lacking or without

              incredulity
              ineptitude

   2. Prefixed to certain words to give the senses of in, into, towards, within.

          inbreed
          inbound

         */
        prefixes.put("in", MatchManager.OPPOSITE_MEANING);
        prefixes.put("Indo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("inter", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("intra", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ir", MatchManager.OPPOSITE_MEANING);//in
        prefixes.put("iso", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("italo", MatchManager.LESS_GENERAL_THAN);//was Italo
        prefixes.put("kibi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("kilo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("lip", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("lipo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("lith", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("litho", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("macro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("mal", MatchManager.OPPOSITE_MEANING);
        prefixes.put("mebi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("mega", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("meso", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("meta", MatchManager.MORE_GENERAL_THAN);//-metasearch<search
        prefixes.put("metro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("micro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("midi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("milli", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("mini", MatchManager.LESS_GENERAL_THAN);
        /*
mis-

   1. bad, badly, wrong, wrongly
   2. lack or failure
         */
        prefixes.put("mis", MatchManager.OPPOSITE_MEANING);
        prefixes.put("miso", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("mono", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("multi", MatchManager.MORE_GENERAL_THAN);//-genre>multi-genre
        prefixes.put("myria", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("myxo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("nano", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("naso", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("necro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("neo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("non", MatchManager.OPPOSITE_MEANING);//   1. A prefix used in the sense of not to negate the meaning of the word to which it is attached, as in nonattention (or non-attention), nonconformity, nonmetallic and nonsuit.
        prefixes.put("nona", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("oct", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("octa", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("olig", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("oligo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("omni", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ortho", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("out", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("over", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ovi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("palaeo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("paleo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("para", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("pebi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("pent", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("penta", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("peta", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("phono", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("photo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("pico", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("poly", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("praeter", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("pre", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("preter", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("proto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("pseud", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("pseudo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("psycho", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ptero", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("pyro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("quadr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("quadri", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("quin", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("quinqu", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("quinque", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("radio", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("re", MatchManager.SYNOMYM);//Added to a noun or verb to make a new noun or verb being made again or done again (sometimes implying an undoing first, as "reintegrate"); as renew, revisit, remake etc.
        prefixes.put("robo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("schizo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("semi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("sept", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("septa", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("septem", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("septi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("sex", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("sexa", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("sino", MatchManager.LESS_GENERAL_THAN);//was Sino
        prefixes.put("step", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("sub", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("sui", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("super", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("supra", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("sym", MatchManager.SYNOMYM);
        prefixes.put("syn", MatchManager.SYNOMYM);
        prefixes.put("syl", MatchManager.SYNOMYM);
        prefixes.put("tebi", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("tele", MatchManager.SYNOMYM);
        prefixes.put("ter", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("tera", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("tetr", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("tetra", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("thermo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("tri", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ultra", MatchManager.LESS_GENERAL_THAN);
        /*
From Old English un-, from Germanic, related to Latin in-
[edit] Prefix
un-
    * Added to adjectives, nouns and verbs, to give the following meanings:
   1. Not; denoting absence
          unannounced (not being announced)
          uneducated (not educated)
   2. (of nouns) a lack of
          unattractiveness (lack of attractiveness; ugliness)
          unrest (a lack of rest [i.e., peace]; war)
   3. Violative of; contrary to
          unconstitutional (in violation of or contrary to the constitution)
[edit] Usage notes
    * Some words formed in this way also have counterparts using in- or non-.
         */
        prefixes.put("un", MatchManager.OPPOSITE_MEANING);
        prefixes.put("under", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("uni", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("up", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("ur", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("uro", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("vice", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("vid", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("xeno", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("xero", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("xylo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("y", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("yocto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("yotta", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("zepto", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("zetta", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("zo", MatchManager.LESS_GENERAL_THAN);
        prefixes.put("zoo", MatchManager.LESS_GENERAL_THAN);

        //"roots"
        prefixes.put("farm", MatchManager.LESS_GENERAL_THAN);

        //"roots"
        //e.g. parrot-fish is less general than fish
        //or it is a fish? :)
        //to handle cases like almond-tree, apple-tree
        suffixes.put("fish", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("fish's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("fishes", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("way", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("ways", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bird", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bird's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("birds", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("room", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("rooms", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("grass", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("grasses", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("boat", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("boats", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bush", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bushes", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bone", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bones", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("band", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bands", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("cake", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("cakes", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("shop", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("shops", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("mill", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("mills", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("paper", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("papers", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("worship", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("snake", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("snake's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("snakes", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("road", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("roads", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("hound", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("hound's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("hounds", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("care", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("cares", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("virus", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("virus'", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("viruses", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("storm", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("storms", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("sail", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("sail's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("sails", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("boot", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("boots", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bee", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bee's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("bees", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("ache", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("aches", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("wear", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("wears", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("tit", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("tits", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("tax", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("tree", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("trees", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("taxes", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("spoon", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("spoons", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("song", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("songs", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("builder", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("builder's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("builders", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("vine", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("vines", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("saddle", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("saddles", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("plant", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("plants", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("knife", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("knives", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("frog", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("frog's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("frogs", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("chop", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("chops", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("writer", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("writer's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("writers", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("wright", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("wrights", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("person", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("person's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("persons", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("owner", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("owner's", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("owners", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("mint", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("rack", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("racks", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("name", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("names", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("mast", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("in-law", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("fruit", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("fruits", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("pox", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("poxes", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("hide", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("force", MatchManager.LESS_GENERAL_THAN);
        suffixes.put("forces", MatchManager.LESS_GENERAL_THAN);
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
                } else if (str1.endsWith(str2)) {
                    rel = matchSuffix(str1, str2);
                } else if (str2.endsWith(str1)) {
                    rel = match(str2, str1);
                    rel = reverseRelation(rel);
                } else {
                    rel = MatchManager.IDK_RELATION;
                }
            } else {
                rel = MatchManager.IDK_RELATION;
            }//if ((str1.length() > 3) && (str2.length() > 3)) {
        }//null

        if (rel != MatchManager.IDK_RELATION) {
            relCount++;
            addCase(str1, str2, rel);
        }
        return rel;
    }

    /**
     * Computes the relation with suffix matcher.
     *
     * @param str1 the source input
     * @param str2 the target input
     * @return synonym, more general, less general or IDK relation
     */
    private char matchSuffix(String str1, String str2) {
        //here always str1.endsWith(str2)
        char rel = MatchManager.IDK_RELATION;
        int spacePos1 = str1.lastIndexOf(' ');
        String prefix = str1.substring(0, str1.length() - str2.length());
        if (-1 < spacePos1 && !prefixes.containsKey(prefix)) {//check prefixes - ordered set!unordered set // TODO the matcher is suffix not prefix. need revision
            if (str1.length() == spacePos1 + str2.length() + 1) {//adhesive tape<tape   attention deficit disorder<disorder
                rel = MatchManager.LESS_GENERAL_THAN;
            } else {//connective tissue<issue
                String left = str1.substring(spacePos1 + 1, str1.length());
                char secondRel = match(left, str2);
                if (MatchManager.MORE_GENERAL_THAN == secondRel ||
                        MatchManager.SYNOMYM == secondRel) {
                    rel = MatchManager.LESS_GENERAL_THAN;
                } else { //?,<,!
                    rel = secondRel;
                }
            }
        } else {
            if (prefix.startsWith("-")) {
                prefix = prefix.substring(1);
            }
            if (prefix.endsWith("-") && !prefixes.containsKey(prefix = prefix.substring(0, prefix.length() - 1))) {
                //prefix = prefix.substring(0, prefix.length() - 1);
                //smth like cajun-creole, parrot-fish
                //but anti-virus
                rel = MatchManager.LESS_GENERAL_THAN;
            } else {
                if (prefixes.containsKey(prefix)) {
                    rel = prefixes.get(prefix);
                } else if (suffixes.containsKey(str2)) {
                    rel = suffixes.get(str2);
                }
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
        System.out.println("GSuffix");
        System.out.println("GSuffix rel count = " + relCount);
        System.out.println("GSuffix invocation count = " + invocationCount);
        String[] arr = new String[hm.size()];
        hm.toArray(arr);
        Arrays.sort(arr);
        for (String entry : arr) {
            System.out.println(entry);
        }
        System.out.println("GSuffix rel count = " + hm.size());
        System.out.println("GSuffix");
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
