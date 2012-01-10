package org.thechiselgroup.biomixer.client.services.ontology_version;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class OntologyVersionServiceAsyncClientImplementation extends
        AbstractXMLWebResourceService implements OntologyVersionServiceAsync {

    private final OntologyVersionParser parser;

    @Inject
    public OntologyVersionServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory, OntologyVersionParser parser) {
        super(urlFetchService, urlBuilderFactory);

        this.parser = parser;
    }

    private String buildUrl(String virtualOntologyId) {
        UrlBuilder urlBuilder = urlBuilderFactory.createUrlBuilder();
        String path = "/bioportal/virtual/ontology" + virtualOntologyId + "/";
        urlBuilder.setPath(path);
        return urlBuilder.buildString();
    }

    @Override
    public void getOntologyVersionId(final String virtualOntologyId,
            final AsyncCallback<String> callback) {

        String url = buildUrl(virtualOntologyId);

        fetchUrl(callback, url, new Transformer<String, String>() {
            @Override
            public String transform(String xmlText) throws Exception {
                return parser.parse(virtualOntologyId, xmlText);
            }

        });

    }

}
