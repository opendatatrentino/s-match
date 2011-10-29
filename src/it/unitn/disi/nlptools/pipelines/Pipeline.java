package it.unitn.disi.nlptools.pipelines;

import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.nlptools.INLPPipeline;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.Label;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Default pipeline implementation.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Pipeline extends Configurable implements INLPPipeline {

    private static final Logger log = Logger.getLogger(Pipeline.class);

    private List<IPipelineComponent> pipelineComponents;

    public ILabel process(String label) throws PipelineComponentException {
        ILabel result = new Label(label);
        process(result);
        return result;
    }

    public void process(ILabel label) throws PipelineComponentException {
        for (IPipelineComponent c : pipelineComponents) {
            c.process(label);
        }
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);

        pipelineComponents = new ArrayList<IPipelineComponent>();

        boolean result = super.setProperties(newProperties);
        if (result) {
            int componentIndex = 1;
            String strComponentIndex = "1";
            boolean componentFound = newProperties.containsKey(strComponentIndex);
            while (componentFound) {
                IPipelineComponent component = null;
                component = (IPipelineComponent) configureComponent(component, oldProperties, newProperties, "pipeline component #" + strComponentIndex, strComponentIndex, IPipelineComponent.class);
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
}