
package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted;

import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.alg.DijkstraShortestPath;
import org._3pq.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeAccessor;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeNode;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl.GraphVertexTuple;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl.TreeNodeTuple;
//import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.GraphRenderer;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.IDistanceConversion;
//import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.ConsoleGraphRenderer;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException;
//import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.VisualJGraphRenderer;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.WorstCaseDistanceConversion;

import java.util.*;

/**
 * This implements an edit distance calculation for trees. The trees are rooted
 * and ordered. The algorithm is taken from Gabriel Valientes book "Algorithms
 * on trees and graphs" (Springer) and described in chapter 2.1 "The tree edit
 * distance problem".
 */
public class TreeEditDistance {

    /**
     * Default weight of delete operation
     */
    public static double DEFAULT_WEIGHT_DELETE = 1d;

    /**
     * Default weight of insert operation
     */
    public static double DEFAULT_WEIGHT_INSERT = 1d;

    /**
     * Default weight of substitute operation for non-equal objects. Equality is
     * determined by comparator.
     */
    public static double DEFAULT_WEIGHT_SUBSTITUE = 0.5d;

    /**
     * Default weight of substitute operation for equal objects. Equality is
     * determined by comparator.
     */
    public static double DEFAULT_WEIGHT_SUBSTITUE_EQUAL = 0d;

    /**
     * Default path length limit is infinity
     */
    public static double DEFAULT_PATH_LENGTH_LIMIT = Double.POSITIVE_INFINITY;

    /**
     * This will contain the used tree comparator
     */
    protected Comparator<ITreeNode> comparator;

    /**
     * Contains the first tree
     */
    private ITreeNode tree1;

    /**
     * The first trees preordered list of nodes
     */
    private List<ITreeNode> list1 = null;

    /**
     * This stores the depth for each element of tree1
     */
    private HashMap<ITreeNode, Integer> depth1 = new HashMap<ITreeNode, Integer>();

    /**
     * Contains the second tree
     */
    private ITreeNode tree2;

    /**
     * The second trees preordered list of nodes
     */
    private List<ITreeNode> list2 = null;

    /**
     * This stores the depth for each element of tree2
     */
    private HashMap<ITreeNode, Integer> depth2 = new HashMap<ITreeNode, Integer>();

    /**
     * Default path length limit is infinity.
     */
    private double pathLengthLimit = DEFAULT_PATH_LENGTH_LIMIT;

    /**
     * Contains the edit graph
     */
    private SimpleDirectedWeightedGraph editDistanceGraph;

    /**
     * Saves the first vertex of the editDistanceGraph
     */
    private GraphVertexTuple firstVertex;

    /**
     * Saves the last vertex of the editDistanceGraph
     */
    private GraphVertexTuple lastVertex;

    /**
     * Default weight of delete operation
     */
    private double weightDelete = DEFAULT_WEIGHT_DELETE;

    /**
     * Default weight of insert operation
     */
    private double weightInsert = DEFAULT_WEIGHT_INSERT;

    /**
     * Default weight of substitute operation for non-equal objects. Equality is
     * determined by comparator.
     */
    private double weightSubstitute = DEFAULT_WEIGHT_SUBSTITUE;

    /**
     * Default weight of substitute operation for equal objects. Equality is
     * determined by comparator.
     */
    private double weightSubstituteEqual = DEFAULT_WEIGHT_SUBSTITUE_EQUAL;

    /**
     * This is the calculated shortest path
     */
    private DijkstraShortestPath shortestPath;

    /**
     *
     */
    private IDistanceConversion conversion;

//    /*
//     * 
//     */
//    private GraphRenderer graphRenderer;
    
