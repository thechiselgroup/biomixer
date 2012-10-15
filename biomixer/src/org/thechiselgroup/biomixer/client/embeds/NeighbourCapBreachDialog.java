/*******************************************************************************
 * Copyright 2009, 2010 Eric Verbeek
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
package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.ui.dialog.AbstractDialog;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class NeighbourCapBreachDialog extends AbstractDialog {

    private static final String CSS_FEEDBACK_MESSAGE = "feedback-message";

    private final String messageBeforeNum = "There are "; // "The concept neighbourhood includes ";

    private final String messageAfterNum = " concepts to load. Retrieving these concepts may take several minutes, and things may appear to be frozen. Shall we load only "; // " concepts, which may take long to load and clutter your graph view. The screen may even appear to be frozen.<br/>Is it ok if we limit the number of neighbour concepts rendered to ";

    private final String messageAfterMaximum = " concepts?";

    private final String message;

    private final String title = "Attention";

    private final String header = "Very Large Concept Neighbourhood";

    private int numberOfNeighbours;

    private int maxDefault;

    public NeighbourCapBreachDialog(int numberOfNeighbours, int maxDefault) {
        this.numberOfNeighbours = numberOfNeighbours;
        this.maxDefault = maxDefault;
        this.message = messageBeforeNum + numberOfNeighbours + messageAfterNum
                + maxDefault + messageAfterMaximum;
        this.width = 400;
        this.height = 200;
    }

    @Override
    public Widget getContent() {
        VerticalPanel panel = new VerticalPanel();

        HTML label = new HTML(message);
        label.addStyleName(CSS_FEEDBACK_MESSAGE);
        panel.add(label);

        return panel;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getOkayButtonLabel() {
        // return "Accept Neighbour Cap";
        return "Load only " + maxDefault + " concepts";
    }

    @Override
    public String getCancelButtonLabel() {
        // return "Reject Cap, Expand All Neighbours";
        return "Load all " + numberOfNeighbours + " concepts";
    }

    @Override
    public String getWindowTitle() {
        return title;
    }

    @Override
    public void okay() {
        // Nothing. This design...doesn't help with dialogs that are triggering
        // serious work.
        // But the design sure works for things like triggering a
        // SendFeedbackCommand()
    }

    @Override
    public void cancel() {
        // do nothing.
    }
}