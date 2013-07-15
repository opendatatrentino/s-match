package it.unitn.disi.smatch.matchers.structure.tree.spsm.ted;

import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl.GraphVertexTuple;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.IDistanceConversion;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.WorstCaseDistanceConversion;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.alg.DijkstraShortestPath;
import org._3pq.jgrapht.graph.SimpleDirectedWeightedGraph;

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
    protected Comparator<INode> comparator;

    /**
     * Contains the first tree
     */
    private INode tree1;

    /**
     * The first trees preordered list of nodes
     */
    private List<INode> list1 = null;

    /**
     * This stores the depth for each element of tree1
     */
    private HashMap<INode, Integer> depth1 = new HashMap<INode, Integer>();

    /**
     * Contains the second tree
     */
    private INode tree2;

    /**
     * The second trees preordered list of nodes
     */
    private List<INode> list2 = null;

    /**
     * This stores the depth for each element of tree2
     */
    private HashMap<INode, Integer> depth2 = new HashMap<INode, Integer>();

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

    private IDistanceConversion conversion;

    /**
     * Constructor.
     * <p/>
     * Pass two trees and expect the edit distance in
     * {@link #getTreeEditDistance()} after calling {@link #calculate()}
     * <p/>
     * Use the given comparator for compares.
     * </p>
     *
     * @param tree1 tree1
     * @param tree2 tree2
     * @param comparator comparator
     * @param conversion conversion
     * @throws NullPointerException if tree1 or tree2 are null
     */
    public TreeEditDistance(IContext tree1,
                            IContext tree2,
                            Comparator<INode> comparator,
                            IDistanceConversion conversion) throws NullPointerException {
        this(tree1, tree2, comparator, conversion,
                DEFAULT_PATH_LENGTH_LIMIT, DEFAULT_WEIGHT_INSERT,
                DEFAULT_WEIGHT_DELETE, DEFAULT_WEIGHT_SUBSTITUE);
    }


    /**
     * Constructor.
     * <p/>
     * Pass two trees and expect the edit distance in
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
     * @param tree1 tree1
     * @param tree2 tree2
     * @param comparator comparator
     * @param conversion conversion
     * @param pathLengthLimit path length limit
     * @param weightInsert weight of insert operation
     * @param weightDelete weight of delete operation
     * @param weightSubstitute weight of subs operation
     * @throws NullPointerException if tree1 or tree2 are null
     */
    public TreeEditDistance(IContext tree1,
                            IContext tree2,
                            Comparator<INode> comparator,
                            IDistanceConversion conversion, double pathLengthLimit,
                            double weightInsert, double weightDelete, double weightSubstitute)
            throws NullPointerException {
        this(tree1, tree2, comparator, conversion,
                pathLengthLimit, weightInsert, weightDelete, weightSubstitute,
                0);
    }

    /**
     * Constructor.
     * <p/>
     * Pass two trees and expect the edit distance in
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
     * @param tree1 tree1
     * @param tree2 tree2
     * @param comparator comparator
     * @param conversion conversion
     * @param pathLengthLimit path length limit
     * @param weightInsert weight of insert operation
     * @param weightDelete weight of delete operation
     * @param weightSubstitute weight of subs operation
     * @param weightSubstituteEqual weight of subs operation
     * @throws NullPointerException if tree1 or tree2 are null
     */
    public TreeEditDistance(IContext tree1,
                            IContext tree2,
                            Comparator<INode> comparator,
                            IDistanceConversion conversion, double pathLengthLimit,
                            double weightInsert, double weightDelete, double weightSubstitute,
                            double weightSubstituteEqual) throws NullPointerException {

        this.conversion = conversion;

        if (tree1 == null || tree2 == null || tree1.getRoot() == null || tree2.getRoot() == null) {
            throw new NullPointerException("Invalid trees passed!");
        }

        this.tree1 = tree1.getRoot();
        this.tree2 = tree2.getRoot();

        list1 = preorder(this.tree1);
        list2 = preorder(this.tree2);

        this.comparator = comparator;
        this.pathLengthLimit = pathLengthLimit;
        this.weightInsert = weightInsert;
        this.weightDelete = weightDelete;
        this.weightSubstitute = weightSubstitute;
        this.weightSubstituteEqual = weightSubstituteEqual;
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

                similarity = conversion.convert(getTreeEditDistance());

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
        HashMap<INode, Integer> orderNum1 = new HashMap<INode, Integer>();
        HashMap<INode, Integer> orderNum2 = new HashMap<INode, Integer>();

        // calculate preorder numeration and depth information for each node
        preorderTreeDepth(tree1, orderNum1, depth1);
        preorderTreeDepth(tree2, orderNum2, depth2);

        // put all depth information into array; ordering is by preorder
        int[] d1 = new int[list1size + 1];
        int[] d2 = new int[list2size + 1];
        for (INode a : list1) {
            d1[orderNum1.get(a)] = depth1.get(a);
        }
        for (INode a : list2) {
            d2[orderNum2.get(a)] = depth2.get(a);
        }

        // clear graph
        editDistanceGraph = new SimpleDirectedWeightedGraph();

        // create vertexes for all tree1/tree2 crossings
        GraphVertexTuple[][] vertexArray = new GraphVertexTuple[list1size + 1][list2size + 1];
        for (int i = 0; i <= list1size; i++) {
            for (int j = 0; j <= list2size; j++) {
                GraphVertexTuple t = new GraphVertexTuple(i, j);
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

        return true;
    }

    double getNodeWeight(INode node) {
        return 1;
    }

    /**
     * Implementing method "preorder_tree_depth" by Gabriel Valiente: See
     * http://www.lsi.upc.es/~valiente/algorithm/combin.cpp
     *
     * @param root root
     * @param order order
     * @param depth depth
     */
    private void preorderTreeDepth(INode root, HashMap<INode, Integer> order, HashMap<INode, Integer> depth) {
        order.clear();
        depth.clear();

        int num = 1;

        Deque<INode> stack = new ArrayDeque<INode>();
        stack.push(root);
        while (!stack.isEmpty()) {
            INode v = stack.pop();

            order.put(v, num++);
            if (!v.hasParent()) {
                depth.put(v, 0);
            } else {
                depth.put(v, depth.get(v.getParent()) + 1);
            }

            for (int i = v.getChildCount() - 1; i >= 0; i--) {
                stack.push(v.getChildAt(i));
            }
        }
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

        for (Object o : edges) {
            Edge edge = (Edge) o;
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

            // in this loop gather all available edges outgoing from a vertex
            // and
            // assign them to the corresponding variable '...Edge'.
            for (Object o : adjacentEdges) {
                Edge edge = (Edge) o;
                GraphVertexTuple oppositeVertex = (GraphVertexTuple) edge.oppositeVertex(vertex);
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
     * Preorders the subtree rooted at the given node.
     *
     * @param root root node
     * @return the converted list, empty if no elements in input
     */
    private static List<INode> preorder(INode root) {
        ArrayList<INode> result = new ArrayList<INode>();
        Deque<INode> stack = new ArrayDeque<INode>();
        stack.push(root);
        while (!stack.isEmpty()) {
            INode c = stack.pop();

            result.add(c);

            for (int i = c.getChildCount() - 1; i >= 0; i--) {
                stack.push(c.getChildAt(i));
            }
        }

        return result;
    }
}