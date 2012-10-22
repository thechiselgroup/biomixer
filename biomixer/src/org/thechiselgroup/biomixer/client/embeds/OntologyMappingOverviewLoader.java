package org.thechiselgroup.biomixer.client.embeds;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.visualization.LeftViewTopBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.dnd.resources.DropEnabledViewContentDisplay;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCount;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCountServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology_overview.TotalMappingCount;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.HorizontalTreeLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Largely similar to the {@link AbstractTermGraphEmbedLoader}. Significantly
 * different data requirements. Could possibly get refactored a fair bit.
 * 
 * @author everbeek
 * 
 */
public class OntologyMappingOverviewLoader implements OntologyEmbedLoader {

    // For the parallel term system, these embed mode parts are in the multiple
    // non-abstract classes.
    // See equivalent there for how we can make canned embed views for this
    // ontology mapping overview.
    // e.g. in PathsToRootEmbedLoader
    // public static final String EMBED_MODE = "ontology_overview";

    @Inject
    protected DialogManager dialogManager;

    public static final int MAX_NUMBER_OF_NEIGHBOURS = 20;

    private class OntologyMappingOverviewCallback extends
            TimeoutErrorHandlingAsyncCallback<TotalMappingCount> {

        private final ResourceSet resourceSet;

        private final List<String> virtualOntologyIds;

        private final View graphView;

        // private final Resource targetResource;

        /**
         * IN PROGRESS, COPIED FROM ELSEWHERE
         * 
         * @param errorHandler
         * @param resourceSet
         * @param fullConceptId
         * @param graphView
         * @param targetResource
         */
        private OntologyMappingOverviewCallback(ErrorHandler errorHandler,
                ResourceSet virtualOntologies, List<String> virtualOntologyIds,
                View graphView
        // Resource targetResource
        ) {
            super(errorHandler);
            this.resourceSet = virtualOntologies;
            this.virtualOntologyIds = virtualOntologyIds;
            this.graphView = graphView;
            // this.targetResource = targetResource;
        }

        @Override
        protected String getMessage(Throwable caught) {
            String virtualIdsPrintable = "";
            String separator = "";
            for (String id : virtualOntologyIds) {
                virtualIdsPrintable = separator + id;
                separator = ", ";
            }
            return "Could not retrieve mapping data for all ontologies with ids: "
                    + virtualIdsPrintable;
        }

        @Override
        protected void runOnSuccess(TotalMappingCount totalMappingCount)
                throws Exception {

            hideLoadingBar();

            // Add arcs for mapping relations on set members
            // TODO Should I do this in an exposed way here, or set up
            // MappingCount to have
            // PartialProperties that get this done? I think the latter...
            for (OntologyMappingCount count : totalMappingCount) {
                // TODO I think I want a map of ids to resources.
                // Or is the uri argument from the ResourceSet the same as the
                // ids I get from the count?
                Resource first = resourceSet.getByUri(count.getId1());
                Resource second = resourceSet.getByUri(count.getId2());
                // TODO Do something to make an arc...what kind? This isn't
                // really a MappingArcType arc...
                // This is right. And we will be in a different graph view.
                // So, make a new type!
            }

            // targetResource.applyPartialProperties(targetNeighbourhood
            // .getPartialProperties());
            // resourceSet.addAll(targetNeighbourhood.getResources());

            graphView.getResourceModel().addResourceSet(resourceSet);
        }

    }

    @Named("embed")
    @Inject
    protected ViewWindowContentProducer viewContentProducer;

    @Inject
    protected DelayedExecutor executor;

    protected NodeAnimator nodeAnimator;

    @Inject
    private OntologyMappingCountServiceAsync ontologyMappingService;

    // See PathToRootEmbed for how to refactor this if we have multiple
    // embed types
    private final String id = "ontology_overview";

    private final String label = "ontology overview";

    public OntologyMappingOverviewLoader() {
    }

