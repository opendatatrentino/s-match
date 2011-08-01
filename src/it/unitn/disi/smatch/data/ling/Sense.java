package it.unitn.disi.smatch.data.ling;

/**
 * Default sense implementation.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Sense implements ISense {

    char pos;
    long id;

    private Sense() {
    }

    public Sense(char pos, long id) {
        this.pos = pos;
        this.id = id;
    }

    public char getPos() {
        return pos;
    }

    public void setPos(char pos) {
        this.pos = pos;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return pos + "#" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sense)) return false;

        Sense sense = (Sense) o;

        if (id != sense.id) return false;
        if (pos != sense.pos) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) pos;
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
}
