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

    @Ignore("TODO implement")
    @Test
    public void basicUrlNoParams() {
        String url = urlBuilderFactory.createUrlBuilder()
                .path("bioportal/virtual/ontology/1078/all").toString();
        assertThat(
                url,
                equalTo("http://stage.bioontology.org/ajax/jsonp?path=%2Fvirtual%2Fontology%2F1078%2Fall&apikey=6700f7bc-5209-43b6-95da-44336cbc0a3a"));
    }

    @Before
    public void setUp() {
        urlBuilderFactory = new NcboJsonpRestUrlBuilderFactory();
    }

}
