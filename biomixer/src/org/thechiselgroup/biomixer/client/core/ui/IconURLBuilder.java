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
package org.thechiselgroup.biomixer.client.core.ui;

import com.google.gwt.core.client.GWT;

public final class IconURLBuilder {

    public static enum IconType {

        NORMAL("normal"), DISABLED("disabled"), HIGHLIGHTED("highlighted");

        private String name;

        IconType(String name) {
            this.name = name;
        }

    }

    public static final String PATH = "images/";

    public static final String PREFIX = "icon";

    public static final String SEPARATOR = "-";

    public static final String SUFFIX = ".png";

    public static String getIconUrl(String name, IconType type) {
        if (name == null) {
            return null;
        }

        return getIconUrl(name, type, PREFIX + SEPARATOR);
    }

    public static String getIconUrl(String name, IconType type, String prefix) {
        if (name == null) {
            return null;
        }

        return GWT.getModuleBaseURL() + PATH + prefix + name + SEPARATOR
                + type.name + SUFFIX;
    }

    private IconURLBuilder() {
    }
}
