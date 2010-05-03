package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.IContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Default mapping implementation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Mapping implements IMapping {

    private HashSet<IMappingElement> set;
    private IContext sourceContext;
    private IContext targetContext;

    public Mapping(IContext sourceContext, IContext targetContext) {
        set = new HashSet<IMappingElement>();
        this.sourceContext = sourceContext;
        this.targetContext = targetContext;
    }

    public int size() {
        return set.size();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public boolean contains(Object o) {
        return set.contains(o);
    }

    public Iterator<IMappingElement> iterator() {
        return set.iterator();
    }

    public Object[] toArray() {
        return set.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    public boolean add(IMappingElement e) {
        return set.add(e);
    }

    public boolean remove(Object o) {
        return set.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    public boolean addAll(Collection<? extends IMappingElement> c) {
        return set.addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    public void clear() {
        set.clear();
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
}
