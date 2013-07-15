package it.unitn.disi.smatch.classifiers;

import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

import java.util.Iterator;

/**
 * Version with an iterator.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class CNFContextClassifierIt extends CNFContextClassifier {

    public void buildCNodeFormulas(IContext context) throws ContextClassifierException {
        for (Iterator<INode> i = context.getNodes(); i.hasNext();) {
            buildCNode(i.next());
        }
    }

}
