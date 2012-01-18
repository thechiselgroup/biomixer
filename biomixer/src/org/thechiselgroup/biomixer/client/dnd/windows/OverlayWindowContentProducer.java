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
package org.thechiselgroup.biomixer.client.dnd.windows;

/**
 * Creates {@link WindowContent} objects using registered
 * {@link WindowContentFactory}. If no factory is requested for a given type,
 * the creation is deferred to another {@link WindowContentProducer} (Chain of
 * responsibility design pattern).
 * 
 * @author Lars Grammel
 */
public class OverlayWindowContentProducer implements WindowContentProducer {

    private DefaultWindowContentProducer defaultProducer = new DefaultWindowContentProducer();

    private WindowContentProducer delegate;

    public OverlayWindowContentProducer(WindowContentProducer delegate) {
        assert delegate != null;

        this.delegate = delegate;
    }

    @Override
    public WindowContent createWindowContent(String contentType) {
        assert contentType != null;

        if (defaultProducer.containsWindowContentFactory(contentType)) {
            return defaultProducer.createWindowContent(contentType);
        }

        return delegate.createWindowContent(contentType);
    }

    public void register(String contentType,
            WindowContentFactory windowContentFactory) {
        defaultProducer.register(contentType, windowContentFactory);
    }

}