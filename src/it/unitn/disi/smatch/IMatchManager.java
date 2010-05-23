package it.unitn.disi.smatch;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;

/**
 * Interface for matching related functionalities.<br>
 * The following code can be used in order to obtain an instance of IMatchManager interface.<br>
 * IMatchManager mm=it.unitn.disi.smatch.MatchManager.getInstance();
 * <p/>
 * An S-Match.properties file contain an example configuration with documented properties names and values.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatchManager extends IConfigurable {

    /**
     * Creates a context instance.
     *
     * @return a context instance
     */
    public IContext createContext();

    /**
     * Uses current loader to load the context from external source into internal data structure.
     *
     * @param fileName a string passed to the current loader
     * @return interface to internal context representation
     * @throws SMatchException SMatchException
     */
    IContext loadContext(String fileName) throws SMatchException;

    /**
     * Renders the context using a current renderer.
     *
     * @param ctxSource context to be rendered
     * @param fileName  a render destination passed to the context renderer
     * @throws SMatchException SMatchException
     */
    void renderContext(IContext ctxSource, String fileName) throws SMatchException;

    /**
     * Loads the mapping between source and target contexts using the current mapping loader.
     *
     * @param ctxSource source context
     * @param ctxTarget target context
     * @param inputFile a mapping location passed to the mapping loader
     * @return a mapping
     * @throws SMatchException SMatchException
     */
    IContextMapping<INode> loadMapping(IContext ctxSource, IContext ctxTarget, String inputFile) throws SMatchException;

    /**
     * Renders the mapping using a current mapping renderer.
     *
     * @param mapping    a mapping
     * @param outputFile a render destination passed to the mapping renderer
     * @throws SMatchException SMatchException
     */
    void renderMapping(IContextMapping<INode> mapping, String outputFile) throws SMatchException;

    /**
     * Filters a mapping. For example, filtering could be a minimization.
     *
     * @param mapping a mapping to filter
     * @return a filtered mapping
     * @throws SMatchException SMatchException
     */
    IContextMapping<INode> filterMapping(IContextMapping<INode> mapping) throws SMatchException;

    /**
     * Performs the first step of the semantic matching algorithm.
     *
     * @param context interface to a context to be preprocessed
     * @throws SMatchException SMatchException
     */
    void preprocess(IContext context) throws SMatchException;

    /**
     * Performs the second step of the semantic matching algorithm.
     *
     * @param context interface to the preprocessed context
     * @throws SMatchException SMatchException
     */
    void classify(IContext context) throws SMatchException;

    /**
     * Performs the third step of semantic matching algorithm.
     *
     * @param sourceContext interface of source context with concept at node formula
     * @param targetContext interface of target context with concept at node formula
     * @return interface to a matrix of semantic relations between atomic concepts of labels in the contexts
     * @throws SMatchException SMatchException
     */
    IContextMapping<IAtomicConceptOfLabel> elementLevelMatching(IContext sourceContext, IContext targetContext) throws SMatchException;

    /**
     * Performs the fourth step of semantic matching algorithm.
     *
     * @param sourceContext interface of source context with concept at node formula
     * @param targetContext interface of target context with concept at node formula
     * @param acolMapping   mapping between atomic concepts of labels in the contexts
     * @return mapping between the concepts at nodes in the contexts
     * @throws SMatchException SMatchException
     */
    IContextMapping<INode> structureLevelMatching(IContext sourceContext, IContext targetContext,
                                                  IContextMapping<IAtomicConceptOfLabel> acolMapping) throws SMatchException;

    /**
     * Performs the first two steps of the semantic matching algorithm.
     *
     * @param context interface to context to be preprocessed
     * @throws SMatchException SMatchException
     */
    void offline(IContext context) throws SMatchException;

    /**
     * Performs the last two steps of the semantic matching algorithm.
     *
     * @param sourceContext interface to preprocessed source context to be matched
     * @param targetContext interface to preprocessed target context to be matched
     * @return interface to resulting mapping
     * @throws SMatchException SMatchException
     */
    IContextMapping<INode> online(IContext sourceContext, IContext targetContext) throws SMatchException;

    /**
     * Performs the whole matching process.
     *
     * @param sourceContext interface to source context to be matched
     * @param targetContext interface to target context to be matched
     * @return interface to resulting mapping
     * @throws SMatchException SMatchException
     */
    IContextMapping<INode> match(IContext sourceContext, IContext targetContext) throws SMatchException;
}