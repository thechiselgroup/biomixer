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
package org.thechiselgroup.biomixer.client.workbench;

import org.thechiselgroup.biomixer.client.dnd.windows.OverlayWindowContentProducer;
import org.thechiselgroup.biomixer.client.dnd.windows.WindowContentProducer;
import org.thechiselgroup.biomixer.client.workbench.init.WorkbenchInitializer;
import org.thechiselgroup.biomixer.client.workbench.ui.HelpWindowContentFactory;
import org.thechiselgroup.biomixer.client.workbench.ui.CommentWindowContentFactory;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DefaultWindowContentProducerProvider implements
        Provider<WindowContentProducer> {

    @Inject
    private ViewWindowContentProducer viewProducer;

    @Override
    public WindowContentProducer get() {
        OverlayWindowContentProducer contentProducer = new OverlayWindowContentProducer(
                viewProducer);

        contentProducer.register(WorkbenchInitializer.WINDOW_CONTENT_HELP,
                new HelpWindowContentFactory());
        contentProducer.register(WorkbenchInitializer.WINDOW_CONTENT_COMMENT,
                new CommentWindowContentFactory());

        return contentProducer;
    }

}