/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;

/**
 * {@linkplain http://code.google.com/p/simile-widgets/wiki/Timeline}
 * {@linkplain http ://code.google.com/p/simile-widgets/wiki/Timeline_BandClass}
 */
public class JsTimeLine extends JavaScriptObject {

    // TODO expose # of bands
    // @formatter:off
    public static native JsTimeLine create(Element element,
            JsTimeLineEventSource eventSource, String dateAsString, 
            String mainBandWidth, String overviewBandWidth) /*-{
		var theme = $wnd.Timeline.ClassicTheme.create();
		theme.event.bubble.width = 350;
		theme.event.bubble.height = 300;

		var bandInfos = [ $wnd.Timeline.createBandInfo({
			startsOn : dateAsString,
			width : mainBandWidth,
			intervalUnit : $wnd.SimileAjax.DateTime.DAY,
			intervalPixels : 50,
			eventSource : eventSource,
			zoomIndex : 7,
			zoomSteps : new Array({
				pixelsPerInterval : 280,
				unit : $wnd.SimileAjax.DateTime.HOUR
			}, {
				pixelsPerInterval : 140,
				unit : $wnd.SimileAjax.DateTime.HOUR
			}, {
				pixelsPerInterval : 70,
				unit : $wnd.SimileAjax.DateTime.HOUR
			}, {
				pixelsPerInterval : 35,
				unit : $wnd.SimileAjax.DateTime.HOUR
			}, {
				pixelsPerInterval : 400,
				unit : $wnd.SimileAjax.DateTime.DAY
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.DAY
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.DAY
			}, {
				pixelsPerInterval : 50,
				unit : $wnd.SimileAjax.DateTime.DAY
			}, {
				pixelsPerInterval : 400,
				unit : $wnd.SimileAjax.DateTime.MONTH
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.MONTH
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.MONTH
			}, {
				pixelsPerInterval : 400,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 50,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 400,
				unit : $wnd.SimileAjax.DateTime.DECADE
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.DECADE
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.DECADE
			}, {
				pixelsPerInterval : 50,
				unit : $wnd.SimileAjax.DateTime.DECADE
			})
		}), $wnd.Timeline.createBandInfo({
			startsOn : dateAsString,
			width : overviewBandWidth,
			intervalUnit : $wnd.SimileAjax.DateTime.MONTH,
			intervalPixels : 200,
			showEventText : false,
			trackHeight : 0.5,
			trackGap : 0.2,
			eventSource : eventSource,
			overview : true,
			zoomIndex : 1,
			zoomSteps : new Array({
				pixelsPerInterval : 400,
				unit : $wnd.SimileAjax.DateTime.MONTH
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.MONTH
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.MONTH
			}, {
				pixelsPerInterval : 400,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 40,
				unit : $wnd.SimileAjax.DateTime.YEAR
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.DECADE
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.DECADE
			}, {
				pixelsPerInterval : 40,
				unit : $wnd.SimileAjax.DateTime.DECADE
			}, {
				pixelsPerInterval : 200,
				unit : $wnd.SimileAjax.DateTime.CENTURY
			}, {
				pixelsPerInterval : 100,
				unit : $wnd.SimileAjax.DateTime.CENTURY
			})
		}) ];

		bandInfos[1].syncWith = 0;
		bandInfos[1].highlight = true;

		return $wnd.Timeline.create(element, bandInfos,
				$wnd.Timeline.HORIZONTAL);
    }-*/;
    // @formatter:on

    protected JsTimeLine() {
    }

    /**
     * Replaces the _showBubble function in the event painter with a null
     * functions, thus preventing info bubbles from being shown.
     */
    public final native void disableBubbles() /*-{
                                              for ( var i = 0; i < this.getBandCount(); i++) {
                                              var eventPainter = this.getBand(i)._eventPainter;
                                              eventPainter._showBubble = function(x, y, evt) {
                                              };
                                              }
                                              }-*/;

    /**
     * Returns getCenterVisibleDate() from the main (first) band as GMT String
     * in a form similar to "Fri, 29 Sep 2000 06:23:54 GMT"
     * ("EEE, d MMM yyyy HH:mm:ss Z")
     */
    public final native String getCenterVisibleDateAsGMTString() /*-{
                                                                 // TODO change if bands are not synchronized any more
                                                                 return this.getBand(0).getCenterVisibleDate().toGMTString();
                                                                 }-*/;

    public final String getEventElementID(int bandIndex, String elementType,
            JsTimeLineEvent event) {
        /*
         * see Timeline.EventUtils.encodeEventElID = function(timeline, band,
         * elType, evt)
         */
        return elementType + "-tl-" + getTimeLineID() + "-" + bandIndex + "-"
                + event.getID();
    }

