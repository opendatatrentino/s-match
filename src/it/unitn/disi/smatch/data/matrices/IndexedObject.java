package it.unitn.disi.smatch.data.matrices;

/**
 * An object with an index.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class IndexedObject implements IIndexedObject {

    protected int index;

    public IndexedObject() {
        this.index = -1;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int newIndex) {
        index = newIndex;
    }
}
