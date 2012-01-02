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
package org.thechiselgroup.biomixer.client.workbench.ui.messages;

import static org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants.DEFAULT;

import org.thechiselgroup.biomixer.client.core.ui.shade.ShadeManager;
import org.thechiselgroup.biomixer.client.core.util.RemoveHandle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ShadeMessageManager implements MessageManager {

    private MessageManager delegate;

    private ShadeManager shadeManager;

    @Inject
    public ShadeMessageManager(@Named(DEFAULT) MessageManager delegate,
            ShadeManager shadeManager) {

        assert delegate != null;
        assert shadeManager != null;

        this.delegate = delegate;
        this.shadeManager = shadeManager;
    }

    @Override
    public RemoveHandle showMessage(String message) {
        final RemoveHandle shadeHandle = shadeManager.showShade();
        final RemoveHandle messageHandle = delegate.showMessage(message);

        return new RemoveHandle() {

            @Override
            public void remove() {
                messageHandle.remove();
                shadeHandle.remove();
            }
        };
    }

}
