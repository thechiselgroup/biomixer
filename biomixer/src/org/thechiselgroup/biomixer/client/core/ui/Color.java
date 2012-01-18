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
package org.thechiselgroup.biomixer.client.core.ui;

import java.io.Serializable;

public class Color implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static Color TRANSPARENT = new Color(255, 255, 255, 0.0);

    /**
     * Checks if value is between 0 and 255.
     */
    private static boolean isValidRgbComponentValue(int value) {
        return value >= 0 && value <= 255;
    }

    private static int parseRgbComponentValueFromHex(String hex,
            int beginIndex, int endIndex) {

        int value = Integer.parseInt(hex.substring(beginIndex, endIndex), 16);
        if (!isValidRgbComponentValue(value)) {
            throw new IllegalArgumentException(
                    "Argument given not of form #ffffff :" + hex);
        }
        return value;
    }

    private int red;

    private int green;

    private int blue;

    private double alpha;

    @SuppressWarnings("unused")
    private Color() {
        // for GWT serialization
        // XXX might need to be public
    }

    public Color(int red, int green, int blue) {
        this(red, green, blue, 1d);
    }

    public Color(int red, int green, int blue, double alpha) {
        assert isValidRgbComponentValue(red);
        assert isValidRgbComponentValue(green);
        assert isValidRgbComponentValue(blue);
        assert alpha >= 0 && alpha <= 1;

        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    /**
     * @param hex
     *            String in form #ffffff
     */
    public Color(String hex) {
        assert hex != null;

        if (hex.charAt(0) != '#' && hex.length() != 7) {
            throw new IllegalArgumentException(
                    "Argument given not of form #ffffff :" + hex);
        }

        try {
            this.alpha = 1.0;
            this.red = parseRgbComponentValueFromHex(hex, 1, 3);
            this.green = parseRgbComponentValueFromHex(hex, 3, 5);
            this.blue = parseRgbComponentValueFromHex(hex, 5, 7);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Argument given not of form #ffffff :" + hex);
        }
    }

    public Color alpha(double alpha) {
        assert alpha >= 0 && alpha <= 1;

        return new Color(red, green, blue, alpha);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Color other = (Color) obj;

        // JavaScript does not have doubleToLongBits
        if (Math.abs(alpha - other.alpha) > .000001) {
            return false;
        }

        if (blue != other.blue) {
            return false;
        }
        if (green != other.green) {
            return false;
        }
        if (red != other.red) {
            return false;
        }

        return true;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getBlue() {
        return blue;
    }

    public int getGreen() {
        return green;
    }

    public int getRed() {
        return red;
    }

    @Override
    public int hashCode() {
        /*
         * We ignore alpha in hashCode because JavaScript does not have
         * doubleToLongBits.
         */
        final int prime = 31;
        int result = 1;
        result = prime * result + blue;
        result = prime * result + green;
        result = prime * result + red;
        return result;
    }

    /**
     * Blends b into a with a blendFactor (percentage of b in result).
     */
    private double interpolate(double a, double b, double blendFactor) {
        return (1 - blendFactor) * a + blendFactor * b;
    }

    /**
     * Returns a color that is a blend of another color into this color.
     * 
     * @param otherColor
     *            Colors thats gets blend into this color.
     * @param blendFactor
     *            Specifies how much (percentage value between 0 and 1) of the
     *            resulting color is determined by {@code otherColor}.
     *            Effectively this calculates
     *            {@code blendFactor * otherColor + (1 - blendFactor) * this}
     */
    public Color interpolateWith(Color otherColor, double blendFactor) {
        assert blendFactor >= 0;
        assert blendFactor <= 1;
        assert otherColor != null;

        int red = (int) interpolate(getRed(), otherColor.getRed(), blendFactor);
        int green = (int) interpolate(getGreen(), otherColor.getGreen(),
                blendFactor);
        int blue = (int) interpolate(getBlue(), otherColor.getBlue(),
                blendFactor);
        double alpha = interpolate(getAlpha(), otherColor.getAlpha(),
                blendFactor);

        return new Color(red, green, blue, alpha);
    }

    public Color opaque() {
        return new Color(red, green, blue);
    }

    /**
     * @return This will return the hexidecimal representation of the color in
     *         the form "#ffffff".
     */
    public String toHex() {
        String hexVal = Integer.toHexString((this.red << 16)
                | (this.green << 8) | this.blue);

        // This String may not have prepending zeros, we must add them to ensure
        // 6 hex digits
        for (int i = hexVal.length(); i < 6; i++) {
            hexVal = "0" + hexVal;
        }
        return "#" + hexVal;
    }

    public String toRGB() {
        return "rgb(" + red + "," + green + "," + blue + ")";
    }

    public String toRGBa() {
        return "rgba(" + red + "," + green + "," + blue + "," + alpha + ")";
    }

    @Override
    public String toString() {
        return toRGBa();
    }

}