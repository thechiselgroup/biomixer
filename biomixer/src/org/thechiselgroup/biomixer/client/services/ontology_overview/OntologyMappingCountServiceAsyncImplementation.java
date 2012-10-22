package org.thechiselgroup.biomixer.client.services.ontology_overview;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.NcboStageRestUrlBuilderFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class OntologyMappingCountServiceAsyncImplementation extends
        AbstractWebResourceService implements OntologyMappingCountServiceAsync {

    private OntologyMappingCountParser parser;

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
            NcboStageRestUrlBuilderFactory urlBuilderFactory,
            OntologyMappingCountParser parser) {
        super(urlFetchService, urlBuilderFactory);
        this.parser = parser;
    }

    private String buildUrl(List<String> virtualOntologyIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < virtualOntologyIds.size(); i++) {
            sb.append(virtualOntologyIds.get(i));
        }
        return urlBuilderFactory.createUrlBuilder()
                .path("/mappings/stats/ontologies")
                .parameter("ontologyids", sb.toString()).toString();
    }

    @Override
    public void getMappingCounts(List<String> virtualOntologyIds,
            AsyncCallback<TotalMappingCount> callback) {

        String url = buildUrl(virtualOntologyIds);
        fetchUrl(callback, url, new Transformer<String, TotalMappingCount>() {
            @Override
            public TotalMappingCount transform(String xmlText) throws Exception {
                return parser.parse(xmlText);
            }

        });

    }
}
