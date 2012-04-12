/*******************************************************************************
 * Copyright 2011 Lars Grammel. All rights reserved.
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.core.util.text;

import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;

public interface TextBoundsEstimator {

    void configureFontFamily(String fontFamily);

    void configureFontSize(String fontSize);

    void configureFontStyle(String fontStyle);

    void configureFontWeight(String fontWeight);

    int getHeight(String text);

    SizeInt getSize(String text);

    int getWidth(String text);

    /**
     * Sets up this bounds estimator. Some implemented need to be attached to
     * the DOM, and thus this needs to be called before anything else.
     */
    void setUp();

    /**
     * Tears down this bounds estimator. Some implemented need to be attached to
     * the DOM, and this methods removes them again.
     */
    void tearDown();

}