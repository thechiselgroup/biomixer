/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.embeds;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogExitCallback;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.VerticalTreeLayoutAlgorithm;

import com.google.inject.Inject;

public class TermNeighbourhoodLoader extends AbstractTermGraphEmbedLoader {

    @Inject
    protected DialogManager dialogManager;

    public static final int MAX_NUMBER_OF_NEIGHBOURS = 20;

    private class ConceptNeighbourhoodCallback extends
            ErrorHandlingAsyncCallback<ResourceNeighbourhood> {

        private NeighbourCapBreachDialog neighbourBreachDialog;

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

        private ResourceNeighbourhood originalTargetNeighbourhood;

        private int entriesAddedSoFar = 0;

        private ConceptNeighbourhoodCallback(ErrorHandler errorHandler,
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

            // This is where the really slow stuff happens. One result,
            // with 381 children, took about 3 or 4 seconds to parse (just prior
            // to this part), but then takes on the order of a minute or longer
            // to render.
            // So...we can render just a few of the results (20 or so).
            // 0. Do we care about the 3 or 4 seconds to get the neighbourhood
            // parsed? Answer: Probably nothing to do, and that's not so bad
            // really.
            // 1. Can I check ahead of time how many children there are? Would
            // this help? Answer: Not really. They need to be parsed to see
            // this, which takes about as much time as a REST request to grab
            // that number might take.
            // 2. How should I restrict the number of children that get loaded?
            // Answer: Grab the default number, in whatever order. I had no
            // better alternative.
            // 3. Can I change how they are loaded to make it faster rather than
            // restricting loading? Answer: no, this appears to be a DOM
            // efficiency or browser memory issue. With 381 nodes, things are
            // very very slow to load, and slow to move all together.
            // 4. Should we somehow cache these and be able to avoid an
            // additional REST call if the user triggers expansion again?
            // Answer: Perhaps later.

            // Memorize this to make the callbacks easier and cleaner to use.
            // Maybe not cool?
            ConceptNeighbourhoodCallback.this.originalTargetNeighbourhood = targetNeighbourhood;
            // Storing the number added so far helps with trickle results (such
            // as the individual calls required for composition relations).
            // Before prompting, we need to see if we have already prompted, and
            // if so and we want to cap results, we don't want to add any
            // results that are trickling in from individual REST calls.
            if (this.entriesAddedSoFar
                    + targetNeighbourhood.getResources().size() > MAX_NUMBER_OF_NEIGHBOURS) {
                // Callback will perform setGraphViewResources() for us.
                // setGraphViewResources(true);
                userPromptForNeighbourCap(targetNeighbourhood.getResources()
                        .size(), MAX_NUMBER_OF_NEIGHBOURS);
            } else {
                setGraphViewResources(false);
            }
        }

        private void setGraphViewResources(boolean capNodes) {
            ResourceNeighbourhood targetNeighbourhood = this.originalTargetNeighbourhood;
            if (capNodes) {
                targetNeighbourhood = updateRenderedNeighboursWithMaximumNumber(targetNeighbourhood);
            }
            this.entriesAddedSoFar += targetNeighbourhood.getResources().size();

            // TODO XXX Do I even need this partial property thing here? I think
            // the automatic expander might do this too...but maybe the
            // Seems to work fine without these being applied here.
            // Perhaps I can add properties immediately after getting them?
            // Perhaps we can safely rely on the automatic expanders providing
            // relational properties rather than handling them in neighbourhood
            // collectors?
            targetResource.addRelationalProperties(targetNeighbourhood
                    .getPartialProperties());
            resourceSet.addAll(targetNeighbourhood.getResources());
            graphView.getResourceModel().addResourceSet(resourceSet);
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
            neighbourBreachDialog = new NeighbourCapBreachDialog(numResources,
                    maxDefault);
            neighbourBreachDialog.setDialogExitCallback(dialogExitCallback);
            dialogManager.show(neighbourBreachDialog);
        }

        /**
         * Updates the graph view with the neighborhood, with a maximum number
         * of neighbors to enhance performance with large neighborhoods.
         * 
         * @param originalTargetNeighbourhood
         * 
         * @return
         * 
         */
        private ResourceNeighbourhood updateRenderedNeighboursWithMaximumNumber(
                ResourceNeighbourhood originalTargetNeighbourhood) {
            long i = 0;
            List<Resource> cappedSet = new ArrayList<Resource>(
                    TermNeighbourhoodLoader.MAX_NUMBER_OF_NEIGHBOURS);

            for (Resource res : originalTargetNeighbourhood.getResources()) {
                cappedSet.add(res);
                i++;
                // Put break here, so we have a minimum of 1, in case max is 0.
                if (i >= TermNeighbourhoodLoader.MAX_NUMBER_OF_NEIGHBOURS) {
                    break;
                }
            }
            ResourceNeighbourhood cappedNeighbourhood = new ResourceNeighbourhood(
                    originalTargetNeighbourhood.getPartialProperties(),
                    cappedSet);
            return cappedNeighbourhood;
        }
    }

    public static final String EMBED_MODE = "concept_neighbourhood";

    @Inject
    private TermServiceAsync termService;

    @Inject
    private ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    @Inject
    public TermNeighbourhoodLoader() {
        super("term neighborhood", EMBED_MODE);
    }

    private void doLoadData(final String ontologyAcronym,
            final String fullConceptId, final View graphView,
            ErrorHandler errorHandler) {
        termService.getBasicInformation(ontologyAcronym, fullConceptId,
                new ErrorHandlingAsyncCallback<Resource>(errorHandler) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve basic information for "
                                + fullConceptId;
                    }

                    @Override
                    protected void runOnSuccess(final Resource targetResource)
                            throws Exception {
                        final ResourceSet resourceSet = new DefaultResourceSet();
                        resourceSet.add(targetResource);
                        conceptNeighbourhoodService.getNeighbourhood(
                                ontologyAcronym, fullConceptId,
                                new ConceptNeighbourhoodCallback(errorHandler,
                                        resourceSet, fullConceptId, graphView,
                                        targetResource), targetResource);
                    }
                });

    }

    @Override
    protected LayoutAlgorithm getLayoutAlgorithm(ErrorHandler errorHandler) {
        // return new RadialTreeLayoutAlgorithm(false, errorHandler,
        // nodeAnimator);
        return new VerticalTreeLayoutAlgorithm(true, errorHandler, nodeAnimator);
    }

    @Override
    protected void loadData(final String ontologyAcronym,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {
        // XXX remove once proper view content display lifecycle is available
        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadData(ontologyAcronym, fullConceptId, graphView,
                        errorHandler);
            }
        }, new ViewIsReadyCondition(graphView), 200);
    }

}
