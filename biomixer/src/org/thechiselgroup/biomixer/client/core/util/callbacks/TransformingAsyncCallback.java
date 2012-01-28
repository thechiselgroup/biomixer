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
package org.thechiselgroup.biomixer.client.core.util.callbacks;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TransformingAsyncCallback<FROM, TO> implements AsyncCallback<FROM> {

    // NOTE: allows creation without specifying generics twice
    public static <FROM, TO> TransformingAsyncCallback<FROM, TO> create(
            AsyncCallback<TO> callback, Transformer<FROM, TO> transformer) {

        return new TransformingAsyncCallback<FROM, TO>(callback, transformer);
    }

    private final AsyncCallback<TO> callback;

    private final Transformer<FROM, TO> transformer;

    protected TransformingAsyncCallback(AsyncCallback<TO> callback,
            Transformer<FROM, TO> transformer) {

        assert transformer != null;
        assert callback != null;

        this.callback = callback;
        this.transformer = transformer;
    }

    @Override
    public void onFailure(Throwable caught) {
        callback.onFailure(caught);
    }

    @Override
    public void onSuccess(FROM result) {
        try {
            callback.onSuccess(transformer.transform(result));
        } catch (Exception e) {
            callback.onFailure(new TransformationException(result, e));
        }
    }

}