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
            Iterable<String> ontologyAcronyms) {
        StringBuilder sb = new StringBuilder();
        for (String id : ontologyAcronyms) {
            sb.append(id).append(",");
        }
        // Stage Rest service only, at the moment. No on-line documentation of
        // this.
        // XXX May not have support in the new API. If there was only stage
        // support
        // for this before, perhaps it was never used by us. Look into that.
        UrlBuilder parameter = urlBuilderFactory.createUrlBuilder()
                .path("/mappings/stats/ontologies")
                .parameter("ontologyids", sb.toString());
        return parameter.toString();
    }

    @Override
    public void getMappingCounts(Iterable<String> ontologyAcronyms,
            AsyncCallback<TotalMappingCount> callback) {
        String url = buildUrlForSpecifiedNeighbourhoodQuery(ontologyAcronyms);
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

    private String buildUrlForCentralOntologyQuery(String ontologyAcronym) {
        // New API Target:
        // "http://stagedata.bioontology.org/mappings/statistics/ontologies/"+centralOntologyAcronym+"/?format=jsonp&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"+"&callback=?";

        // Stage Rest service only, at the moment. No on-line documentation of
        // this.
        // UrlBuilder parameter = urlBuilderFactory.createUrlBuilder().path(
        // "/virtual/mappings/stats/ontologies/" + virtualOntologyId);
        UrlBuilder parameter = urlBuilderFactory.createUrlBuilder().path(
                "/mappings/statistics/ontologies/" + ontologyAcronym);
        return parameter.toString();
    }

    @Override
    public void getAllMappingCountsForCentralOntology(
            final String ontologyAcronym,
            AsyncCallback<TotalMappingCount> callback) {
        String url = buildUrlForCentralOntologyQuery(ontologyAcronym);
        fetchUrl(callback, url, new Transformer<String, TotalMappingCount>() {
            @Override
            public TotalMappingCount transform(String responseText)
                    throws Exception {
                mappingsForCentralOntologyCountParser
                        .setSourceOntologyAcronym(ontologyAcronym);
                TotalMappingCount parse = mappingsForCentralOntologyCountParser
                        .parse(responseText);

                return parse;
            }

        });
    }
}
