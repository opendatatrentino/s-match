package it.unitn.disi.common.pipelines;

import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Base pipeline class.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class BasePipeline<E> extends Configurable implements IBasePipeline<E> {

    private static final Logger log = Logger.getLogger(BasePipeline.class);

    protected List<IBasePipelineComponent<E>> pipelineComponents;

    public void process(E instance) throws PipelineComponentException {
        for (IBasePipelineComponent<E> c : pipelineComponents) {
            c.beforeInstanceProcessing(instance);
        }
        for (IBasePipelineComponent<E> c : pipelineComponents) {
            c.process(instance);
        }
        for (IBasePipelineComponent<E> c : pipelineComponents) {
            c.afterInstanceProcessing(instance);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        pipelineComponents = new ArrayList<IBasePipelineComponent<E>>();

        boolean result = super.setProperties(newProperties);
        if (result) {
            int componentIndex = 1;
            String strComponentIndex = "1";
            boolean componentFound = newProperties.containsKey(strComponentIndex);
            while (componentFound) {
                IBasePipelineComponent<E> component = null;
                component = (IBasePipelineComponent<E>) configureComponent(component, oldProperties, newProperties, "pipeline component #" + strComponentIndex, strComponentIndex, IBasePipelineComponent.class);
                if (null != component) {
                    pipelineComponents.add(component);
                }
                componentIndex++;
                strComponentIndex = Integer.toString(componentIndex);
                componentFound = newProperties.containsKey(strComponentIndex);
            }

            if (log.isEnabledFor(Level.INFO)) {
                log.info("Loaded pipeline components: " + Integer.toString(componentIndex - 1));
            }
        }
        return result;
    }

    public void beforeProcessing() throws PipelineComponentException {
        //nop
    }

    public void afterProcessing() throws PipelineComponentException {
        //nop
    }
}