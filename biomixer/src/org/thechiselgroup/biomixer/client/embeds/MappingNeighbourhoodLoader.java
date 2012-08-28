/*******************************************************************************
 * Copyright 2012 David Rusk, Bo Fu
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

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.animation.NullNodeAnimationFactory;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.mapping.MappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.circle.CircleLayoutAlgorithm;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;

public class MappingNeighbourhoodLoader extends AbstractTermGraphEmbedLoader {

    private class MappingCallback extends
            TimeoutErrorHandlingAsyncCallback<ResourceNeighbourhood> {

        private class BasicTermInfoCallback extends
                TimeoutErrorHandlingAsyncCallback<Resource> {

            private final String otherConceptId;

            private BasicTermInfoCallback(ErrorHandler errorHandler,
                    String otherConceptId) {
                super(errorHandler);
                this.otherConceptId = otherConceptId;
            }

            @Override
            protected String getMessage(Throwable caught) {
                return "Could not get basic information for " + otherConceptId;
            }

            @Override
            protected void runOnSuccess(Resource result) throws Exception {
                // hide loading bar
                RootPanel rootPanel = RootPanel.get("loadingMessage");
                rootPanel.setVisible(false);

                resourceSet.add(result);
                graphView.getResourceModel().addResourceSet(resourceSet);
            }
        }

        private final Resource targetResource;

        private final ResourceSet resourceSet;

        private final String fullConceptId;

        private final View graphView;

        private MappingCallback(ErrorHandler errorHandler,
                Resource targetResource, ResourceSet resourceSet,
                String fullConceptId, View graphView) {
            super(errorHandler);
            this.targetResource = targetResource;
            this.resourceSet = resourceSet;
            this.fullConceptId = fullConceptId;
            this.graphView = graphView;
        }

        @Override
        protected String getMessage(Throwable caught) {
            return "Could not expand mapping neighbourhood for "
                    + fullConceptId;
        }

        @Override
        protected void runOnSuccess(ResourceNeighbourhood mappingNeighbourhood)
                throws Exception {

            targetResource.applyPartialProperties(mappingNeighbourhood
                    .getPartialProperties());

            for (Resource mappingResource : mappingNeighbourhood.getResources()) {
                String sourceUri = Mapping.getSource(mappingResource);
                String targetUri = Mapping.getTarget(mappingResource);

                final String otherUri = targetResource.getUri().equals(
                        sourceUri) ? targetUri : sourceUri;

                final String otherOntologyId = Concept.getOntologyId(otherUri);
                final String otherConceptId = Concept.getConceptId(otherUri);

                termService.getBasicInformation(otherOntologyId,
                        otherConceptId, new BasicTermInfoCallback(errorHandler,
                                otherConceptId));

            }

        }
    }

    private static final double MIN_ANGLE = 0.0;

    private static final double MAX_ANGLE = 360.0;

    public static final String EMBED_MODE = "mapping_neighbourhood";

    @Inject
    private TermServiceAsync termService;

    @Inject
    private MappingServiceAsync mappingService;

    @Inject
    public MappingNeighbourhoodLoader() {
        super("mappings neighborhood", EMBED_MODE);
    }

    private void doLoadData(final String virtualOntologyId,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {

        termService.getBasicInformation(virtualOntologyId, fullConceptId,
                new TimeoutErrorHandlingAsyncCallback<Resource>(errorHandler) {

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

                        // TODO move to MappedConceptsServiceAsyncImpl
                        mappingService.getMappings(virtualOntologyId,
                                fullConceptId, true, new MappingCallback(
                                        errorHandler, targetResource,
                                        resourceSet, fullConceptId, graphView));
                    }

                });
    }

    @Override
    protected LayoutAlgorithm getLayoutAlgorithm(ErrorHandler errorHandler) {
        /*
         * NOTE: we use null node animations for this embed because it is prone
         * to performance problems when trying to animate so many highly
         * interconnected nodes at the same time as loading the data.
         */
        CircleLayoutAlgorithm layout = new CircleLayoutAlgorithm(errorHandler,
                new NodeAnimator(new NullNodeAnimationFactory()));
        layout.setAngleRange(MIN_ANGLE, MAX_ANGLE);
        return layout;
    }

    @Override
    protected void loadData(final String virtualOntologyId,
            final String fullConceptId, final View graphView,
            final ErrorHandler errorHandler) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadData(virtualOntologyId, fullConceptId, graphView,
                        errorHandler);
            }
        }, new ViewIsReadyCondition(graphView), 200);
    }

}