    // public OntologyOverviewLoader(String label, String id) {
    // assert label != null;
    // assert id != null;
    //
    // this.id = id;
    // this.label = label;
    // }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    protected LayoutAlgorithm getLayoutAlgorithm(ErrorHandler errorHandler) {
        return new HorizontalTreeLayoutAlgorithm(true, errorHandler,
                nodeAnimator);
    }

    private NodeAnimator getNodeAnimator(View graphView) {
        return graphView.adaptTo(GraphLayoutSupport.class).getNodeAnimator();
    }

    protected void hideLoadingBar() {
        RootPanel rootPanel = RootPanel.get("loadingMessage");
        rootPanel.setVisible(false);
    }

    private void setLayoutAlgorithm(View graphView,
            LayoutAlgorithm layoutAlgorithm) {
        graphView.adaptTo(GraphLayoutSupport.class).registerDefaultLayout(
                layoutAlgorithm);
    }

    @Override
    public void loadView(ResourceSet virtualOntologies,
            List<String> virtualOntologyIds, IsWidget topBarWidget,
            AsyncCallback<IsWidget> callback) {

        View graphView = ((ViewWindowContent) viewContentProducer
                .createWindowContent(Graph.ID)).getView();

        // XXX likely to break when view content setup changes
        // get the error handler from the view content display
        // to show the errors in the view-specific error box (ListBox)
        DropEnabledViewContentDisplay cd1 = (DropEnabledViewContentDisplay) graphView
                .getModel().getViewContentDisplay();
        Graph graph = (Graph) cd1.getDelegate();
        ErrorHandler errorHandler = graph.getErrorHandler();

        graphView.addTopBarExtension(new LeftViewTopBarExtension(topBarWidget));

        // add a loading bar so the user knows the application is being
        // loaded
        Image loadingMessage = new Image(GWT.getModuleBaseURL()
                + "images/ajax-loader-bar.gif");
        graphView
                .addTopBarExtension(new LeftViewTopBarExtension(loadingMessage));
        loadingMessage.getElement().setId("loadingMessage");

        graphView.init();
        nodeAnimator = getNodeAnimator(graphView);
        setLayoutAlgorithm(graphView, getLayoutAlgorithm(errorHandler));
        callback.onSuccess(graphView);

        loadData(virtualOntologyIds, virtualOntologies, graphView, errorHandler);
    }

    protected void loadData(final List<String> virtualOntologyIds,
            final ResourceSet virtualOntologies, final View graphView,
            final ErrorHandler errorHandler) {
        // XXX remove once proper view content display lifecycle is
        // available
        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadData(virtualOntologyIds, virtualOntologies, graphView,
                        errorHandler);
            }
        }, new ViewIsReadyCondition(graphView), 200);
    }

    /**
     * IN PROGRESS, COPIED FROM ELSEWHERE
     * 
     * @param virtualOntologyId
     * @param graphView
     * @param errorHandler
     */
    private void doLoadData(final List<String> virtualOntologyIds,
            final ResourceSet virtualOntologies, final View graphView,
            ErrorHandler errorHandler) {
        /*
         * TODO Need to receive or create Resources containing ontologies. Which
         * way should I do it? The TermServiceImplementation deals with
         * resources that come directly from queries, using a transformer,
         * pasted below. This implies to me that we want to receive resources
         * rather than ontology strings in this case, because they will be
         * dropped in from a search list or some such. That is, there will
         * already be resources for each; we are looking up their mapping
         * relations to render them in a particular fashion. Also, when we look
         * at the TermServiceImplementation, we see that the targetTerm gets
         * added as a resource right away. new Transformer<String, Resource>() {
         * 
         * @Override public Resource transform(String value) throws Exception {
         * Resource resource = responseParser .parseConcept(ontologyId, value);
         * resource.putValue( Concept.CONCEPT_ONTOLOGY_NAME, ontologyName);
         * return resource; } }
         */

        final ResourceSet resourceSet = new DefaultResourceSet();
        for (Resource ontologyId : virtualOntologies) {
            resourceSet.add(ontologyId);
        }
        ontologyMappingService.getMappingCounts(virtualOntologyIds,
                new OntologyMappingOverviewCallback(errorHandler,
                        virtualOntologies, virtualOntologyIds, graphView));
    };

}
