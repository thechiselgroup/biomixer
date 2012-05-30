/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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

import org.thechiselgroup.biomixer.client.core.util.callbacks.TransformingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;

import com.google.gwt.user.client.rpc.AsyncCallback;

/*
 * Implementation node: written so it is as independent of the execution
 * environment (client vs. server) as possible.
 */
public abstract class AbstractWebResourceService {

    protected final UrlFetchService urlFetchService;

    protected final UrlBuilderFactory urlBuilderFactory;

    public AbstractWebResourceService(UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory) {

        this.urlFetchService = urlFetchService;
        this.urlBuilderFactory = urlBuilderFactory;
    }

    protected <T> void fetchUrl(final AsyncCallback<T> callback, String url,
            Transformer<String, T> transformer) {

        urlFetchService.fetchURL(url,
                TransformingAsyncCallback.create(callback, transformer));
    }

}