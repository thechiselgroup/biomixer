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
package org.thechiselgroup.biomixer.client.core.util.url;

/**
 * Creates URL builders, which can be pre-configured (e.g. by supplying API key
 * parameters or server and protocol settings).
 * 
 * @author Lars Grammel
 */
public interface UrlBuilderFactory {

    String BIO_MIXER_API_KEY = "6700f7bc-5209-43b6-95da-44336cbc0a3a";

    UrlBuilder createUrlBuilder();

    void setUserApiKey(String apiKey);

}