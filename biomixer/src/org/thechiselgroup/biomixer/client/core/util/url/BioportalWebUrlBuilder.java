/*******************************************************************************
 * Copyright 2012 Eric Verbeek
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
package org.thechiselgroup.biomixer.client.core.util.url;

/**
 * This class serves as a utility to generate URLs for various types of
 * BioPortal Web URLs. This is not related to the BioPortal REST services.
 * 
 * @author everbeek
 * 
 */
public class BioportalWebUrlBuilder {

    public static final String PROTOCOL = "http";

    public static final String SERVER = "bioportal.bioontology.org";

    public static final String ONTOLOGY_SUMMARY_PATH = "/ontologies/";

    public static final String ONTOLOGY_SUMMARY_P_PARAM = "p";

    public static final String ONTOLOGY_SUMMARY_P_PARAM_VALUE = "summary";

    private static UrlBuilder baseUrlBuilder() {
        return new DefaultUrlBuilder().host(SERVER).protocol(PROTOCOL);
    }

    public static UrlBuilder generateOntologySummaryUrl(String virtualOntologyId) {
        assert (null != virtualOntologyId);
        UrlBuilder builder = baseUrlBuilder();
        builder.path(ONTOLOGY_SUMMARY_PATH + virtualOntologyId).parameter(
                ONTOLOGY_SUMMARY_P_PARAM, ONTOLOGY_SUMMARY_P_PARAM_VALUE);
        return builder;
    }

}