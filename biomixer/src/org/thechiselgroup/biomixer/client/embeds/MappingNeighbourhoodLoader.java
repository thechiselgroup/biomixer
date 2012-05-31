/*******************************************************************************
 * Copyright 2012 David Rusk 
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
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.util.animation.NullAnimationRunner;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.core.visualization.ViewIsReadyCondition;
import org.thechiselgroup.biomixer.client.services.mapping.MappingServiceAsync;
import org.thechiselgroup.biomixer.client.services.term.TermServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.circle.CircleLayoutAlgorithm;

import com.google.inject.Inject;

public class MappingNeighbourhoodLoader extends AbstractTermGraphEmbedLoader {

    private static final double MIN_ANGLE = 0.0;

    private static final double MAX_ANGLE = 180.0;

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
            final String fullConceptId, final View graphView) {

        termService.getBasicInformation(virtualOntologyId, fullConceptId,
                new ErrorHandlingAsyncCallback<Resource>(errorHandler) {
                    @Override
                    protected void runOnSuccess(final Resource targetResource)
                            throws Exception {
                        final ResourceSet resourceSet = new DefaultResourceSet();
                        resourceSet.add(targetResource);

                        // TODO move to MappedConceptsServiceAsyncImpl
                        mappingService
                                .getMappings(
                                        virtualOntologyId,
                                        fullConceptId,
                                        new ErrorHandlingAsyncCallback<ResourceNeighbourhood>(
                                                errorHandler) {

                                            @Override
                                            protected void runOnSuccess(
                                                    ResourceNeighbourhood mappingNeighbourhood)
                                                    throws Exception {

                                                targetResource
                                                        .applyPartialProperties(mappingNeighbourhood
                                                                .getPartialProperties());

                                                for (Resource mappingResource : mappingNeighbourhood
                                                        .getResources()) {
                                                    String sourceUri = Mapping
                                                            .getSource(mappingResource);
                                                    String targetUri = Mapping
                                                            .getTarget(mappingResource);

                                                    final String otherUri = targetResource
                                                            .getUri().equals(
                                                                    sourceUri) ? targetUri
                                                            : sourceUri;

                                                    final String otherOntologyId = Concept
                                                            .getOntologyId(otherUri);
                                                    final String otherConceptId = Concept
                                                            .getConceptId(otherUri);
                                                    termService
                                                            .getBasicInformation(
                                                                    otherOntologyId,
                                                                    otherConceptId,
                                                                    new ErrorHandlingAsyncCallback<Resource>(
                                                                            errorHandler) {
                                                                        @Override
                                                                        protected void runOnSuccess(
                                                                                Resource result)
                                                                                throws Exception {

                                                                            resourceSet
                                                                                    .add(result);
                                                                            graphView
                                                                                    .getResourceModel()
                                                                                    .addResourceSet(
                                                                                            resourceSet);
                                                                        }

                                                                        @Override
                                                                        protected Throwable wrapException(
                                                                                Throwable caught) {
                                                                            return new Exception(
                                                                                    "Could not get basic information for "
                                                                                            + otherConceptId,
                                                                                    caught);
                                                                        }
                                                                    });

                                                }

                                            }

                                            @Override
                                            protected Throwable wrapException(
                                                    Throwable caught) {
                                                return new Exception(
                                                        "Could not expand mapping neighbourhood for "
                                                                + fullConceptId,
                                                        caught);
                                            }

                                        });
                    }

                    @Override
                    protected Throwable wrapException(Throwable caught) {
                        return new Exception(
                                "Could not retrieve basic information for "
                                        + fullConceptId, caught);
                    }
                });
    }

    @Override
    protected LayoutAlgorithm getLayoutAlgorithm() {
        /*
         * XXX using NullAnimationRunner for now due to excess lag issues with
         * regular animations when the layout is triggered repeatedly. Once that
         * problem is fixed, use the regular animation runner
         */
        CircleLayoutAlgorithm layout = new CircleLayoutAlgorithm(errorHandler,
                new NullAnimationRunner());
        layout.setAngleRange(MIN_ANGLE, MAX_ANGLE);
        return layout;
    }

    @Override
    protected void loadData(final String virtualOntologyId,
            final String fullConceptId, final View graphView) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                doLoadData(virtualOntologyId, fullConceptId, graphView);
            }
        }, new ViewIsReadyCondition(graphView), 200);
    }

}
