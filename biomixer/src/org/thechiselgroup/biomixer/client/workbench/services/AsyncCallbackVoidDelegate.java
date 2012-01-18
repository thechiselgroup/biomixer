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
package org.thechiselgroup.biomixer.client.workbench.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncCallbackVoidDelegate<T> implements AsyncCallback<T> {

    private AsyncCallback<Void> delegate;

    public AsyncCallbackVoidDelegate(AsyncCallback<Void> delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    @Override
    public void onFailure(Throwable caught) {
        delegate.onFailure(caught);
    }

    @Override
    public void onSuccess(T result) {
        delegate.onSuccess(null);
    }
}
