package org.thechiselgroup.biomixer.client.services.rootpath;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourcePath;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class RootPathServiceAsyncClientImplementation extends
        AbstractXMLWebResourceService implements RootPathServiceAsync {

    private final RootPathParser resultParser;

    @Inject
    public RootPathServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory, RootPathParser resultParser) {

        super(urlFetchService, urlBuilderFactory);

        this.resultParser = resultParser;
    }

    private String buildUrl(String conceptId, String ontologyVersionId) {
        return urlBuilderFactory.createUrlBuilder()
                .path("/bioportal/path/" + ontologyVersionId + "/")
                .parameter("source", conceptId).parameter("target", "root")
                .toString();
    }

    @Override
    public void findPathToRoot(final String virtualOntologyId,
            final String ontologyVersionId, final String conceptId,
            final AsyncCallback<ResourcePath> callback) {

        String url = buildUrl(conceptId, ontologyVersionId);
        fetchUrl(callback, url, new Transformer<String, ResourcePath>() {
            @Override
            public ResourcePath transform(String xmlText) throws Exception {
                return resultParser.parse(ontologyVersionId, virtualOntologyId,
                        conceptId, xmlText);
            }

        });

    }

}
