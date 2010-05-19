package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.INode;

/**
 * Holds mapping element referring to nodes.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MappingNodeElement implements IMappingElement {

    protected INode sourceNode;
    protected INode targetNode;
    protected char relation;

    public MappingNodeElement(INode sourceNode, INode targetNode, char relation) {
        if (null != sourceNode && sourceNode.getNodeData().getSource()) {
            this.relation = relation;
            this.sourceNode = sourceNode;
            this.targetNode = targetNode;
        } else {
            this.sourceNode = targetNode;
            this.targetNode = sourceNode;
            if (LESS_GENERAL == relation) {
                this.relation = MORE_GENERAL;
            } else {
                if (MORE_GENERAL == relation) {
                    this.relation = LESS_GENERAL;
                } else {
                    this.relation = relation;
                }
            }
        }
    }

    public INode getSource() {
        return sourceNode;
    }

    public void setSourceNode(INode newSource) {
        sourceNode = newSource;
    }

    public INode getTarget() {
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

    public int hashCode() {
        int result;
        result = (sourceNode != null ? sourceNode.hashCode() : 0);
        result = 31 * result + (targetNode != null ? targetNode.hashCode() : 0);
        result = 31 * result + (int) relation;
        return result;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MappingNodeElement)) {
            return false;
        }

        MappingNodeElement that = (MappingNodeElement) o;

        if (relation != that.relation) {
            return false;
        }
        if (sourceNode != null ? !sourceNode.equals(that.sourceNode) : that.sourceNode != null) {
            return false;
        }
        if (targetNode != null ? !targetNode.equals(that.targetNode) : that.targetNode != null) {
            return false;
        }

        return true;
    }

    public String toString() {
        return sourceNode.getNodeData().getNodeUniqueName() + "\t" + targetNode.getNodeData().getNodeUniqueName() + "\t" + relation;
    }
}