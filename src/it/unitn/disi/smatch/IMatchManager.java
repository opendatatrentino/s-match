package it.unitn.disi.smatch;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.mappings.IMapping;

import java.util.Properties;
import java.util.Vector;

/**
 * Interface for matching related functionalities.<br>
 * The following code can be used in order to obtain an instance of IMatchManager interface.<br>
 * IMatchManager mm=it.unitn.disi.smatch.MatchManager.getInstance();
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatchManager {

    /**
     * Set up the matching properties.
     * A list of parameters which influence on the matching process work flow.<br>
     * <br>
     * Name of the class which implements IWordNetMatcher interface.<br>
     * WNmatcher = it.unitn.disi.smatch.oracles.wordnet.InMemoryWordNet<br>
     * <br>
     * Name of the class which implements ILinguisticOracle interface.<br>
     * LinguisticOracle = it.unitn.disi.smatch.oracles.wordnet.WordNet<br>
     * <br>
     * Name of the class which implements IClassifier interface.<br>
     * Classifier = it.unitn.disi.smatch.classifiers.DefaultClassifier<br>
     * <br>
     * Name of the class which implements IPreprocessor interface.<br>
     * Preprocessor = it.unitn.disi.smatch.preprocessors.DefaultPreprocessor<br>
     * <br>
     * Name of the class which implements IMatcherLibrary interface.<br>
     * MatcherLibrary = it.unitn.disi.smatch.matchers.element.MatcherLibrary<br>
     * <br>
     * Name of the class which implements ILoader interface.<br>
     * Loader = it.unitn.disi.smatch.loaders.CTXMLLoader<br>
     * <br>
     * Name of the class which implements IMappingRenderer interface.<br>
     * MappingRenderer = it.unitn.disi.smatch.renderers.mapping.DefaultHTMLMappingRenderer<br>
     * <br>
     * Name of the class which implements IContextRenderer interface.<br>
     * ContextRenderer = it.unitn.disi.smatch.renderers.context.CTXMLContextRenderer<br>
     * <br>
     * Name of the class which implements IFilter interface.<br>
     * Filter = it.unitn.disi.smatch.filters.DefaultFilter<br>
     * <br>
     *
     * @param properites the object with the properties of specific properties file
     */
    void setProperties(Properties properites);

    /**
     * Performs the first step of the semantic matching algorithm.
     *
     * @param ctxSource interface to context to be preprocessed
     * @return interface to preprocessed context
     */
    IContext preprocess(IContext ctxSource);

    /**
     * Performs the second step of the semantic matching algorithm.
     *
     * @param ctxSource interface to preprocessed context without concept at node formulas
     * @return interface to preprocessed with concept at node formulas
     */
    IContext classify(IContext ctxSource);

    /**
     * Renders context to screen,file or database.
     *
     * @param ctxSource context to be rendered
     * @param fileName  string which defines the output device
     */
    void renderContext(IContext ctxSource, String fileName);

    /**
     * Performs the third step of semantic matching algorithm.
     *
     * @param sourceContext interface of source context with concept at node formula
     * @param targetContext interface of target context with concept at node formula
     * @return interface to matrix of semantic relations between atomic concepts of labels in the contexts
     * @throws SMatchException
     */
    IMatchMatrix elementLevelMatching(IContext sourceContext, IContext targetContext) throws SMatchException;

    /**
     * Performs the fourth step of semantic matching algorithm.
     *
     * @param sourceContext interface of source context with concept at node formula
     * @param targetContext interface of target context with concept at node formula
     * @param ClabMatrix    interface to matrix of semantic relations between atomic concepts of labels in the contexts
     * @return interface to matrix of semantic relations between concepts at nodes in the contexts
     * @throws SMatchException
     */
    IMatchMatrix structureLevelMatching(IContext sourceContext, IContext targetContext,
                                        IMatchMatrix ClabMatrix) throws SMatchException;

    /**
     * Performs additional filtering step which executes after 4th step of semantic matching algorithm.
     *
     * @param args parameters to the filtering process (as in DefaultFilter implementation)<br>
     *             0 element fileName name of file for intermediate filtering results<br>
     *             1 element IMatchMatrix CnodMatrix matrix of semantic relations between concepts at nodes<br>
     *             2 element IMatchMatrix ClabMatrix matrix of semantic relations between concepts of labels<br>
     *             3 element IContext sourceContext interface of source context with concept at node formula<br>
     *             4 element IContext targetContext interface of target context with concept at node formula<br>
     *             all the other elements are reserved for future use<br>
     * @return interface to filtered matrix of semantic relations between concepts at nodes (CnodMatrix)
     */
    IMatchMatrix filter(Vector args);

    /**
     * Renders mappings into appropriate format into file.
     *
     * @param args parameters to the rendering process (as in DefaultHTMLMappingRenderer implementation)<br>
     *             0 element fileName name of file for intermediate filtering results<br>
     *             1 element IMatchMatrix CnodMatrix matrix of semantic relations between concepts at nodes<br>
     *             2 element IMatchMatrix ClabMatrix matrix of semantic relations between concepts of labels<br>
     *             3 element IContext sourceContext interface of source context with concept at node formula<br>
     *             4 element IContext targetContext interface of target context with concept at node formula<br>
     */
    IMapping renderMapping(Vector args);

    /**
     * Performs the first two steps of the semantic matching algorithm and renders the results
     * by current IContextRenderer.
     *
     * @param ctxSource      interface to context to be preprocessed
     * @param ctxsSourceFile a string passed to IContextRenderer for implementation of rendering
     * @return interface to preprocessed context
     */
    IContext offline(IContext ctxSource, String ctxsSourceFile);

    /**
     * Performs the last two steps of the semantic matching algorithm, filtering
     * and render the results by current IMappingRender.
     *
     * @param sourceContext interface to preprocessed source context to be matched
     * @param targetContext interface to preprocessed target context to be matched
     * @return interface to resulting mapping
     * @throws SMatchException
     */
    IMapping online(IContext sourceContext, IContext targetContext) throws SMatchException;

    /**
     * Performs the whole matching process.
     *
     * @param sourceContext interface to source context to be matched
     * @param targetContext interface to target context to be matched
     * @return interface to resulting mapping
     * @throws SMatchException
     */
    IMapping match(IContext sourceContext, IContext targetContext) throws SMatchException;

    /**
     * Uses current Loader in order to load the context from external source into
     * internal data structure.
     *
     * @param fileName a string passed to current Loader
     * @return interface to internal context representation
     * @throws SMatchException
     */
    IContext loadContext(String fileName) throws SMatchException;
}