    /**
     * Constructor.
     * <p/>
     * Pass two <code>org.openk.core.module.matcher.tree_matcher.data.ITreeAccessor</code> and expect the edit distance in
     * {@link #getTreeEditDistance()} after calling {@link #calculate()}
     * <p/>
     * Use the given comparator for compares.
     * </p>
     *
     * @param treeAccessor1
     * @param treeAccessor2
     * @param comparator
     * @param conversion
     * @throws it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException
     *                              when a tree contains an invalid structure or childs
     * @throws NullPointerException if tree1 or tree2 are null
     */
    public TreeEditDistance(ITreeAccessor treeAccessor1,
                            ITreeAccessor treeAccessor2,
                            Comparator<ITreeNode> comparator,
                            IDistanceConversion conversion) throws NullPointerException,
            InvalidElementException {
        this(treeAccessor1, treeAccessor2, comparator, conversion,
                DEFAULT_PATH_LENGTH_LIMIT, DEFAULT_WEIGHT_INSERT,
                DEFAULT_WEIGHT_DELETE, DEFAULT_WEIGHT_SUBSTITUE);
    }


    /**
     * Constructor.
     * <p/>
     * Pass two <code>org.openk.core.module.matcher.tree_matcher.data.ITreeAccessor</code> and expect the edit distance in
     * {@link #getTreeEditDistance()} after calling {@link #calculate()}
     * <p/>
     * You can limit the search for a path by length when passing the
     * pathLengthLimit argument. Manipulate the weigth assigned with transitions
     * (edges) with the other parameters.
     * </p>
     * <p/>
     * Use the given comparator as comparator
     * </p>
     * <p/>
     * Any of pathLengthLimit and the weigth arguments can be null upon which
     * default values will be used.
     * </p>
     *
     * @param treeAccessor1
     * @param treeAccessor2
     * @param comparator
     * @param conversion
     * @param pathLengthLimit
     * @param weigthInsert
     * @param weigthDelete
     * @param weigthSubstitute
     * @throws it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException
     *                              when a tree contains an invalid structure or childs
     * @throws NullPointerException if tree1 or tree2 are null
     */
    public TreeEditDistance(ITreeAccessor treeAccessor1,
                            ITreeAccessor treeAccessor2,
                            Comparator<ITreeNode> comparator,
                            IDistanceConversion conversion, double pathLengthLimit,
                            double weigthInsert, double weigthDelete, double weigthSubstitute)
            throws NullPointerException, InvalidElementException {
        this(treeAccessor1, treeAccessor2, comparator, conversion,
                pathLengthLimit, weigthInsert, weigthDelete, weigthSubstitute,
                0);
    }

    /**
     * Constructor.
     * <p/>
     * Pass two <code>org.openk.core.module.matcher.tree_matcher.data.ITreeAccessor</code> and expect the edit distance in
     * {@link #getTreeEditDistance()} after calling {@link #calculate()}
     * <p/>
     * You can limit the search for a path by length when passing the
     * pathLengthLimit argument. Manipulate the weigth assigned with transitions
     * (edges) with the other parameters.
     * </p>
     * <p/>
     * Use the given comparator as comparator
     * </p>
     * <p/>
     * Any of pathLengthLimit and the weigth arguments can be null upon which
     * default values will be used.
     * </p>
     *
     * @param treeAccessor1
     * @param treeAccessor2
     * @param comparator
     * @param conversion
     * @param pathLengthLimit
     * @param weigthInsert
     * @param weigthDelete
     * @param weigthSubstitute
     * @param weigthSubstituteEqual
     * @throws InvalidElementException when a tree contains an invalid structure or childs
     * @throws NullPointerException    if tree1 or tree2 are null
     */
    public TreeEditDistance(ITreeAccessor treeAccessor1,
                            ITreeAccessor treeAccessor2,
                            Comparator<ITreeNode> comparator,
                            IDistanceConversion conversion, double pathLengthLimit,
                            double weigthInsert, double weigthDelete, double weigthSubstitute,
                            double weigthSubstituteEqual) throws NullPointerException,
            InvalidElementException {

        this.conversion = conversion;

        if (treeAccessor1 == null || treeAccessor2 == null
                || treeAccessor1.getRoot() == null
                || treeAccessor2.getRoot() == null)
            throw new NullPointerException("Invalid accessors passed!");

        this.tree1 = treeAccessor1.getRoot();
        this.tree2 = treeAccessor2.getRoot();

        list1 = enumerationToList(tree1.preorderEnumeration());
        list2 = enumerationToList(tree2.preorderEnumeration());

        this.comparator = comparator;
        this.pathLengthLimit = pathLengthLimit;
        this.weightInsert = weigthInsert;
        this.weightDelete = weigthDelete;
        this.weightSubstitute = weigthSubstitute;
        this.weightSubstituteEqual = weigthSubstituteEqual;

        //TODO: code the hability to load dinamically the implementing class from a config file
        //graphRenderer = new VisualJGraphRenderer();//ConsoleGraphRenderer();//
    }

