package org.thechiselgroup.biomixer.client.core.ui;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ColorTest {

    @DataPoints
    public static String[] hexTestValues = new String[] { "#123f6d", "#0f5d3a",
            "#a7bccc", "#010101", "#000000", "#ffffff" };

    @Test
    public void hex000000() {
        assertEquals(new Color(0, 0, 0), new Color("#000000"));
    }

    @Test
    public void hex000083() {
        assertEquals(new Color(0, 0, 131), new Color("#000083"));
    }

    @Test
    public void hex0000ff() {
        assertEquals(new Color(0, 0, 255), new Color("#0000ff"));
    }

    @Test
    public void hex004ac0() {
        assertEquals(new Color(0, 74, 192), new Color("#004ac0"));
    }

    @Test
    public void hex00Cf00() {
        assertEquals(new Color(0, 207, 0), new Color("#00Cf00"));
    }

    @Test
    public void hex00ff00() {
        assertEquals(new Color(0, 255, 0), new Color("#00ff00"));
    }

    @Test
    public void hex2a0000() {
        assertEquals(new Color(42, 0, 0), new Color("#2a0000"));
    }

    @Test
    public void hex3100d6() {
        assertEquals(new Color(49, 0, 214), new Color("#3100d6"));
    }

    @Test
    public void hex3f6d00() {
        assertEquals(new Color(63, 109, 0), new Color("#3f6d00"));
    }

    @Test
    public void hex789abc() {
        assertEquals(new Color(120, 154, 188), new Color("#789abc"));
    }

    @Test
    public void hexff0000() {
        assertEquals(new Color(255, 0, 0), new Color("#ff0000"));
    }

    @Test
    public void hexffffff() {
        assertEquals(new Color(255, 255, 255), new Color("#ffffff"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHex00_000() {
        new Color("00-000");
        fail("Should have thrown IllegalArgumentException: hex contains invalid character");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHex00_500() {
        new Color("#00-500");
        fail("Should have thrown IllegalArgumentException: hex contains invalid character");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHex00g000() {
        new Color("#00g000");
        fail("Should have thrown IllegalArgumentException: hex contains invalid character");
    }

    @Test
    public void testInterpolateAll255() {
        Color color1 = new Color(255, 255, 255);
        Color color2 = new Color(255, 255, 255);

        assertEquals(new Color(255, 255, 255),
                color1.interpolateWith(color2, .95));
    }

    @Test
    public void testInterpolateAllZero() {
        Color color1 = new Color(0, 0, 0, 0);
        Color color2 = new Color(0, 0, 0, 0);
        assertEquals(new Color(0, 0, 0, 0), color1.interpolateWith(color2, 0.3));
    }

    @Test
    public void testInterpolateAverage() {
        Color color1 = new Color(18, 52, 86);
        Color color2 = new Color(36, 104, 172);
        assertEquals(new Color(27, 78, 129),
                color1.interpolateWith(color2, 0.5));
    }

    @Test
    public void testInterpolateWithAlpha() {
        Color color1 = new Color(200, 111, 5, 0.5);
        Color color2 = new Color(15, 155, 30, 0.15);
        assertEquals(new Color(158, 120, 10, 0.4223),
                color1.interpolateWith(color2, 0.222));
    }

    @Test
    public void testInterpolateZeroPointOne() {
        Color color1 = new Color(169, 226, 81);
        Color color2 = new Color(19, 87, 155);
        assertEquals(new Color(154, 212, 88),
                color1.interpolateWith(color2, 0.1));
    }

    @Theory
    public void toHex(String hex) {
        assertThat(new Color(hex).toHex(), equalTo(hex));
    }

}