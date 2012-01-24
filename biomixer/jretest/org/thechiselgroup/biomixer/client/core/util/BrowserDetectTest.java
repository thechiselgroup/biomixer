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
package org.thechiselgroup.biomixer.client.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class BrowserDetectTest {

    private BrowserDetect underTest;

    @Test
    public void chrome3() {
        assertEquals(false, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-US) "
                + "AppleWebKit/533.2 (KHTML, like Gecko) "
                + "Chrome/3.0.342.2 Safari/533.2"));
    }

    @Test
    public void chrome4() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-US) "
                + "AppleWebKit/533.2 (KHTML, like Gecko) "
                + "Chrome/4.0.342.2 Safari/533.2"));
    }

    @Test
    public void chrome5() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-US) "
                + "AppleWebKit/533.2 (KHTML, like Gecko) "
                + "Chrome/5.0.342.2 Safari/533.2"));
    }

    @Test
    public void firefox3_4() {
        assertEquals(false, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-US; rv:1.9.1.8) "
                + "Gecko/20100202 " + "Firefox/3.4.1 (.NET CLR 3.5.30729)"));
    }

    @Test
    public void firefox3_5() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-US; rv:1.9.1.8) "
                + "Gecko/20100202 " + "Firefox/3.5.8 (.NET CLR 3.5.30729)"));
    }

    @Test
    public void firefox3_6() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-US; rv:1.9.1.8) "
                + "Gecko/20100202 " + "Firefox/3.6.0 (.NET CLR 3.5.30729)"));
    }

    @Test
    public void firefox3_6b() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-GB; rv:1.9.2) "
                + "Gecko/20100115 Firefox/3.6"));
    }

    @Test
    public void firefox4_0b6() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows NT 6.1; WOW64; rv:2.0b6) "
                + "Gecko/20100101 Firefox/4.0b6"));
    }

    @Test
    public void ie8() {
        assertEquals(false, underTest.isValidBrowser("Mozilla/4.0 "
                + "(compatible; MSIE 8.0; Windows NT 5.1; "
                + "Trident/4.0; .NET CLR 1.1.4322; "
                + ".NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; "
                + ".NET CLR 3.5.30729; InfoPath.2)"));
    }

    @Test
    public void ie9() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(compatible; MSIE 9.0; Windows NT 6.1; "
                + "WOW64; Trident/5.0; SLCC2; "
                + ".NET CLR 2.0.50727; .NET CLR 3.5.30729; "
                + ".NET CLR 3.0.30729; Media Center PC 6.0"
                + "; MAAU; .NET4.0C; Alexa Toolbar; InfoPath.3)"));
    }

    @Test
    public void opera9() {
        assertEquals(false, underTest.isValidBrowser("Opera/9.50 "
                + "(Windows NT 5.1; U; en)"));
    }

    @Test
    public void safari4() {
        assertEquals(false, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 5.1; en-US) "
                + "AppleWebKit/531.21.8 (KHTML, like Gecko) "
                + "Version/4.0.4 Safari/531.21.10"));
    }

    @Test
    public void safari5() {
        assertEquals(true, underTest.isValidBrowser("Mozilla/5.0 "
                + "(Windows; U; Windows NT 6.1; en-US) "
                + "AppleWebKit/533.18.1 (KHTML, like Gecko) "
                + "Version/5.0.2 Safari/533.18.5"));
    }

    @Before
    public void setUp() {
        this.underTest = new BrowserDetect();
    }
}