    public boolean calculate() {
        setCalculated(false);
        if (calculateGraph()) {
            shortestPath = new DijkstraShortestPath(editDistanceGraph,
                    firstVertex, lastVertex, pathLengthLimit);
            if (shortestPath != null) {
                setCalculated(true);

                if (conversion instanceof WorstCaseDistanceConversion) {
                    ((WorstCaseDistanceConversion) conversion)
                            .setWorstCaseDistance(getWorstCaseSumOfNodes());
                }

                similarity = new Double(conversion
                        .convert(getTreeEditDistance()));

                return true;
            }
        }
        return false;
    }

    /**
     * This method is a modified version of a "tree_edit_graph(tree&, tree&, GRAPH<string,string>&)"
     * from http://www.lsi.upc.es/~valiente/algorithm/combin.cpp. Modifications allowed to take into account 
     * various (other than equivalence) semantic relations holding among tree nodes
     *
     * @return true upon success, false otherwise
     */
    private boolean calculateGraph() {
        // cache size of lists
        int list1size = list1.size();
        int list2size = list2.size();

        // this stores the order number for each Node
        HashMap<ITreeNode, Integer> orderNum1 = new HashMap<ITreeNode, Integer>();
        HashMap<ITreeNode, Integer> orderNum2 = new HashMap<ITreeNode, Integer>();

        // calculate preorder numeration and depth information for each node
        preorderTreeDepth(tree1, orderNum1, depth1);
        preorderTreeDepth(tree2, orderNum2, depth2);

        // put all depth information into array; ordering is by preorder
        int[] d1 = new int[list1size + 1];
        int[] d2 = new int[list2size + 1];
        Iterator<ITreeNode> iter = list1.listIterator();
        while (iter.hasNext()) {
            ITreeNode a = iter.next();
            d1[orderNum1.get(a)] = depth1.get(a);
        }
        iter = list2.listIterator();
        while (iter.hasNext()) {
            ITreeNode a = iter.next();
            d2[orderNum2.get(a)] = depth2.get(a);
        }

        // clear graph
        editDistanceGraph = new SimpleDirectedWeightedGraph();

        // create vertexes for all tree1/tree2 crossings
        GraphVertexTuple[][] vertexArray = new GraphVertexTuple[list1size + 1][list2size + 1];
        for (int i = 0; i <= list1size; i++) {
            for (int j = 0; j <= list2size; j++) {
                GraphVertexTuple t = new GraphVertexTuple(new Integer(i),
                        new Integer(j));
                if (i > 0 && j > 0)
                    t.setTreeNodeTuple(new TreeNodeTuple(list1.get(i - 1),
                            list2.get(j - 1)));
                vertexArray[i][j] = t;
                if (!editDistanceGraph.addVertex(t))
                    return false;
            }
        }

        // save eckpunkte
        firstVertex = vertexArray[0][0];
        lastVertex = vertexArray[list1size][list2size];

        // delete edges at outer right
        for (int i = 0; i < list1size; i++) {
            Edge e = editDistanceGraph.addEdge(vertexArray[i][list2size],
                    vertexArray[i + 1][list2size]);
            if (e == null)
                return false;
            e.setWeight(weightDelete);
        }
        // insert edges at bottom
        for (int j = 0; j < list2size; j++) {
            Edge e = editDistanceGraph.addEdge(vertexArray[list1size][j],
                    vertexArray[list1size][j + 1]);
            if (e == null)
                return false;
            e.setWeight(weightInsert);
        }
        for (int i = 0; i < list1size; i++) {
            double sourceNodeWeight = getNodeWeight(list1.get(i));
            for (int j = 0; j < list2size; j++) {
//double targetNodeWeight=getNodeWeight(list2.get(j));

                if (d1[i + 1] >= d2[j + 1]) {
                    Edge e = editDistanceGraph.addEdge(vertexArray[i][j],
                            vertexArray[i + 1][j]);
                    if (e == null)
                        return false;

                    e.setWeight(sourceNodeWeight * weightDelete);
                }
                if (d1[i + 1] == d2[j + 1]) {
                    Edge e = editDistanceGraph.addEdge(vertexArray[i][j],
                            vertexArray[i + 1][j + 1]);
                    if (e == null)
                        return false;
//					if (comparator.compare(list1.get(i), list2.get(j)) == 0) {
//						e.setWeight(weightSubstituteEqual);
//					} else {
//						e.setWeight(weightSubstitute);
//					}
                    if ((comparator.compare(list1.get(i), list2.get(j)) == 1) ||
                            (comparator.compare(list1.get(i), list2.get(j)) == 2)) {
                        e.setWeight(weightSubstitute);
                    }
                    if (comparator.compare(list1.get(i), list2.get(j)) == 0) {
                        e.setWeight(weightSubstituteEqual);
                    }
                    if (comparator.compare(list1.get(i), list2.get(j)) == 3) {
                        e.setWeight(Double.POSITIVE_INFINITY);
                    }
                    if (comparator.compare(list1.get(i), list2.get(j)) == -1) {
                        e.setWeight(sourceNodeWeight * weightSubstitute * 2);
                    }

                }
                if (d1[i + 1] <= d2[j + 1]) {
                    Edge e = editDistanceGraph.addEdge(vertexArray[i][j],
                            vertexArray[i][j + 1]);
                    if (e == null)
                        return false;
                    e.setWeight(weightInsert);
                }
            }
        }

//        if (graphRenderer != null){
//        	graphRenderer.render(editDistanceGraph, list1, list2);
//        }
        return true;
    }

