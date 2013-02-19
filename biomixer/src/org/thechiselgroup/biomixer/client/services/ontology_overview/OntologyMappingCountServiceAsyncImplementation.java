package org.thechiselgroup.biomixer.client.services.ontology_overview;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class OntologyMappingCountServiceAsyncImplementation extends
        AbstractWebResourceService implements OntologyMappingCountServiceAsync {

    private OntologyNeighbourhoodMappingCountJSONParser specifiedNeighbourhoodCountParser;

    private OntologyMappingCountJSONParser mappingsForCentralOntologyCountParser;

    /**
     * Note that the rest call used here is currently only available at
     * stagerest. Change it over when it has been made available on the
     * production server.
     * 
     * @param urlFetchService
     * @param urlBuilderFactory
     * @param parser
     */
    @Inject
    public OntologyMappingCountServiceAsyncImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologyMappingCountJSONParser parser,
            OntologyNeighbourhoodMappingCountJSONParser parser2) {
        super(urlFetchService, urlBuilderFactory);
        this.mappingsForCentralOntologyCountParser = parser;
        this.specifiedNeighbourhoodCountParser = parser2;
    }

    private String buildUrlForSpecifiedNeighbourhoodQuery(
            Iterable<String> virtualOntologyIds) {
        StringBuilder sb = new StringBuilder();
        for (String id : virtualOntologyIds) {
            sb.append(id).append(",");
        }
        // Stage Rest service only, at the moment. No on-line documentation of
        // this.
        UrlBuilder parameter = urlBuilderFactory.createUrlBuilder()
                .path("/mappings/stats/ontologies")
                .parameter("ontologyids", sb.toString());
        return parameter.toString();
    }

    @Override
    public void getMappingCounts(Iterable<String> virtualOntologyIds,
            AsyncCallback<TotalMappingCount> callback) {
        String url = buildUrlForSpecifiedNeighbourhoodQuery(virtualOntologyIds);
        fetchUrl(callback, url, new Transformer<String, TotalMappingCount>() {
            @Override
            public TotalMappingCount transform(String responseText)
                    throws Exception {
                TotalMappingCount parse = specifiedNeighbourhoodCountParser
                        .parse(responseText);

                return parse;
            }

        });
    }

    private String buildUrlForCentralOntologyQuery(String virtualOntologyId) {
        // Stage Rest service only, at the moment. No on-line documentation of
        // this.
        UrlBuilder parameter = urlBuilderFactory.createUrlBuilder().path(
                "/virtual/mappings/stats/ontologies/" + virtualOntologyId);
        return parameter.toString();
    }

    @Override
    public void getAllMappingCountsForCentralOntology(
            final String virtualOntologyId,
            AsyncCallback<TotalMappingCount> callback) {
        String url = buildUrlForCentralOntologyQuery(virtualOntologyId);
        fetchUrl(callback, url, new Transformer<String, TotalMappingCount>() {
            @Override
            public TotalMappingCount transform(String responseText)
                    throws Exception {
                TotalMappingCount parse = mappingsForCentralOntologyCountParser
                        .parse(responseText);

                return parse;
            }

        });
    }
}
