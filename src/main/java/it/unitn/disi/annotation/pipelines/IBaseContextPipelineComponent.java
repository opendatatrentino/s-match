package it.unitn.disi.annotation.pipelines;

import it.unitn.disi.common.pipelines.IBasePipelineComponent;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IBaseNode;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IBaseContextPipelineComponent<E extends IBaseNode> extends IBasePipelineComponent<IBaseContext<E>> {
}