    double getNodeWeight(ITreeNode node) {
//        Object o = node.getUserObject();
//        INode n=(INode) o;
//        Vector<IAtomicConceptOfLabel> v=n.getNodeData().getACoLs();
//        double max=0;
//        for (int i = 0; i < v.size(); i++) {
//            IAtomicConceptOfLabel acol = v.elementAt(i);
//            double weight=acol.getWeight();
//            max=Math.max(max,weight);
//        }
        return 1;
//        return max;
    }

    /**
     * Implementing method "preorder_tree_depth" by Gabriel Valiente: See
     * http://www.lsi.upc.es/~valiente/algorithm/combin.cpp
     *
     * @param tree
     * @param order
     * @param depth
     */
    private void preorderTreeDepth(ITreeNode tree,
                                   HashMap<ITreeNode, Integer> order, HashMap<ITreeNode, Integer> depth) {
        order.clear();
        depth.clear();

        Stack<ITreeNode> stack = new Stack<ITreeNode>();
        stack.push((ITreeNode) tree.getRoot());
        int num = 1;
        ITreeNode v, w;
        do {
            v = stack.pop();
            order.put(v, num++);
            if (v.isRoot())
                depth.put(v, 0);
            else
                depth.put(v, depth.get(v.getParent()) + 1);
            try {
                w = (ITreeNode) v.getLastChild();
            } catch (NoSuchElementException e) {
                continue;
            }
            while (w != null) {
                stack.push(w);
                w = w.getPreviousSibling();
            }

        } while (!stack.isEmpty());
    }

