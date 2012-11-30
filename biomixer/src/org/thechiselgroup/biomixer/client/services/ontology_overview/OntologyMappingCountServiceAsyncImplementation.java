package org.thechiselgroup.biomixer.client.services.ontology_overview;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.NcboJsonpStageRestUrlBuilderFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class OntologyMappingCountServiceAsyncImplementation extends
        AbstractWebResourceService implements OntologyMappingCountServiceAsync {

    private OntologyMappingCountJSONParser countParser;

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
            NcboJsonpStageRestUrlBuilderFactory urlBuilderFactory,
            OntologyMappingCountJSONParser parser) {
        super(urlFetchService, urlBuilderFactory);
        this.countParser = parser;
    }

    private String buildUrl(Iterable<String> virtualOntologyIds) {
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
        // Stage Rest service only, at the moment. No on-line documentation of
        // this.
        String url = buildUrl(virtualOntologyIds);
        fetchUrl(callback, url, new Transformer<String, TotalMappingCount>() {
            @Override
            public TotalMappingCount transform(String responseText)
                    throws Exception {
                TotalMappingCount parse = countParser.parse(responseText);

                return parse;
            }

        });
    }
}
