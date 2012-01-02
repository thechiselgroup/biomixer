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
package org.thechiselgroup.biomixer.client.visualization_component.timeline;

import java.util.Arrays;

import com.google.gwt.event.shared.GwtEvent;

public class TimelineInteractionEvent extends
        GwtEvent<TimelineInteractionEventHandler> {

    public static final GwtEvent.Type<TimelineInteractionEventHandler> TYPE = new GwtEvent.Type<TimelineInteractionEventHandler>();

    private TimeLineWidget timeline;

    private int sourceBand;

    // TODO use enum
    private String interaction;

    private BandInformation[] bandInformations;

    public TimelineInteractionEvent(TimeLineWidget timeline, int sourceBand,
            String interaction, BandInformation[] bandInformations) {
        this.timeline = timeline;
        this.sourceBand = sourceBand;
        this.interaction = interaction;
        this.bandInformations = bandInformations;
    }

    @Override
    protected void dispatch(TimelineInteractionEventHandler handler) {
        handler.onInteraction(this);
    }

    @Override
    public GwtEvent.Type<TimelineInteractionEventHandler> getAssociatedType() {
        return TYPE;
    }

    public BandInformation[] getBandInformations() {
        return bandInformations;
    }

    public String getInteraction() {
        return interaction;
    }

    public int getSourceBand() {
        return sourceBand;
    }

    public TimeLineWidget getTimeline() {
        return timeline;
    }

    @Override
    public String toString() {
        return "TimelineInteractionEvent [timeline=" + timeline
                + ", sourceBand=" + sourceBand + ", interaction=" + interaction
                + ", bandInformations=" + Arrays.toString(bandInformations)
                + "]";
    }

}