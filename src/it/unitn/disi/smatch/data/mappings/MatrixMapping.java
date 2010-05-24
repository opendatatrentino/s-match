package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.matrices.IIndexedObject;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.IMatchMatrixFactory;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Manages a mapping using a matrix. Needs a configuration key matchMatrixFactory with a class implementing
 * {@link it.unitn.disi.smatch.data.matrices.IMatchMatrixFactory} to produce matrix instances.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatrixMapping<T extends IIndexedObject> extends AbstractSet<IMappingElement<T>> implements IContextMapping<T>, IMappingFactory {

    private static final Logger log = Logger.getLogger(MatrixMapping.class);

    private static final String MATCH_MATRIX_FACTORY_KEY = "matchMatrixFactory";
    protected IMatchMatrixFactory factory;

    protected Properties properties;
    protected IMatchMatrix matrix;
    protected IContext sourceContext;
    protected IContext targetContext;

    // for set size();
    private int elementCount;

    private T[] sources;
    private T[] targets;

    private volatile transient int modCount;

    private class MatrixMappingIterator implements Iterator<IMappingElement<T>> {

        private int expectedModCount;
        private int curRow;
        private int curCol;
        private IMappingElement<T> next;
        private IMappingElement<T> current;

        private MatrixMappingIterator() {
            this.expectedModCount = modCount;
            if (0 == size()) {
                next = null;
            } else {
                curRow = -1;
                curCol = matrix.getY() - 1;
                next = findNext();
            }
        }

        public boolean hasNext() {
            return null != next;
        }

        public IMappingElement<T> next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (null == next) {
                throw new NoSuchElementException();
            }

            current = next;
            next = findNext();
            return current;
        }

        public void remove() {
            if (null == current) {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            setRelation(current.getSource(), current.getTarget(), IMappingElement.IDK);
            expectedModCount = modCount;
            current = null;
        }

        private IMappingElement<T> findNext() {
            IMappingElement<T> result = null;
            char relation = IMappingElement.IDK;
            do {
                curCol++;
                if (matrix.getY() == curCol) {
                    curRow++;
                    curCol = 0;
                }
            } while (curRow < matrix.getX() && curCol < matrix.getY() && IMappingElement.IDK == (relation = matrix.get(curRow, curCol)));

            if (IMappingElement.IDK != relation) {
                result = new MappingElement<T>(sources[curRow], targets[curCol], relation);
            }
            return result;
        }
    }

    public MatrixMapping() {
        properties = new Properties();
    }

    public MatrixMapping(Properties properties) {
        this.properties = properties;
    }

    public MatrixMapping(IMatchMatrixFactory factory) {
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    public MatrixMapping(IMatchMatrixFactory factory, IContext sourceContext, IContext targetContext) {
        this.sourceContext = sourceContext;
        this.targetContext = targetContext;
        this.factory = factory;
        matrix = factory.getInstance();
        // counts and indexes them
        int rows = getRowCount(sourceContext);
        int cols = getColCount(targetContext);
        matrix.init(rows, cols);

        sources = (T[]) new IIndexedObject[rows];
        targets = (T[]) new IIndexedObject[cols];

        elementCount = 0;
        modCount = 0;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = !newProperties.equals(properties);
        if (result) {
            if (newProperties.containsKey(MATCH_MATRIX_FACTORY_KEY)) {
                factory = (IMatchMatrixFactory) Configurable.configureComponent(factory, properties, newProperties, "match matrix factory", MATCH_MATRIX_FACTORY_KEY, IMatchMatrixFactory.class);
            } else {
                final String errMessage = "Cannot find configuration key " + MATCH_MATRIX_FACTORY_KEY;
                log.error(errMessage);
                throw new ConfigurableException(errMessage);
            }

            properties.clear();
            properties.putAll(newProperties);
        }
        return result;
    }

    public boolean setProperties(String fileName) throws ConfigurableException {
        return setProperties(Configurable.loadProperties(fileName));
    }

    public char getRelation(T source, T target) {
        return matrix.get(source.getIndex(), target.getIndex());
    }

    public boolean setRelation(final T source, final T target, final char relation) {
        final boolean result =
                source == sources[source.getIndex()] &&
                target == targets[target.getIndex()] &&
                relation == matrix.get(source.getIndex(), target.getIndex());

        if (!result) {
            modCount++;
            matrix.set(source.getIndex(), target.getIndex(), relation);
            if (IMappingElement.IDK == relation) {
                elementCount--;
                sources[source.getIndex()] = null;
                targets[target.getIndex()] = null;
            } else {
                elementCount++;
                sources[source.getIndex()] = source;
                targets[target.getIndex()] = target;
            }
        }

        return !result;
    }

    public int size() {
        return elementCount;
    }

    public boolean isEmpty() {
        return 0 == elementCount;
    }

    public boolean contains(Object o) {
        boolean result = false;
        if (o instanceof IMappingElement) {
            final IMappingElement e = (IMappingElement) o;
            if (e.getSource() instanceof IIndexedObject) {
                @SuppressWarnings("unchecked")
                final T s = (T) e.getSource();
                if (e.getTarget() instanceof IIndexedObject) {
                    @SuppressWarnings("unchecked")
                    final T t = (T) e.getTarget();
                    result = IMappingElement.IDK != getRelation(s, t) && s == sources[s.getIndex()] && t == targets[t.getIndex()];
                }
            }
        }
        return result;
    }

    public Iterator<IMappingElement<T>> iterator() {
        return new MatrixMappingIterator();
    }

    public boolean add(IMappingElement<T> e) {
        return setRelation(e.getSource(), e.getTarget(), e.getRelation());
    }

    public boolean remove(Object o) {
        boolean result = false;
        if (o instanceof IMappingElement) {
            IMappingElement e = (IMappingElement) o;
            if (e.getSource() instanceof IIndexedObject) {
                @SuppressWarnings("unchecked")
                T s = (T) e.getSource();
                if (e.getTarget() instanceof IIndexedObject) {
                    @SuppressWarnings("unchecked")
                    T t = (T) e.getTarget();
                    result = setRelation(s, t, IMappingElement.IDK);
                }
            }
        }

        return result;
    }

    public void clear() {
        final int rows = matrix.getX();
        final int cols = matrix.getY();
        matrix.init(rows, cols);

        elementCount = 0;

        Arrays.fill(sources, null);
        Arrays.fill(targets, null);
    }

    public IContext getSourceContext() {
        return sourceContext;
    }

    public IContext getTargetContext() {
        return targetContext;
    }

    public void setSourceContext(IContext newContext) {
        sourceContext = newContext;
    }

    public void setTargetContext(IContext newContext) {
        targetContext = newContext;
    }

    public IContextMapping<INode> getContextMappingInstance(IContext source, IContext target) {
        return new NodesMatrixMapping(factory, source, target);
    }

    public IContextMapping<IAtomicConceptOfLabel> getACoLMappingInstance(IContext source, IContext target) {
        return new ACoLMatrixMapping(factory, source, target);
    }

    protected int getColCount(IContext c) {
        return -1;
    }

    protected int getRowCount(IContext c) {
        return -1;
    }
}
