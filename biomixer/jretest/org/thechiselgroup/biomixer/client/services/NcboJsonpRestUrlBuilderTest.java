/*******************************************************************************
 * Copyright 2012 David Rusk
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
package org.thechiselgroup.biomixer.client.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;

public class NcboJsonpRestUrlBuilderTest {

    private UrlBuilderFactory urlBuilderFactory;

    @Ignore("TODO: java implementation of uri encoding")
    @Test
    public void basicUrlNoParams() {
        String url = urlBuilderFactory.createUrlBuilder()
                .path("bioportal/virtual/ontology/1078/all").toString();
        assertThat(
                url,
                equalTo("http://stage.bioontology.org/ajax/jsonp?path=%2Fvirtual%2Fontology%2F1078%2Fall&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"));
    }

    @Ignore("TODO: java implementation of uri encoding")
    @Test
    public void pathAttributeHasParam() {
        String url = urlBuilderFactory
                .createUrlBuilder()
                .path("bioportal/virtual/ontology/1070")
                .uriParameter("conceptid",
                        "http://purl.org/obo/owl/GO#GO_0007569").toString();
        assertThat(
                url,
                equalTo("http://stage.bioontology.org/ajax/jsonp?path=%2Fvirtual%2Fontology%2F1070%3Fconceptid%3Dhttp%253A%252F%252Fpurl.org%252Fobo%252Fowl%252FGO%2523GO_0007569&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"));
    }

    @Before
    public void setUp() {
        urlBuilderFactory = new NcboJsonpRestUrlBuilderFactory();
    }

    @Ignore("TODO: java implementation of uri encoding")
    @Test
    public void urlWithParam() {
        String url = urlBuilderFactory.createUrlBuilder()
                .path("bioportal/virtual/ontology/1516")
                .parameter("light", "1").parameter("norelations", "1")
                .uriParameter("conceptid", "O80-O84.9").toString();
        assertThat(
                url,
                equalTo("http://stage.bioontology.org/ajax/jsonp?path=%2Fvirtual%2Fontology%2F1516%3Fconceptid%3DO80-O84.9%26light%3D1%26norelations%3D1&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"));
    }

}
