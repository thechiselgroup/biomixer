package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogExitCallback;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.visualization.LeftViewTopBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.dnd.resources.DropEnabledViewContentDisplay;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
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
public class OntologyOverviewLoader implements OntologyEmbedLoader {

    @Inject
    protected DialogManager dialogManager;

    public static final int MAX_NUMBER_OF_NEIGHBOURS = 20;

    @Named("embed")
    @Inject
    protected ViewWindowContentProducer viewContentProducer;

    @Inject
    protected DelayedExecutor executor;

    protected NodeAnimator nodeAnimator;

    // @Inject
    // // WHAT IS THE MANIFEST CLASS FOR THIS?
    // private OntologyTermCountServiceAsync ontologyService;
    //
    // @Inject
    // private OntologyMappingOverviewServiceAsync ontologyMappingService;

    private final String id;

    private final String label;

    public OntologyOverviewLoader(String label, String id) {
        assert label != null;
        assert id != null;

        this.id = id;
        this.label = label;
    }

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
    public void loadView(String virtualOntologyId, IsWidget topBarWidget,
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

        // add a loading bar so the user knows the application is being loaded
        Image loadingMessage = new Image(GWT.getModuleBaseURL()
                + "images/ajax-loader-bar.gif");
        graphView
                .addTopBarExtension(new LeftViewTopBarExtension(loadingMessage));
        loadingMessage.getElement().setId("loadingMessage");

        graphView.init();
        nodeAnimator = getNodeAnimator(graphView);
        setLayoutAlgorithm(graphView, getLayoutAlgorithm(errorHandler));
        callback.onSuccess(graphView);

        loadData(virtualOntologyId, graphView, errorHandler);
    }

    /**
     * IN PROGRESS, COPIED FROM ELSEWHERE
     * 
     * @param virtualOntologyId
     * @param graphView
     * @param errorHandler
     */
    protected void loadData(final String virtualOntologyId,
            final View graphView, final ErrorHandler errorHandler) {
        // XXX remove once proper view content display lifecycle is available
        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadData(virtualOntologyId, graphView, errorHandler);
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
    private void doLoadData(final String virtualOntologyId,
            final View graphView, ErrorHandler errorHandler) {
        // ontologyService.getNeighbourhood(
        // virtualOntologyId,
        // new OntologyMappingOverviewCallback(
        // errorHandler, graphView));
        // });

    }

    private class OntologyMappingOverviewCallback extends
            TimeoutErrorHandlingAsyncCallback<ResourceNeighbourhood> {

        private MappingCapBreachDialog neighbourBreachDialog;

        final private DialogExitCallback dialogExitCallback = new DialogExitCallback() {
            @Override
            public void dialogExited() {
                setGraphViewResources(neighbourBreachDialog.getExitCode() == NeighbourCapBreachDialog.OK_WITH_CAP_EXIT_CODE);
            }
        };

        private final ResourceSet resourceSet;

        private final String fullConceptId;

        private final View graphView;

        private final Resource targetResource;

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
                ResourceSet resourceSet, String fullConceptId, View graphView,
                Resource targetResource) {
            super(errorHandler);
            this.resourceSet = resourceSet;
            this.fullConceptId = fullConceptId;
            this.graphView = graphView;
            this.targetResource = targetResource;
        }

        @Override
        protected String getMessage(Throwable caught) {
            return "Could not expand concept neighbourhood for "
                    + fullConceptId;
        }

        @Override
        protected void runOnSuccess(ResourceNeighbourhood targetNeighbourhood)
                throws Exception {

            hideLoadingBar();

            // targetResource.applyPartialProperties(targetNeighbourhood
            // .getPartialProperties());
            // resourceSet.addAll(targetNeighbourhood.getResources());
            //
            // // This is where the really slow stuff happens. One result,
            // // with 381 children, took about 3 or 4 seconds to parse (just
            // prior
            // // to this part), but then takes on the order of a minute or
            // longer
            // // to render.
            // // So...we can render just a few of the results (20 or so).
            // // 0. Do we care about the 3 or 4 seconds to get the
            // neighbourhood
            // // parsed? Answer: Probably nothing to do, and that's not so bad
            // // really.
            // // 1. Can I check ahead of time how many children there are?
            // Would
            // // this help? Answer: Not really. They need to be parsed to see
            // // this, which takes about as much time as a REST request to grab
            // // that number might take.
            // // 2. How should I restrict the number of children that get
            // loaded?
            // // Answer: Grab the default number, in whatever order. I had no
            // // better alternative.
            // // 3. Can I change how they are loaded to make it faster rather
            // than
            // // restricting loading? Answer: no, this appears to be a DOM
            // // efficiency or browser memory issue. With 381 nodes, things are
            // // very very slow to load, and slow to move all together.
            // // 4. Should we somehow cache these and be able to avoid an
            // // additional REST call if the user triggers expansion again?
            // // Answer: Perhaps later.
            //
            // if (resourceSet.size() > MAX_NUMBER_OF_NEIGHBOURS) {
            // // Callback will perform setGraphViewResources() for us.
            // // setGraphViewResources(true);
            // userPromptForNeighbourCap(resourceSet.size(),
            // MAX_NUMBER_OF_NEIGHBOURS);
            // } else {
            // setGraphViewResources(false);
            // }
        }

        private void setGraphViewResources(boolean capNodes) {
            ResourceSet setToRender = resourceSet;
            if (capNodes) {
                setToRender = updateRenderedNeighboursWithMaximumNumber();
            } else {
                setToRender = resourceSet;
            }
            graphView.getResourceModel().addResourceSet(setToRender);
        }

        /**
         * This asks the user if they would like to limit the number of rendered
         * results. Since we cannot have synchronous things in GWT (JS is single
         * threaded), we are stuck using callbacks, ultimately.
         * 
         * @param setToRender
         * @return
         */
        private void userPromptForNeighbourCap(int numResources, int maxDefault) {
            neighbourBreachDialog = new MappingCapBreachDialog(numResources,
                    maxDefault);
            neighbourBreachDialog.setDialogExitCallback(dialogExitCallback);
            dialogManager.show(neighbourBreachDialog);
        }

        /**
         * Updates the graph view with the neighborhood, with a maximum number
         * of neighbors to enhance performance with large neighborhoods.
         * 
         * @return
         * 
         */
        private ResourceSet updateRenderedNeighboursWithMaximumNumber() {
            long i = 0;
            ResourceSet cappedSet = new DefaultResourceSet();

            for (Resource res : resourceSet) {
                cappedSet.add(res);
                i++;
                // Put break here, so we have a minimum of 1, in case max is 0.
                if (i >= MAX_NUMBER_OF_NEIGHBOURS) {
                    break;
                }
            }
            return cappedSet;
        }
    }

}