    /**
     * Returns getMaxVisibleDate() from the band as GMT String in a form similar
     * to "Fri, 29 Sep 2000 06:23:54 GMT" ("EEE, d MMM yyyy HH:mm:ss Z")
     */
    public final native String getMaxVisibleDateAsGMTString(int band) /*-{
                                                                      return this.getBand(band).getMaxVisibleDate().toGMTString();
                                                                      }-*/;

    public final native String getMaxVisibleDateAsGMTString(int band,
            JavaScriptObject centerDate) /*-{
                                         return this.getBand(band).getMaxVisibleDateForCenter(centerDate)
                                         .toGMTString();
                                         }-*/;

    /**
     * Returns getMinVisibleDate() from the band as GMT String in a form similar
     * to "Fri, 29 Sep 2000 06:23:54 GMT" ("EEE, d MMM yyyy HH:mm:ss Z")
     */
    public final native String getMinVisibleDateAsGMTString(int band) /*-{
                                                                      return this.getBand(band).getMinVisibleDate().toGMTString();
                                                                      }-*/;

    public final native String getMinVisibleDateAsGMTString(int band,
            JavaScriptObject centerDate) /*-{
                                         return this.getBand(band).getMinVisibleDateForCenter(centerDate)
                                         .toGMTString();
                                         }-*/;

    public final native int getTimeLineID() /*-{
                                            return this.timelineID;
                                            }-*/;

    /**
     * Returns the zoom index of a band. What time interval the zoom index
     * refers to depends on the band (defined in
     * {@link #create(Element, JsTimeLineEventSource, String)}).
     */
    public final native int getZoomIndex(int bandIndex) /*-{
                                                        return this.getBand(bandIndex)._zoomIndex;
                                                        }-*/;

    public final native void layout() /*-{
                                      this.layout();
                                      }-*/;

    public final native void paint() /*-{
                                     this.paint();
                                     }-*/;

    // @formatter:off
    public final native void registerInteractionHandler(JsTimelineInteractionCallback callback) /*-{
		var handler = function(interaction, band, newCenterDate) {
			var bandIndex = band.getIndex();
			if (!newCenterDate) {
				callback.@org.thechiselgroup.biomixer.client.visualization_component.timeline.JsTimelineInteractionCallback::onInteraction(Ljava/lang/String;I)(interaction, bandIndex);
			} else {
				callback.@org.thechiselgroup.biomixer.client.visualization_component.timeline.JsTimelineInteractionCallback::onInteraction(Ljava/lang/String;ILcom/google/gwt/core/client/JavaScriptObject;)(interaction, bandIndex, newCenterDate);
			}
		}

		for ( var i = 0; i < this.getBandCount(); i++) {
			this.getBand(i).addInteractionHandler(handler);
		}
    }-*/;
    // @formatter:on

    // @formatter:off
    public final native void registerPaintListener(JsTimelinePaintCallback callback) /*-{
		var listener = function(band, operation, event, elements) {
			if ("paintedEvent" == operation) {
				var bandIndex = band.getIndex();
				var timeline = band.getTimeline();
				callback.@org.thechiselgroup.biomixer.client.visualization_component.timeline.JsTimelinePaintCallback::eventPainted(ILorg/thechiselgroup/biomixer/client/visualization_component/timeline/JsTimeLineEvent;)(bandIndex, event);
			}
		};
		for ( var i = 0; i < this.getBandCount(); i++) {
			var eventPainter = this.getBand(i)._eventPainter;
			if (eventPainter.addEventPaintListener) {
				eventPainter.addEventPaintListener(listener);
			}
		}
    }-*/;
    // @formatter:on

    // @formatter:off
    public final native String setCenterVisibleDate(String gmtString) /*-{
		// TODO change if bands are not synchronized any more
		// TODO parse date ?!?
		return this.getBand(0).setCenterVisibleDate(Date.parse(gmtString));
    }-*/;
    // @formatter:on

    /**
     * Sets the zoom index of a band. What time interval the zoom index refers
     * to depends on the band (defined in
     * {@link #create(Element, JsTimeLineEventSource, String)}). WARNING:
     * calling this function will change the center date of the band, call
     * {@link #setCenterVisibleDate(String)} afterwards.
     */
    // @formatter:off
    public final native void setZoomIndex(int bandNumber, int zoomIndex) /*-{
		// calculate number of steps because API function is boolean zoom with 
		// location.
		var band = this.getBand(bandNumber);
		var zoomDifference = zoomIndex - this.getBand(bandNumber)._zoomIndex;
		var zoomIn = zoomDifference < 0;
		var zoomSteps = Math.abs(zoomDifference);
		// did not quite work
		// var centerX = band.dateToPixelOffset(band.getCenterVisibleDate());

		var i = 0;
		for (i = 0; i < zoomSteps; i = i + 1) {
			this.getBand(bandNumber).zoom(zoomIn, 0, 0, null);
		}
    }-*/;
    // @formatter:on
}