    /**
     * Checks if a valid path and therefore a valid edit distance has been
     * found.
     *
     * @return true if a path exists, false otherwise
     */
    public boolean hasValidEditDistance() {
        return isCalculated()
                && (getTreeEditDistance() != Double.POSITIVE_INFINITY)
                && (shortestPath.getPathEdgeList() != null);
    }

    /**
     * @return Returns the treeEditDistance.
     * @throws NullPointerException if instance is not calculated
     */
    public double getTreeEditDistance() throws NullPointerException {
        if (!isCalculated()) {
            throw new NullPointerException("Instance did not sucessfully calculate!");
        }
        return shortestPath.getPathLength();
    }

    /**
     * This worst-case is the sum of nodes in both trees.
     *
     * @return the worst-case scenario of edit operations
     */
    public double getWorstCaseSumOfNodes() {
        return list1.size() + list2.size();
    }

    /**
     * This worst-case is computed as follows: We look at the original graph
     * <code>editDistanceGraph</code>, and change the weights of all diagonal
     * edges to {@link #weightSubstitute}. Previously their weights depended on
     * whether the node-tuple is equal or not. But now we look at it as if all
     * the labels in both trees were different. Then we compute again the
     * shortest path through this altered graph. By considering the
     * shortestPath, we are still able to insert nodes prior to delete others.
     * This is not possible in: {@link #getWorstCaseRetainStructure()}.
     *
     * @return the worst-case scenario of edit operations
     */
    public double getWorstCaseSubstituteAll() {
        double worstCase = -1;

        // make a copy of editDistanceGraph
        SimpleDirectedWeightedGraph worstCaseGraph = new SimpleDirectedWeightedGraph();
        Set vertices = editDistanceGraph.vertexSet();
        Set edges = editDistanceGraph.edgeSet();
        worstCaseGraph.addAllVertices(vertices);
        worstCaseGraph.addAllEdges(edges);

        edges = worstCaseGraph.edgeSet();
        Iterator edgeIterator = edges.iterator();

        while (edgeIterator.hasNext()) {
            Edge edge = (Edge) edgeIterator.next();
            GraphVertexTuple vertex1 = (GraphVertexTuple) edge.getSource();
            GraphVertexTuple vertex2 = (GraphVertexTuple) edge.getTarget();
            // check if this edge is a diagonal
            if (vertex2.getLeft() == vertex1.getLeft() + 1
                    && vertex2.getRight() == vertex1.getRight() + 1) {
                edge.setWeight(weightSubstitute);
            }
        }
        DijkstraShortestPath shortestPath = new DijkstraShortestPath(worstCaseGraph, firstVertex, lastVertex, pathLengthLimit);
        worstCase = shortestPath.getPathLength();
        return worstCase;
    }

