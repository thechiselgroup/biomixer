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
package org.thechiselgroup.biomixer.client.core.util;

import java.util.ArrayList;
import java.util.List;

public class DisposableComposite implements Disposable {

    private List<Disposable> disposables = new ArrayList<Disposable>();

    public boolean addDisposable(Disposable e) {
        return disposables.add(e);
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }

        for (Disposable disposable : disposables) {
            disposable.dispose();
        }

        disposables = null;
    }

    private boolean isDisposed() {
        return disposables == null;
    }

}
