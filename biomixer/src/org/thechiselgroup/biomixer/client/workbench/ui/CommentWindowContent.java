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
package org.thechiselgroup.biomixer.client.workbench.ui;

import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.Persistable;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.dnd.windows.AbstractWindowContent;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class CommentWindowContent extends AbstractWindowContent implements
        Persistable {

    private static final String CSS_NOTE = "note";

    private static final String MEMENTO_COMMENT = CSS_NOTE;

    /**
     * Note padding in PX.
     * 
     * TODO replace with dynamic lookup.
     */
    private static final int PADDING = 5;

    private TextArea commentArea;

    /**
     * Wrapper panel to deal with resizing issues (the note has a border and
     * padding which interferes with setting the size etc).
     */
    private SimplePanel commentPanel;

    public CommentWindowContent() {
        super("Comment", MEMENTO_COMMENT);
    }

    @Override
    public Widget asWidget() {
        return commentPanel;
    }

    @Override
    public void init() {
        super.init();

        commentArea = new TextArea();
        commentArea.addStyleName(CSS_NOTE);

        commentPanel = new SimplePanel(commentArea) {
            @Override
            public void setPixelSize(int width, int height) {
                super.setPixelSize(width, height);
                commentArea.setPixelSize(width - 2 * PADDING, height - 2 * PADDING);
            }
        };
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        commentArea.setText((String) state.getValue(MEMENTO_COMMENT));
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento state = new Memento();
        state.setValue(MEMENTO_COMMENT, commentArea.getValue());
        return state;
    }
}