package org.thechiselgroup.biomixer.client.services.ontology_overview;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MappingCountServiceTest {

    private static final String URL = "http://stagerest.bioontology.org/bioportal/mappings/stats/ontologies?ontologyids=1009,1099,1032&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a";

    @Mock
    private UrlFetchService urlFetchService;

    @Mock
    private UrlBuilderFactory urlBuilderFactory;

    @Mock
    private OntologyMappingCountParser responseParser;

    private UrlBuilder urlBuilder;

    private OntologyMappingCountServiceImplementation underTest;

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new OntologyMappingCountServiceImplementation(
                urlFetchService, urlBuilderFactory, responseParser);

        this.urlBuilder = Mockito.spy(new UrlBuilder());

        when(urlBuilderFactory.createUrlBuilder()).thenReturn(urlBuilder);
        when(urlBuilder.toString()).thenReturn(URL);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void urlFetched() {
        List<String> ontologyIds = new ArrayList<String>();
        ontologyIds.add("1009");
        ontologyIds.add("1099");
        ontologyIds.add("1032");

        underTest.getMappingCounts(ontologyIds, mock(AsyncCallback.class));

        verify(urlFetchService, times(1)).fetchURL(eq(URL),
                any(AsyncCallback.class));
    }
}
