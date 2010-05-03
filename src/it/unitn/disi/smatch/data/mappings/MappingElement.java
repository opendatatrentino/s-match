package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.INode;

/**
 * Default mapping element implementation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MappingElement implements IMappingElement {

    private INode sourceNode;
    private INode targetNode;
    private char relation;

    public MappingElement() {
    }

    public MappingElement(INode sourceNode, INode targetNode, char relation) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.relation = relation;
    }

    public INode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(INode newSource) {
        sourceNode = newSource;
    }

    public INode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(INode newTarget) {
        targetNode = newTarget;
    }

    public char getRelation() {
        return relation;
    }

    public void setRelation(char newRelation) {
        relation = newRelation;
    }
}