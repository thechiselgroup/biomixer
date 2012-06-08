package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.util.collections.IdentifiablesList;
import org.thechiselgroup.biomixer.client.core.util.collections.SingleItemIterable;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbedLoader;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class OntologyCentricEmbedLoader implements EmbeddedViewLoader {

    public static final String EMBED_MODE = "ontology_overview";

    private IdentifiablesList<OntologyOverviewLoader> embedLoaders = new IdentifiablesList<OntologyOverviewLoader>();

    public OntologyCentricEmbedLoader(
            OntologyOverviewLoader ontologyOverviewLoader) {
        registerLoader(ontologyOverviewLoader);
    }

    @Override
    public Iterable<String> getEmbedModes() {
        return new SingleItemIterable<String>(EMBED_MODE);
    }

    @Override
    public void loadView(WindowLocation windowLocation, String embedMode,
            AsyncCallback<IsWidget> callback, EmbedLoader loader) {

    }

    protected void registerLoader(OntologyOverviewLoader loader) {
        embedLoaders.add(loader);
    }
}
