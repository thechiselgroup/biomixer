package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TestLayoutLoader implements EmbeddedViewLoader {

    public static final String EMBED_MODE = "test";

    @Inject
    private WindowContentProducer windowContentProducer;

    @Inject
    private ErrorHandler errorHandler;

    @Override
    public String getEmbedMode() {
        return EMBED_MODE;
    }

    @Override
    public void loadView(WindowLocation windowLocation,
            AsyncCallback<View> callback) {
        final View graphView = ((ViewWindowContent) windowContentProducer
                .createWindowContent(Graph.ID)).getView();
        graphView.init();
        callback.onSuccess(graphView);

        final String fullConceptId = UriUtils.decodeURIComponent(windowLocation
                .getParameter("full_concept_id"));
        final String virtualOntologyId = windowLocation
                .getParameter("virtual_ontology_id");

        // TODO get appropriate data for your layout

    }

}
