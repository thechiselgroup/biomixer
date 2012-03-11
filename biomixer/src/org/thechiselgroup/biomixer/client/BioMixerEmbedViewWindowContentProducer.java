package org.thechiselgroup.biomixer.client;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.visualization.ConfigurationBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.DefaultSelectionModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

public class BioMixerEmbedViewWindowContentProducer extends
        BioMixerViewWindowContentProducer {

    @Override
    protected List<ConfigurationBarExtension> createConfigurationBarExtensions(
            ResourceModel resourceModel, DefaultSelectionModel selectionModel) {

        return new ArrayList<ConfigurationBarExtension>();
    }

}
