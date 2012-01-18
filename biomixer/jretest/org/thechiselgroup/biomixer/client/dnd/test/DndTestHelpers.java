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
package org.thechiselgroup.biomixer.client.dnd.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.thechiselgroup.biomixer.client.core.test.mockito.MockitoGWTBridge;

import com.allen_sauer.gwt.dnd.client.util.DragClientBundle;
import com.allen_sauer.gwt.dnd.client.util.DragClientBundle.DragCssResource;

public final class DndTestHelpers {

    public static void mockDragClientBundle(MockitoGWTBridge bridge) {
        /*
         * client bundle mock will only get created in the first class
         * initialization because it is bound to a constant.
         */
        mock(DragClientBundle.class); // call to trigger class loading
        DragClientBundle clientBundle = bridge
                .getCreatedMock(DragClientBundle.class);
        if (clientBundle != null) {
            DragCssResource cssResource = mock(DragClientBundle.DragCssResource.class);
            when(clientBundle.css()).thenReturn(cssResource);
        }
    }

    private DndTestHelpers() {

    }

}
