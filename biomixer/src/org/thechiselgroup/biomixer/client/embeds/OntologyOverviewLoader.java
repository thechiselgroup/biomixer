package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.util.collections.SingleItemIterable;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedLoader;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class OntologyOverviewLoader implements EmbeddedViewLoader {

    public static final String EMBED_MODE = "ontology_overview";

    @Override
    public Iterable<String> getEmbedModes() {

        return new SingleItemIterable<String>(EMBED_MODE);
    }

    @Override
    public void loadView(WindowLocation windowLocation, String embedMode,
            AsyncCallback<IsWidget> callback, EmbedLoader loader) {

    }

}
