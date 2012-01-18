package org.thechiselgroup.biomixer.client.visualization_component.timeline;

import java.util.Date;

public class BandInformation {

    private int bandIndex;

    private int zoomLevel;

    private String minVisibleDateGMTString;

    private String maxVisibleDateGMTString;

    public BandInformation(int bandIndex, int zoomLevel,
            String minVisibleDateGMTString, String maxVisibleDateGMTString) {

        this.bandIndex = bandIndex;
        this.zoomLevel = zoomLevel;
        this.maxVisibleDateGMTString = maxVisibleDateGMTString;
        this.minVisibleDateGMTString = minVisibleDateGMTString;
    }

    public int getBandIndex() {
        return bandIndex;
    }

    public Date getMaxVisibleDate() {
        return TimeLineWidget.GMT_FORMAT.parse(maxVisibleDateGMTString
                .substring(5));
    }

    public Date getMinVisibleDate() {
        return TimeLineWidget.GMT_FORMAT.parse(minVisibleDateGMTString
                .substring(5));
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    @Override
    public String toString() {
        return "BandInformation [bandIndex=" + bandIndex + ", zoomLevel="
                + zoomLevel + ", minVisibleDateGMTString="
                + minVisibleDateGMTString + ", maxVisibleDateGMTString="
                + maxVisibleDateGMTString + "]";
    }

}