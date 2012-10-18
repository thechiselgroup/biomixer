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
package org.thechiselgroup.biomixer.client.services.term;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;
import org.thechiselgroup.biomixer.client.core.util.url.DefaultUrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TermServiceImplementationTest {

    private static final String URL = "test-url-string";

    @Mock
    private UrlFetchService urlFetchService;

    private TermServiceImplementation underTest;

    @Mock
    private TermWithoutRelationshipsJsonParser responseParser;

    @Mock
    private OntologyNameServiceAsync ontologyNameService;

    @Mock
    private UrlBuilderFactory urlBuilderFactory;

    private DefaultUrlBuilder urlBuilder;

    @Ignore("TODO: introduction of the name service causes no argument to be captured")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void returnParsedConcept() throws Exception {
        String ontologyId = "1ontologyId1";
        String conceptId = "1conceptId1";
        String xmlResultStub = "xmlResultStub";

        AsyncCallback callback = mock(AsyncCallback.class);

        Resource result = new Resource();

        when(responseParser.parseConcept(ontologyId, xmlResultStub))
                .thenReturn(result);

        ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor
                .forClass(AsyncCallback.class);
        doNothing().when(urlFetchService).fetchURL(eq(URL), captor.capture());

        underTest.getBasicInformation(ontologyId, conceptId, callback);

        AsyncCallback<String> xmlResultCallback = captor.getValue();
        xmlResultCallback.onSuccess(xmlResultStub);

        verify(callback, times(1)).onSuccess(result);
    }

    @Before
    public void setUp() {
        MockitoGWTBridge.setUp();
        MockitoAnnotations.initMocks(this);

        underTest = new TermServiceImplementation(urlFetchService,
                urlBuilderFactory, ontologyNameService, responseParser);

        this.urlBuilder = Mockito.spy(new DefaultUrlBuilder());

        when(urlBuilderFactory.createUrlBuilder()).thenReturn(urlBuilder);
        when(urlBuilder.toString()).thenReturn(URL);
    }

    @Ignore("TODO: introduction of the name service causes verification of the url fetch to fail")
    @SuppressWarnings("unchecked")
    @Test
    public void urlFetched() {
        String ontologyId = "1ontologyId1";
        String conceptId = "1conceptId1";

        underTest.getBasicInformation(ontologyId, conceptId,
                mock(AsyncCallback.class));

        verify(urlFetchService, times(1)).fetchURL(eq(URL),
                any(AsyncCallback.class));
    }
}