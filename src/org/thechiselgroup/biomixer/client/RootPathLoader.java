package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.visualization.DefaultView;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.services.rootpath.RootPathServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourcePath;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphLayouts;
import org.thechiselgroup.biomixer.client.workbench.embed.EmbeddedViewLoader;
import org.thechiselgroup.biomixer.client.workbench.init.WindowLocation;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class RootPathLoader implements EmbeddedViewLoader {

    public static final String EMBED_MODE = "path_to_root";

    @Inject
    private RootPathServiceAsync rootPathService;

    @Inject
    private WindowContentProducer windowContentProducer;

    @Inject
    private ErrorHandler errorHandler;

    private void doLoadData(final DefaultView view, final String ontologyId,
            final String conceptFullId) {

        rootPathService.findPathToRoot(ontologyId, conceptFullId,
                new ErrorHandlingAsyncCallback<ResourcePath>(errorHandler) {
                    @Override
                    protected void runOnSuccess(ResourcePath resourcePath)
                            throws Exception {
                        ResourceSet resourceSet = new DefaultResourceSet();
                        resourceSet.addAll(resourcePath
                                .getPathToRootResources());
                        view.getResourceModel().addResourceSet(resourceSet);
                        layout(view);
                    }
                });
    }

    @Override
    public String getEmbedMode() {
        return EMBED_MODE;
    }

    private void layout(final DefaultView view) {
        new Timer() {
            @Override
            public void run() {
                view.adaptTo(GraphLayoutSupport.class).runLayout(
                        GraphLayouts.VERTICAL_TREE_LAYOUT);
            }
        }.schedule(50);
    }

    // XXX same as concept neighbourhood loader
    private void loadData(final DefaultView view, final String ontologyId,
            final String conceptFullId) {

        if (view.isReady()) {
            doLoadData(view, ontologyId, conceptFullId);
        } else {
            new Timer() {
                @Override
                public void run() {
                    loadData(view, ontologyId, conceptFullId);
                }
            }.schedule(200);
        }
    }

    @Override
    public void loadView(WindowLocation windowLocation,
            AsyncCallback<View> callback) {

        final View graphView = ((ViewWindowContent) windowContentProducer
                .createWindowContent(Graph.ID)).getView();
        graphView.init();
        callback.onSuccess(graphView);

        String conceptFullId = windowLocation.getParameter("full_concept_id");
        conceptFullId = UriUtils.decodeURIComponent(conceptFullId);
        String ontologyId = windowLocation.getParameter("ontology_id");

        loadData((DefaultView) graphView, ontologyId, conceptFullId);
    }
}
