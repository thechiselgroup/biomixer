/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.services.mapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.choosel.core.client.resources.Resource;
import org.thechiselgroup.choosel.core.client.resources.UriList;
import org.thechiselgroup.choosel.core.client.util.UriUtils;
import org.thechiselgroup.choosel.core.client.util.callbacks.TransformingAsyncCallback;
import org.thechiselgroup.choosel.core.client.util.collections.CollectionFactory;
import org.thechiselgroup.choosel.core.client.util.transform.Transformer;
import org.thechiselgroup.choosel.core.client.util.url.UrlBuilder;
import org.thechiselgroup.choosel.core.client.util.url.UrlBuilderFactory;
import org.thechiselgroup.choosel.core.client.util.url.UrlFetchService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Gets mappings from the NCBO REST services.
 * 
 * @author Lars Grammel
 * 
 * @see "http://www.bioontology.org/wiki/index.php/BioPortal_Mappings_Service"
 */
public class MappingServiceImplementation implements MappingServiceAsync {

    private final UrlFetchService urlFetchService;

    private final MappingResponseParser responseParser;

    private final UrlBuilderFactory urlBuilderFactory;

    @Inject
    public MappingServiceImplementation(MappingResponseParser responseParser,
            UrlFetchService urlFetchService, UrlBuilderFactory urlBuilderFactory) {

        this.urlFetchService = urlFetchService;
        this.responseParser = responseParser;
        this.urlBuilderFactory = urlBuilderFactory;
    }

    protected String buildUrl(String ontologyId, String conceptId) {
        UrlBuilder urlBuilder = urlBuilderFactory.createUrlBuilder();
        urlBuilder.setPath("bioportal/virtual/mappings/concepts/" + ontologyId);
        urlBuilder.setParameter("conceptid",
                UriUtils.encodeURIComponent(conceptId));
        return urlBuilder.buildString();
    }

    private Map<String, Serializable> calculatePartialProperties(
            String conceptUri, List<Resource> mappings) {

        UriList outgoingMappings = new UriList();
        UriList incomingMappings = new UriList();
        for (Resource mapping : mappings) {
            String sourceUri = Mapping.getSource(mapping);
            String targetUri = Mapping.getTarget(mapping);

            /*
             * The NCBO mapping service returns results that are not using the
             * input URI as target or source (2011-07-24). Once this is fixed,
             * activate the assertion again.
             */
            // assert conceptUri.equals(sourceUri) ||
            // conceptUri.equals(targetUri) : "'"
            // + conceptUri
            // + "' does match neither source ('"
            // + sourceUri
            // + "') nor target ('" + targetUri + "')";
            if (!(conceptUri.equals(sourceUri) || conceptUri.equals(targetUri))) {
                continue;
            }

            assert !(conceptUri.equals(sourceUri) && conceptUri
                    .equals(targetUri)) : "'" + conceptUri
                    + "' matches both source and target uri";

            if (sourceUri.equals(conceptUri)) {
                outgoingMappings.add(mapping.getUri());
            } else {
                assert targetUri.equals(conceptUri);
                incomingMappings.add(mapping.getUri());
            }
        }
        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();
        partialProperties.put(Concept.INCOMING_MAPPINGS, incomingMappings);
        partialProperties.put(Concept.OUTGOING_MAPPINGS, outgoingMappings);

        return partialProperties;
    }

    @Override
    public void getMappings(final String ontologyId, final String conceptId,
            final AsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyId != null;
        assert conceptId != null;
        assert callback != null;

        // TODO move code into parser
        Transformer<String, ResourceNeighbourhood> transformer = new Transformer<String, ResourceNeighbourhood>() {
            @Override
            public ResourceNeighbourhood transform(String value)
                    throws Exception {

                List<Resource> mappings = responseParser.parseMapping(value);
                Map<String, Serializable> partialProperties = calculatePartialProperties(
                        Concept.toConceptURI(ontologyId, conceptId), mappings);
                return new ResourceNeighbourhood(partialProperties, mappings);
            }
        };

        urlFetchService.fetchURL(buildUrl(ontologyId, conceptId),
                TransformingAsyncCallback.create(callback, transformer));
    }
}