    /**
     * This worst-case is computed as follows: Replace all weights of the
     * diagonal edges with {@link #weightSubstitute} (in case that this edge had
     * received a weight of zero because of equal labels). Walk from firstVertex
     * (top left corner) to lastVertex (bottom right corner) with the following
     * priority: 1. diagonal edges, 2. vertical edges, 3. horizontal edges. In
     * different words: Basically we delete all nodes from the first tree and
     * add all nodes from the second tree. But the nodes that are at the same
     * location in both trees, we will consider as having different labels. That
     * means, we count as if we would substitute their labels. The main
     * difference of this method to {@link #getWorstCaseSubstituteAll()} is,
     * that here it is not possible to insert nodes before we have deleted all
     * wrong nodes.
     *
     * @return the worst-case scenario of edit operations
     */
    public double getWorstCaseRetainStructure() {
        double pathLength = 0;
        Edge verticalEdge;
        Edge horizontalEdge;
        Edge diagonalEdge;

        GraphVertexTuple vertex = firstVertex;

        while (vertex != lastVertex) {
            verticalEdge = null;
            horizontalEdge = null;
            diagonalEdge = null;
            List adjacentEdges = editDistanceGraph.outgoingEdgesOf(vertex);
            Iterator edgeIterator = adjacentEdges.iterator();

            // in this loop gather all available edges outgoing from a vertex
            // and
            // assign them to the corresponding variable '...Edge'.
            while (edgeIterator.hasNext()) {
                Edge edge = (Edge) edgeIterator.next();
                GraphVertexTuple oppositeVertex = (GraphVertexTuple) edge
                        .oppositeVertex(vertex);
                int left = vertex.getLeft();
                int right = vertex.getRight();
                int oppositeLeft = oppositeVertex.getLeft();
                int oppositeRight = oppositeVertex.getRight();
                // first check if edge is a diagonal
                if ((oppositeLeft == left + 1) && (oppositeRight == right + 1)) {
                    diagonalEdge = edge;
                    break;
                }
                // then check if this edge is a vertical (which means to delete
                // a node)
                else if ((oppositeLeft == left + 1) && (oppositeRight == right)) {
                    verticalEdge = edge;
                } else
                    horizontalEdge = edge; // it is a horizontal edge (which
                // means to add a node)
            }
            Edge edgeToWalk;
            double weight = 0;
            if (diagonalEdge != null) {
                edgeToWalk = diagonalEdge;
                weight = weightSubstitute;
            } else if (verticalEdge != null) {
                edgeToWalk = verticalEdge;
                weight = edgeToWalk.getWeight();
            } else {
                edgeToWalk = horizontalEdge;
                weight = edgeToWalk.getWeight();
            }

            pathLength += weight;
            vertex = (GraphVertexTuple) edgeToWalk.oppositeVertex(vertex);
        }
        return pathLength;
    }

    /**
     * @return Returns the weightDelete.
     */
    public double getWeightDelete() {
        return weightDelete;
    }

    /**
     * @param weightDelete The weightDelete to set.
     */
    public void setWeightDelete(double weightDelete) {
        this.weightDelete = weightDelete;
    }

    /**
     * @return Returns the weightInsert.
     */
    public double getWeightInsert() {
        return weightInsert;
    }

    /**
     * @param weightInsert The weightInsert to set.
     */
    public void setWeightInsert(double weightInsert) {
        this.weightInsert = weightInsert;
    }

    /**
     * @return Returns the weightSubstitute.
     */
    public double getWeightSubstitute() {
        return weightSubstitute;
    }

    /**
     * @param weightSubstitute The weightSubstitute to set.
     */
    public void setWeightSubstitute(double weightSubstitute) {
        this.weightSubstitute = weightSubstitute;
    }

    /**
     * @return Returns the shortestPath.
     */
    public DijkstraShortestPath getShortestPath() {
        return shortestPath;
    }

    /**
     * Return the tree node comparator that is used to compare nodes in the tree
     * for equality.
     *
     * @return tree node comparator
     */
    public Comparator getComparator() {
        return comparator;
    }

    /**
     * This contains the similarity value.
     */
    protected Double similarity = null;

    public final String getName() {
        return this.getClass().getName();
    }

    public final Double getSimilarity() {
        if (!isCalculated()) {
            similarity = null;
            // not yet calculated, do calculation
            if (calculate()) {
                // calculation successful
                setCalculated(true);
            }
        }
        return similarity;
    }

    /**
     * This is set to true if calculation happened and was successful.
     */
    private boolean calculated = false;

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;

    }

    /**
     * Converts a given Enumeration of DefaultMutableTreeNode elements into a
     * List of these elements.
     *
     * @param enumeration
     * @return the converted list, empty if no elements in input
     * @throws it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException
     *          when a tree contains an invalid structure or childs
     */
    public static List<ITreeNode> enumerationToList(Enumeration enumeration)
            throws InvalidElementException {
        LinkedList<ITreeNode> ret = new LinkedList<ITreeNode>();
        while (enumeration.hasMoreElements()) {
            Object o = enumeration.nextElement();
            if (o instanceof ITreeNode) {
                ret.add((ITreeNode) o);
            } else
                throw new InvalidElementException("Unexpected child type in Tree while converting enumeration.");
        }
        return ret;
    }


}
