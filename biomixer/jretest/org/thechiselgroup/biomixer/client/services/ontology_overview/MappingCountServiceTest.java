package org.thechiselgroup.biomixer.client.services.ontology_overview;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.url.DefaultUrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.NcboStageRestUrlBuilderFactory;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MappingCountServiceTest {

    private static final String URL = "http://stagerest.bioontology.org/bioportal/mappings/stats/ontologies?ontologyids=1009,1099&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a";

    @Mock
    private UrlFetchService urlFetchService;

    @Mock
    private NcboStageRestUrlBuilderFactory urlBuilderFactory;

    @Mock
    private OntologyMappingCountParser responseParser;

    private UrlBuilder urlBuilder;

    private OntologyMappingCountServiceAsyncImplementation underTest;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TotalMappingCount doGetMappingCount(List<String> ontologyIds,
            TotalMappingCount parsedMappingCount) throws Exception {
        String xmlResultStub = "xmlResultStub";

        AsyncCallback callback = mock(AsyncCallback.class);

        when(responseParser.parse(xmlResultStub))
                .thenReturn(parsedMappingCount);

        ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(urlFetchService).fetchURL(eq(URL), captor.capture());

        underTest.getMappingCounts(ontologyIds, callback);

        AsyncCallback<String> xmlResultCallback = captor.getValue();

        ArgumentCaptor<TotalMappingCount> captor2 = ArgumentCaptor
                .forClass(TotalMappingCount.class);
        doNothing().when(callback).onSuccess(captor2.capture());

        xmlResultCallback.onSuccess(xmlResultStub);

        return captor2.getValue();
    }

    @Test
    public void returnParsedMappingCount() throws Exception {
        List<String> ontologyIds = new ArrayList<String>();
        ontologyIds.add("1009");
        ontologyIds.add("1099");

        OntologyMappingCount testMappingCount1 = new OntologyMappingCount(
                "1009", "1099", "11");
        OntologyMappingCount testMappingCount2 = new OntologyMappingCount(
                "1099", "1009", "11");

        TotalMappingCount testMappingCount = new TotalMappingCount();
        testMappingCount.add(testMappingCount1);
        testMappingCount.add(testMappingCount2);

        TotalMappingCount result = doGetMappingCount(ontologyIds,
                testMappingCount);

        assertThat(result, equalTo(testMappingCount));

        assertThat(result.getMappingCount("1009", "1099"), equalTo(22));

    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new OntologyMappingCountServiceAsyncImplementation(
                urlFetchService, urlBuilderFactory, responseParser);

        this.urlBuilder = Mockito.spy(new DefaultUrlBuilder());

        when(urlBuilderFactory.createUrlBuilder()).thenReturn(urlBuilder);
        when(urlBuilder.toString()).thenReturn(URL);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void urlFetched() {
        List<String> ontologyIds = new ArrayList<String>();
        ontologyIds.add("1009");
        ontologyIds.add("1099");

        underTest.getMappingCounts(ontologyIds, mock(AsyncCallback.class));

        verify(urlFetchService, times(1)).fetchURL(eq(URL),
                any(AsyncCallback.class));
    }
}
