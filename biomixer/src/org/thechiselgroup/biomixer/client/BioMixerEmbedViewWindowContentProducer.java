package org.thechiselgroup.biomixer.client;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.visualization.ViewTopBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.DefaultSelectionModel;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.ResourceModel;

import com.google.gwt.user.client.ui.DockPanel;

public class BioMixerEmbedViewWindowContentProducer extends
        BioMixerViewWindowContentProducer {

    @Override
    protected List<ViewTopBarExtension> createViewTopBarExtensions(
            ResourceModel resourceModel, DefaultSelectionModel selectionModel) {

        ArrayList<ViewTopBarExtension> extensions = new ArrayList<ViewTopBarExtension>();

        extensions.add(new ViewTopBarExtension() {
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
