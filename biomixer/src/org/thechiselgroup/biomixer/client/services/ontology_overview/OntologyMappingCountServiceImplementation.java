package org.thechiselgroup.biomixer.client.services.ontology_overview;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class OntologyMappingCountServiceImplementation extends
        AbstractXMLWebResourceService implements
        OntologyMappingCountServiceAsync {

    private OntologyMappingCountParser parser;

    public OntologyMappingCountServiceImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologyMappingCountParser parser) {
        super(urlFetchService, urlBuilderFactory);
        this.parser = parser;
    }

    private String buildUrl(List<String> virtualOntologyIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://stagerest.bioontology.org/bioportal/mappings/stats/ontologies?ontologyids=");
        for (int i = 0; i < virtualOntologyIds.size(); i++) {
            sb.append(virtualOntologyIds.get(i));
            if (i < virtualOntologyIds.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a");
        return sb.toString();
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
