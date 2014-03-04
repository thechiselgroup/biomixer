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

import com.google.gwt.user.client.Window;

public class BrowserDetect {

    public boolean isValidBrowser() {
        return isValidBrowser(Window.Navigator.getUserAgent());
    }

    @Deprecated
    public boolean isValidBrowser(String userAgent) {
        try {
            if (userAgent == null) {
                return false;
            }

            userAgent = userAgent.toLowerCase();

            if (userAgent.contains("firefox")) {
                int index = userAgent.indexOf("firefox");
                index += "firefox".length();
                int firstDot = userAgent.indexOf(".", index);
                int major = Integer.parseInt(userAgent.substring(index + 1,
                        firstDot));

                if (major > 3) {
                    return true;
                }

                if (major < 3) {
                    return false;
                }

                int secondDot = userAgent.indexOf(".", firstDot + 1);
                if (secondDot == -1) {
                    secondDot = userAgent.length();
                }
                int minor = Integer.parseInt(userAgent.substring(firstDot + 1,
                        secondDot));

                if (major == 3 && minor < 5) {
                    return false;
                }

                return true;
            }

            if (userAgent.contains("chrome")) {
                int index = userAgent.indexOf("chrome");
                index += "chrome".length();
                int firstDot = userAgent.indexOf(".", index);

                int major = Integer.parseInt(userAgent.substring(index + 1,
                        firstDot));

                if (major < 4) {
                    return false;
                }

                return true;
            }

            if (userAgent.contains("safari")) {
                int index = userAgent.indexOf("version");
                index += "version".length();
                int firstDot = userAgent.indexOf(".", index);

                int major = Integer.parseInt(userAgent.substring(index + 1,
                        firstDot));

                if (major <= 4) {
                    return false;
                }

                return true;
            }

            if (userAgent.contains("msie")) {
                int index = userAgent.indexOf("msie") + "msie ".length();
                int firstDot = userAgent.indexOf(".", index);

                int major = Integer.parseInt(userAgent.substring(index,
                        firstDot));

                if (major <= 8) {
                    return false;
                }

                return true;
            }

            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
