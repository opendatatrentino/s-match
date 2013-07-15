package it.unitn.disi.annotation.pipelines;

import it.unitn.disi.common.pipelines.BasePipelineComponent;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IBaseNode;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class BaseContextPipelineComponent<E extends IBaseNode> extends BasePipelineComponent<IBaseContext<E>> implements IBaseContextPipelineComponent<E> {
}
