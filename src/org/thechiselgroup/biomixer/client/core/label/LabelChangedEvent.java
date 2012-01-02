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
package org.thechiselgroup.biomixer.client.core.label;

import com.google.gwt.event.shared.GwtEvent;

public class LabelChangedEvent extends GwtEvent<LabelChangedEventHandler> {

    public static final GwtEvent.Type<LabelChangedEventHandler> TYPE = new GwtEvent.Type<LabelChangedEventHandler>();

    private final String newLabel;

    private final String oldLabel;

    private final HasLabel source;

    public LabelChangedEvent(HasLabel source, String newLabel, String oldLabel) {
        assert source != null;

        this.source = source;
        this.newLabel = newLabel;
        this.oldLabel = oldLabel;
    }

    @Override
    protected void dispatch(LabelChangedEventHandler handler) {
        handler.onLabelChanged(this);
    }

    @Override
    public GwtEvent.Type<LabelChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public String getNewLabel() {
        return newLabel;
    }

    public String getOldLabel() {
        return oldLabel;
    }

    @Override
    public HasLabel getSource() {
        return source;
    }

}