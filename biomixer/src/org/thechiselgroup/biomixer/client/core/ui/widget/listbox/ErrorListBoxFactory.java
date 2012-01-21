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
package org.thechiselgroup.biomixer.client.core.ui.widget.listbox;

import org.thechiselgroup.biomixer.client.core.error_handling.ThrowableCaught;
import org.thechiselgroup.biomixer.client.core.error_handling.ThrowablesContainer;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;

public class ErrorListBoxFactory {

    public static ListBoxControl<ThrowableCaught> createErrorBox(
            ThrowablesContainer throwablesContainer, ListBoxPresenter presenter) {

        ErrorListBoxControl listBoxControl = new ErrorListBoxControl(presenter,
                new Transformer<ThrowableCaught, String>() {
                    @Override
                    public String transform(ThrowableCaught throwableCaught) {
                        return throwableCaught.toString();
                    }
                });
        throwablesContainer.addListener(listBoxControl);
        return listBoxControl;
    }

}
