package org.thechiselgroup.biomixer.client;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.visualization.ConfigurationBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.DefaultSelectionModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

import com.google.gwt.user.client.ui.DockPanel;

public class BioMixerEmbedViewWindowContentProducer extends
        BioMixerViewWindowContentProducer {

    @Override
    protected List<ConfigurationBarExtension> createConfigurationBarExtensions(
            ResourceModel resourceModel, DefaultSelectionModel selectionModel) {

        ArrayList<ConfigurationBarExtension> extensions = new ArrayList<ConfigurationBarExtension>();

        extensions.add(new ConfigurationBarExtension() {
            @Override
            public void dispose() {
                // TODO Auto-generated method stub

            }

            @Override
            public void init(DockPanel configurationBar) {
                // TODO Auto-generated method stub
            }
        });

        return extensions;
    }
}
